package com.bannergress.backend.mission.queue;

import com.bannergress.backend.mission.Mission;
import com.bannergress.backend.mission.MissionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
public class MissionQueueServiceImpl implements MissionQueueService {
    private final Duration lease;

    @Autowired
    private MissionRepository missionRepository;

    public MissionQueueServiceImpl(@Value(value = "${queue.leaseSeconds:60}") long leaseSeconds) {
        lease = Duration.ofSeconds(leaseSeconds);
    }

    @Override
    public void queueUpdateAuthor(Mission mission) {
        if (mission.getAuthorUpdateQueuedSince() == null) {
            mission.setAuthorUpdateQueuedSince(Instant.now());
        }
    }

    @Override
    public void queueUpdateStatus(Mission mission) {
        if (mission.getStatusUpdateQueuedSince() == null) {
            mission.setStatusUpdateQueuedSince(Instant.now());
        }
    }

    @Override
    public Optional<Mission> dequeueUpdateAuthor() {
        Instant now = Instant.now();
        return missionRepository.findFirstByAuthorUpdateQueuedSinceLessThanOrderByAuthorUpdateQueuedSince(now)
            .map(mission -> {
                mission.setAuthorUpdateQueuedSince(now.plus(lease));
                return mission;
            });
    }

    @Override
    public Optional<Mission> dequeueUpdateStatus() {
        Instant now = Instant.now();
        return missionRepository.findFirstByStatusUpdateQueuedSinceLessThanOrderByStatusUpdateQueuedSince(now)
            .map(mission -> {
                mission.setStatusUpdateQueuedSince(now.plus(lease));
                return mission;
            });
    }

    @Override
    public void satisfyUpdateAuthor(Mission mission) {
        mission.setAuthorUpdateQueuedSince(null);
    }

    @Override
    public void satisfyUpdateStatus(Mission mission) {
        mission.setStatusUpdateQueuedSince(null);
    }
}
