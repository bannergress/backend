package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.BannerPicture;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.services.BannerPictureService;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
        hasher.putLong(banner.getId()).putInt(banner.getNumberOfMissions());
        for (Entry<Integer, Mission> entry : banner.getMissions().entrySet()) {
            hasher.putInt(entry.getKey()).putUnencodedChars(entry.getValue().getPicture().toString());
        }
        return hasher.hash().toString();
    }

    private byte[] createPicture(Banner banner) {
        int numberColumns = 6;
        int numberRows = banner.getNumberOfMissions() / numberColumns;
        int DISTANCE_CIRCLES = 10;
        int DIAMETER = 83;
        int width = numberColumns * DIAMETER + (numberColumns + 1) * DISTANCE_CIRCLES;
        int height = numberRows * DIAMETER + (numberRows + 1) * DISTANCE_CIRCLES;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        // TODO actually draw something

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    @Override
    public Optional<BannerPicture> findByHash(String hash) {
        return Optional.ofNullable(entityManager.find(BannerPicture.class, hash));
    }
}
