package com.bannergress.backend.mission.partition;

import java.util.List;

public interface PartitionService {
    List<PartitionRegion> partition(PartitionRegion startRegion);
}
