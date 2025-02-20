package com.bannergress.backend.mission;

import com.bannergress.backend.agent.AgentService;
import com.bannergress.backend.agent.Faction;
import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.BannerService;
import com.bannergress.backend.banner.picture.BannerPictureService;
import com.bannergress.backend.mission.step.MissionStep;
import com.bannergress.backend.mission.step.Objective;
import com.bannergress.backend.poi.POI;
import com.bannergress.backend.poi.POIType;
import com.bannergress.backend.spatial.Spatial;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseImportServiceImpl {
    private static final String AUTHOR_UNKNOWN = "unknown";

    @Autowired
    private AgentService agentService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private BannerPictureService bannerPictureService;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected Spatial spatial;

    protected Mission importMissionById(String id) {
        Mission mission = entityManager.find(Mission.class, id);
        if (mission == null) {
            mission = new Mission();
            mission.setId(id);
        }
        return mission;
    }

    protected final void setMissionAuthor(Mission mission, String newAuthor, Faction newAuthorFaction) {
        if (newAuthor != null) {
            if (newAuthor.equalsIgnoreCase(AUTHOR_UNKNOWN)) {
                mission.setAuthor(null);
            } else {
                mission.setAuthor(agentService.importAgent(newAuthor, newAuthorFaction));
            }
        }
    }

    protected final void setMissionAverageDurationMilliseconds(Mission mission, Long newAverageDurationMilliseconds) {
        if (newAverageDurationMilliseconds != null) {
            mission.setAverageDurationMilliseconds(newAverageDurationMilliseconds);
        }
    }

    protected final void setMissionDescription(Mission mission, String newDescription) {
        if (newDescription != null) {
            mission.setDescription(stripNullChars(newDescription));
            mission.setLatestUpdateSummary(Instant.now());
        }
    }

    protected final void setMissionNumberCompleted(Mission mission, Integer numberCompleted) {
        if (numberCompleted != null) {
            mission.setNumberCompleted(numberCompleted);
        }
    }

    protected final void setMissionStatus(Mission mission, MissionStatus newStatus, RecalculationTracker tracker) {
        boolean setLatestUpdate = newStatus != null;
        newStatus = newStatus == null ? mission.getStatus() : newStatus;
        if (newStatus == MissionStatus.published && isOfflineBecauseNoStepAvailable(mission)) {
            newStatus = MissionStatus.disabled;
        }
        if (newStatus != mission.getStatus()) {
            setLatestUpdate = true;
            mission.setStatus(newStatus);
            tracker.add(mission);
        }
        if (setLatestUpdate) {
            mission.setLatestUpdateStatus(Instant.now());
        }
    }

    protected final void setMissionPicture(Mission mission, URL newPicture, RecalculationTracker tracker) {
        if (newPicture != null && !Objects.equals(newPicture, mission.getPicture())) {
            mission.setPicture(newPicture);
            tracker.add(mission);
        }
    }

    protected final void setMissionRating(Mission mission, Double newRating) {
        if (newRating != null) {
            mission.setRating(newRating);
        }
    }

    protected final void setMissionTitle(Mission mission, String newTitle) {
        if (newTitle != null) {
            mission.setTitle(stripNullChars(newTitle));
        }
    }

    protected final void setMissionType(Mission mission, MissionType newType) {
        if (newType != null) {
            mission.setType(newType);
        }
    }

    protected void setMissionStepSize(Mission mission, int newSize, RecalculationTracker tracker) {
        int oldSize = mission.getSteps().size();
        if (newSize != mission.getSteps().size()) {
            for (int i = oldSize; i < newSize; i++) {
                MissionStep step = new MissionStep();
                step.setMission(mission);
                mission.getSteps().add(step);
            }
            for (int i = oldSize - 1; i >= newSize; i--) {
                mission.getSteps().remove(i);
            }
            tracker.add(mission);
        }
        mission.setLatestUpdateDetails(Instant.now());
    }

    protected void setStepObjective(MissionStep step, Objective newObjective) {
        step.setObjective(newObjective);
    }

    protected void setStepPoi(MissionStep step, POI newPoi, RecalculationTracker tracker) {
        if (!Objects.equals(newPoi, step.getPoi())) {
            step.setPoi(newPoi);
            tracker.add(step.getMission());
        }
    }

    protected POI importPoiById(String id) {
        POI poi = entityManager.find(POI.class, id);
        if (poi == null) {
            poi = new POI();
            poi.setId(id);
        }
        return poi;
    }

    protected final void setPoiPoint(POI poi, Point newPoint, RecalculationTracker tracker) {
        if (newPoint != null && !Objects.equals(newPoint, poi.getPoint())) {
            poi.setPoint(newPoint);
            tracker.add(poi);
        }
    }

    protected final void setPoiPicture(POI poi, URL newPicture) {
        if (newPicture != null) {
            poi.setPicture(newPicture);
        }
    }

    protected final void setPoiTitle(POI poi, String newTitle) {
        if (newTitle != null) {
            poi.setTitle(stripNullChars(newTitle));
        }
    }

    protected final void setPoiType(POI poi, POIType newType, RecalculationTracker tracker) {
        if (newType != null && !Objects.equals(newType, poi.getType())) {
            poi.setType(newType);
            tracker.add(poi);
        }
    }

    private boolean isOfflineBecauseNoStepAvailable(Mission mission) {
        // A mission where all steps are unavailable is counted as an offline mission
        // Hidden steps are counted as available
        return mission.getSteps().size() > 0 && mission.getSteps().stream()
            .noneMatch(step -> step.getPoi() == null || step.getPoi().getType() != POIType.unavailable);
    }

    private static String stripNullChars(String text) {
        return text == null ? null : text.replace("\0", "");
    }

    protected final <X> X withRecalculation(Function<RecalculationTracker, X> importFunction) {
        RecalculationTracker tracker = new RecalculationTracker();
        X result = importFunction.apply(tracker);

        Set<Banner> banners = getBannersForRecalculation(tracker);

        for (Banner banner : banners) {
            bannerService.calculateData(banner);
            bannerPictureService.refresh(banner);
        }

        return result;
    }

    protected final void withRecalculation(Consumer<RecalculationTracker> importFunction) {
        withRecalculation(tracker -> {
            importFunction.accept(tracker);
            return null;
        });
    }

    private Set<Banner> getBannersForRecalculation(RecalculationTracker tracker) {
        Set<Banner> banners = new HashSet<>();

        if (!tracker.pois.isEmpty()) {
            TypedQuery<Banner> queryByPoi = entityManager.createQuery(
                "SELECT b FROM Banner b JOIN b.missions m JOIN m.steps s WHERE s.poi IN :pois", Banner.class);
            queryByPoi.setParameter("pois", tracker.pois);
            banners.addAll(queryByPoi.getResultList());
        }

        if (!tracker.missions.isEmpty()) {
            TypedQuery<Banner> queryByMission = entityManager
                .createQuery("SELECT b FROM Banner b JOIN b.missions m WHERE m IN :missions", Banner.class);
            queryByMission.setParameter("missions", tracker.missions);
            banners.addAll(queryByMission.getResultList());
        }

        return banners;
    }

    public static class RecalculationTracker {
        private Set<Mission> missions = new HashSet<>();

        private Set<POI> pois = new HashSet<>();

        protected void add(Mission mission) {
            missions.add(mission);
        }

        protected void add(POI poi) {
            pois.add(poi);
        }

        private RecalculationTracker() {
        }
    }
}
