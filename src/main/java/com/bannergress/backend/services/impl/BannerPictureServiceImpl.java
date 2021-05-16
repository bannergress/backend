package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.BannerPicture;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.services.BannerPictureService;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * Default implementation of {@link BannerPictureService}.
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class BannerPictureServiceImpl implements BannerPictureService {

    /**
     * The version of this implementation. Change in order to have banner images re-created after
     * an implementation change.
     */
    public static final int IMPLEMENTATION_VERSION = 3;

    private static ForkJoinPool threadPool = new ForkJoinPool(20);

    /**
     * Banner picture compression quality, a value between 0 for low and 1 for high quality.
     *
     * @see ImageWriteParam#setCompressionQuality(float)
     */
    private float compressionQuality;

    @Autowired
    EntityManager entityManager;

    private final OkHttpClient client;

    public BannerPictureServiceImpl(Cache cache, @Value(value = "${picture.quality:0.92f}") float compressionQuality) {
        client = new OkHttpClient.Builder().cache(cache).build();
        this.compressionQuality = compressionQuality;
    }

    @Override
    public void refresh(Banner banner) {
        BannerPicture oldPicture = banner.getPicture();
        if (oldPicture != null) {
            oldPicture.setExpiration(Instant.now().plusSeconds(60));
        }
        String hash = hash(banner);
        BannerPicture newPicture = entityManager.find(BannerPicture.class, hash);
        if (newPicture == null) {
            // Create a new picture
            byte[] picture = createPicture(banner);
            newPicture = new BannerPicture();
            newPicture.setHash(hash);
            newPicture.setPicture(picture);
            entityManager.persist(newPicture);
        } else {
            // Reuse the existing picture, clear potential expiration
            newPicture.setExpiration(null);
        }
        banner.setPicture(newPicture);
    }

    /**
     * Calculates a hash over all picture-relevant attributes of a banner.
     *
     * @param banner Banner.
     * @return Hash.
     */
    private String hash(Banner banner) {
        Hasher hasher = Hashing.murmur3_128().newHasher();
        hasher.putInt(IMPLEMENTATION_VERSION).putFloat(compressionQuality).putInt(banner.getWidth());
        for (Entry<Integer, Mission> entry : banner.getMissions().entrySet()) {
            hasher.putInt(entry.getKey()).putUnencodedChars(entry.getValue().getPicture().toString())
                .putBoolean(entry.getValue().isOnline());
        }
        return hasher.hash().toString();
    }

    private static final BufferedImage maskImageOnline = loadImage("/mask-96-online.png");
    private static final BufferedImage maskImageOffline = loadImage("/mask-96-offline.png");

    private static BufferedImage loadImage(String path) {
        try (InputStream stream = BannerPictureServiceImpl.class.getResourceAsStream(path)) {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected byte[] createPicture(Banner banner) {
        final int numberColumns = banner.getWidth();
        final int numberRows = banner.getMissions().lastKey() / numberColumns + 1;
        final int DISTANCE_CIRCLES = 4;
        final int DIAMETER = 96;
        final int MISSIONSIZE = DIAMETER + DISTANCE_CIRCLES;
        BufferedImage bannerImage = new BufferedImage(numberColumns * MISSIONSIZE + DISTANCE_CIRCLES,
            numberRows * MISSIONSIZE + DISTANCE_CIRCLES, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bannerImage.createGraphics();
        graphics.setPaint(new Color(46, 46, 46));
        graphics.fillRect(0, 0, bannerImage.getWidth(), bannerImage.getHeight());
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        // loads, draws and masks the individual mission images to the banner image.
        try {
            threadPool.submit(() -> banner.getMissions().entrySet().parallelStream().forEach(entry -> {
                BufferedImage missionImage;
                Request request = new Request.Builder().url(entry.getValue().getPicture()).build();
                try (Response response = client.newCall(request).execute()) {
                    missionImage = ImageIO.read(response.body().byteStream());
                } catch (IOException ex) {
                    throw new RuntimeException("failed ro read image: " + entry.getValue().getPicture(), ex);
                }
                int missionPosition = numberColumns * numberRows - entry.getKey().intValue() - 1;
                int x1 = DISTANCE_CIRCLES + (missionPosition % numberColumns) * MISSIONSIZE;
                int y1 = DISTANCE_CIRCLES + (missionPosition / numberColumns) * MISSIONSIZE;
                int x2 = x1 + DIAMETER;
                int y2 = y1 + DIAMETER;
                synchronized (graphics) {
                    graphics.drawImage(missionImage, x1, y1, x2, y2, 0, 0, missionImage.getWidth(),
                        missionImage.getHeight(), null);
                    BufferedImage maskImage = entry.getValue().isOnline() ? maskImageOnline : maskImageOffline;
                    graphics.drawImage(maskImage, x1, y1, x2, y2, 0, 0, maskImage.getWidth(), maskImage.getHeight(),
                        null);
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        graphics.dispose();

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream(24 * 1024 * numberRows);
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(stream)) {
            ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(compressionQuality);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(null, new IIOImage(bannerImage, null, null), imageWriteParam);
            imageWriter.dispose();
            return stream.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("failed to generate banner with id " + banner.getUuid(), ex);
        }
    }

    @Override
    public Optional<BannerPicture> findByHash(String hash) {
        return Optional.ofNullable(entityManager.find(BannerPicture.class, hash));
    }

    @Override
    public void removeExpired() {
        TypedQuery<BannerPicture> query = entityManager
            .createQuery("SELECT p FROM BannerPicture p WHERE p.expiration < :now", BannerPicture.class);
        query.setParameter("now", Instant.now());
        List<BannerPicture> pictures = query.getResultList();
        for (BannerPicture picture : pictures) {
            if (picture.getBanners().isEmpty()) {
                entityManager.remove(picture);
            } else {
                picture.setExpiration(null);
            }
        }
    }
}
