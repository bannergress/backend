package com.bannergress.backend.services.impl;

import com.bannergress.backend.services.BannerPictureMaintenanceService;
import com.bannergress.backend.services.BannerPictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.Instant;

/**
 * Default implementation of {@link BannerPictureService}.
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class BannerPictureMaintenanceServiceImpl implements BannerPictureMaintenanceService {

    @Autowired
    EntityManager entityManager;

    @Override
    public void removeExpired() {
        entityManager.createQuery("DELETE FROM BannerPicture WHERE expiration < :now")
            .setParameter("now", Instant.now())
            .executeUpdate();
    }

}
