package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.BannerPicture;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.services.BannerPictureService;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Optional;

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
    public static final int IMPLEMENTATION_VERSION = 1;

    @Autowired
    EntityManager entityManager;

    @Override
    public void refresh(Banner banner) {
        String hash = hash(banner);
        if (banner.getPicture() != null && hash.equals(banner.getPicture().getHash())) {
            return;
        }
        byte[] picture = createPicture(banner);
        BannerPicture bannerPicture = new BannerPicture();
        bannerPicture.setHash(hash);
        bannerPicture.setPicture(picture);
        entityManager.persist(bannerPicture);
        banner.setPicture(bannerPicture);
    }

    /**
     * Calculates a hash over all picture-relevant attributes of a banner.
     *
     * @param banner Banner.
     * @return Hash.
     */
    private String hash(Banner banner) {
        Hasher hasher = Hashing.murmur3_128().newHasher();
        hasher.putInt(IMPLEMENTATION_VERSION).putLong(banner.getId()).putInt(banner.getNumberOfMissions());
        for (Entry<Integer, Mission> entry : banner.getMissions().entrySet()) {
            hasher.putInt(entry.getKey()).putUnencodedChars(entry.getValue().getPicture().toString());
        }
        return hasher.hash().toString();
    }

    protected static final BufferedImage maskImage;
    static {
        try {
            maskImage = ImageIO.read(new ClassPathResource("mask-96.png").getFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected byte[] createPicture(Banner banner) {
        final int numberColumns = 6;
        final int numberRows = banner.getNumberOfMissions() / numberColumns;
        final int DISTANCE_CIRCLES = 4;
        final int DIAMETER = 96;
        final int MISSIONSIZE = DIAMETER + DISTANCE_CIRCLES;
        BufferedImage bannerImage = new BufferedImage(numberColumns * MISSIONSIZE + DISTANCE_CIRCLES,
            numberRows * MISSIONSIZE + DISTANCE_CIRCLES, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bannerImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        // loads, draws and masks the individual mission images to the banner image.
        for (Entry<Integer, Mission> entry : banner.getMissions().entrySet()) {
            BufferedImage missionImage;
            try {
                missionImage = ImageIO.read(entry.getValue().getPicture());
            } catch (IOException ex) {
                throw new RuntimeException("failed ro read image: " + entry.getValue().getPicture(), ex);
            }
            int missionPosition = banner.getNumberOfMissions() - entry.getKey().intValue() - 1;
            int x1 = DISTANCE_CIRCLES + (missionPosition % numberColumns) * MISSIONSIZE;
            int y1 = DISTANCE_CIRCLES + (missionPosition / numberColumns) * MISSIONSIZE;
            int x2 = x1 + DIAMETER;
            int y2 = y1 + DIAMETER;
            graphics.drawImage(missionImage, x1, y1, x2, y2, 0, 0, missionImage.getWidth(), missionImage.getHeight(),
                null);
            graphics.drawImage(maskImage, x1, y1, x2, y2, 0, 0, maskImage.getWidth(), maskImage.getHeight(), null);
        }

        graphics.dispose();

        // blurs a bit
        bannerImage = new ConvolveOp(
            new Kernel(3, 3, new float[] {0f, 0.125f, 0f, 0.125f, 0.5f, 0.125f, 0f, 0.125f, 0f}), ConvolveOp.EDGE_NO_OP,
            null).filter(bannerImage, null);

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream(40 * 1024 * numberRows)) {
            ImageIO.write(bannerImage, "jpg", stream);
            return stream.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("failed to generate banner with id " + banner.getId(), ex);
        }
    }

    @Override
    public Optional<BannerPicture> findByHash(String hash) {
        return Optional.ofNullable(entityManager.find(BannerPicture.class, hash));
    }
}
