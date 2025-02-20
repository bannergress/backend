package com.bannergress.backend.banner.picture;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.mission.Mission;
import com.bannergress.backend.mission.MissionStatus;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

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
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * Default implementation of {@link BannerPictureService}.
 */
@Service
@Transactional
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
        setPictureExpired(oldPicture);
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
            hasher.putInt(entry.getKey());
            Mission mission = entry.getValue();
            if (mission.getPicture() != null) {
                hasher.putUnencodedChars(mission.getPicture().toString());
            }
            hasher.putBoolean(mission.getStatus() == MissionStatus.published);
        }
        for (Integer position : banner.getPlaceholders()) {
            hasher.putInt(position);
        }
        // The following lines break hash compatibility for banners with at least one disabled mission.
        // Can be removed again when the IMPLEMENTATION_VERSION increases.
        if (banner.getNumberOfDisabledMissions() > 0) {
            hasher.putUnencodedChars("EXTRA_DISABLED");
        }
        // The following lines break hash compatibility for banners with all missions disabled or submitted,
        // and at least one mission submitted.
        // Can be removed again when the IMPLEMENTATION_VERSION increases.
        if (isOnlyDisabledOrSubmitted(banner) && banner.getNumberOfSubmittedMissions() > 0) {
            hasher.putUnencodedChars("EXTRA_ALL_DISABLED_OR_SUBMITTED");
        }
        return hasher.hash().toString();
    }

    private boolean isOnlyDisabledOrSubmitted(Banner banner) {
        return banner.getNumberOfDisabledMissions() + banner.getNumberOfSubmittedMissions() == banner.getNumberOfMissions();
    }

    private static final BufferedImage maskImageOnline = loadImage("mask-100-online.png");
    private static final BufferedImage maskImageOffline = loadImage("mask-100-offline.png");
    private static final BufferedImage maskImagePlaceholder = loadImage("mask-100-placeholder.png");

    private static BufferedImage loadImage(String path) {
        try (InputStream stream = BannerPictureServiceImpl.class.getResourceAsStream(path)) {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected byte[] createPicture(Banner banner) {
        final int numberColumns = banner.getWidth();
        boolean onlyDisabledOrSubmitted = isOnlyDisabledOrSubmitted(banner);
        SortedMap<Integer, Optional<Mission>> missionsAndPlaceholders = banner.getMissionsAndPlaceholders();
        final int numberRows = missionsAndPlaceholders.lastKey() / numberColumns + 1;
        final int TILESIZE = 100;
        final int OUTER_PADDING = 2;
        final int MISSION_PADDING = 2;
        BufferedImage bannerImage = new BufferedImage(numberColumns * TILESIZE + 2 * OUTER_PADDING,
            numberRows * TILESIZE + 2 * OUTER_PADDING, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bannerImage.createGraphics();
        graphics.setPaint(new Color(46, 46, 46));
        graphics.fillRect(0, 0, bannerImage.getWidth(), bannerImage.getHeight());
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        // loads, draws and masks the individual mission images to the banner image.
        try {
            threadPool.submit(() -> missionsAndPlaceholders.entrySet().parallelStream().forEach(entry -> {
                Optional<BufferedImage> optionalMissionImage = entry.getValue().map(Mission::getPicture)
                    .filter(Objects::nonNull).map(url -> {
                        Request request = new Request.Builder().url(url).build();
                        try (Response response = client.newCall(request).execute()) {
                            return ImageIO.read(response.body().byteStream());
                        } catch (IOException ex) {
                            throw new RuntimeException("failed to read image: " + url, ex);
                        }
                    });
                int missionPosition = numberColumns * numberRows - entry.getKey().intValue() - 1;
                int x1 = OUTER_PADDING + (missionPosition % numberColumns) * TILESIZE;
                int y1 = OUTER_PADDING + (missionPosition / numberColumns) * TILESIZE;
                int x2 = x1 + TILESIZE;
                int y2 = y1 + TILESIZE;
                synchronized (graphics) {
                    optionalMissionImage.ifPresent(missionImage -> {
                        graphics.drawImage(missionImage, x1 + MISSION_PADDING, y1 + MISSION_PADDING,
                            x2 - MISSION_PADDING, y2 - MISSION_PADDING, 0, 0, missionImage.getWidth(),
                            missionImage.getHeight(), null);
                    });
                    MissionStatus drawStatus = entry.getValue().map(Mission::getStatus).orElse(MissionStatus.submitted);
                    drawOverlay(graphics, x1, y1, x2, y2, drawStatus, onlyDisabledOrSubmitted);
                }
            })).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            graphics.dispose();
        }

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

    private void drawOverlay(Graphics2D graphics, int x1, int y1, int x2, int y2, MissionStatus status,
                             boolean onlyDisabledOrSubmitted) {
        BufferedImage maskImage = getMaskImage(status, onlyDisabledOrSubmitted);
        graphics.drawImage(maskImage, x1, y1, x2, y2, 0, 0, maskImage.getWidth(), maskImage.getHeight(), null);
    }

    private BufferedImage getMaskImage(MissionStatus status, boolean onlyDisabledOrSubmitted) {
        switch (status) {
            case published:
                return maskImageOnline;
            case disabled:
                return onlyDisabledOrSubmitted ? maskImageOnline : maskImageOffline;
            case submitted:
                return maskImagePlaceholder;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public Optional<BannerPicture> findByHash(String hash) {
        return Optional.ofNullable(entityManager.find(BannerPicture.class, hash));
    }

    @Override
    public void setPictureExpired(BannerPicture picture) {
        if (picture != null) {
            picture.setExpiration(Instant.now().plusSeconds(3_600));
        }
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
