package com.bannergress.backend.event;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.services.BannerPictureService;
import com.bannergress.backend.services.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

@Component
public class BannerRecalculationListeners {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private BannerPictureService bannerPictureService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onPOIChanged(POIChangedEvent event) {
        TypedQuery<Banner> query = entityManager.createQuery(
            "SELECT b FROM Banner b JOIN b.missions m JOIN m.steps s WHERE s.poi = :poi", Banner.class);
        query.setParameter("poi", event.getPoi());
        query.getResultList().stream().forEach((banner) -> {
            bannerService.calculateData(banner);
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onMissionChanged(MissionChangedEvent event) {
        TypedQuery<Banner> query = entityManager
            .createQuery("SELECT b FROM Banner b WHERE :mission MEMBER OF b.missions", Banner.class);
        query.setParameter("mission", event.getMission());
        query.getResultList().stream().forEach((banner) -> {
            bannerService.calculateData(banner);
            bannerPictureService.refresh(banner);
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onBannerChanged(BannerChangedEvent event) {
        bannerService.calculateData(event.getBanner());
        bannerPictureService.refresh(event.getBanner());
    }
}
