package com.bannergress.backend.mission.partition;

import com.bannergress.backend.spatial.Spatial;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.ToDoubleFunction;

import static com.google.common.base.Preconditions.checkArgument;

import static java.util.Comparator.comparingDouble;

@Service
class PartitionServiceImpl implements PartitionService {
    private static final int ROUND_DIGITS = 6;

    private static final double MIN_ALLOWED_DIFFERENCE = 1.5 * Math.pow(10, -ROUND_DIGITS);

    private static final int TARGET_SIZE = 24;

    private static final int MAX_GREEDY_SPLIT_SIZE = 100;

    private static final Comparator<PartitionRegion> GREEDY_COMPARATOR = Comparator.comparing(x -> {
        int size = x.points().size();
        return size <= TARGET_SIZE ? -size : size;
    });

    @FunctionalInterface
    private interface SplitStrategy {
        int getSecondRegionMinIndex(int iteration, int listSize);
    }

    private static final SplitStrategy GREEDY_STRATEGY = (i, size) -> {
        int j = i / 2;
        int k = j < TARGET_SIZE ? TARGET_SIZE - j : TARGET_SIZE + j + 1;
        return i % 2 == 0 ? k : size - k;
    };

    private static final SplitStrategy EQUAL_STRATEGY = (i, size) -> (size / 2) + ((i + 1) / 2) * (i % 2 == 0 ? -1 : 1);

    @Override
    public List<PartitionRegion> partition(PartitionRegion startRegion) {
        Deque<PartitionRegion> queue = new ArrayDeque<>();
        queue.add(startRegion);

        List<PartitionRegion> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            PartitionRegion region = queue.removeFirst();
            List<PartitionRegion> splitRegions = split(region);
            if (splitRegions.size() >= 2) {
                queue.addAll(splitRegions);
            } else {
                result.addAll(splitRegions);
            }
        }
        return result;
    }

    private List<PartitionRegion> split(PartitionRegion region) {
        int size = region.points().size();
        if (size <= TARGET_SIZE) {
            return List.of(region);
        } else if (size <= MAX_GREEDY_SPLIT_SIZE) {
            return splitGreedily(region);
        } else {
            return splitEqually(region);
        }
    }

    private List<PartitionRegion> splitGreedily(PartitionRegion region) {
        var candidates = List.of( //
            splitByStrategy(region, SplitType.latitude, GREEDY_STRATEGY), //
            splitByStrategy(region, SplitType.longitude, GREEDY_STRATEGY) //
        );
        if (getBestSplitType(region.envelope()) == SplitType.longitude) {
            candidates = candidates.reversed();
        }
        return candidates.stream().sorted(Comparator.comparing(this::getBestGreedyRegion, GREEDY_COMPARATOR))
            .findFirst().get();
    }

    private PartitionRegion getBestGreedyRegion(List<PartitionRegion> regions) {
        return regions.stream().sorted(GREEDY_COMPARATOR).findFirst().get();
    }

    private List<PartitionRegion> splitEqually(PartitionRegion region) {
        SplitType splitType = getBestSplitType(region.envelope());
        return splitByStrategy(region, splitType, EQUAL_STRATEGY);
    }

    private SplitType getBestSplitType(Envelope envelope) {
        double deltaLatitude = envelope.maxLatitude() - envelope.minLatitude();
        double deltaLongitude = envelope.maxLongitude() - envelope.minLongitude();
        return deltaLatitude > deltaLongitude ? SplitType.latitude : SplitType.longitude;
    }

    private List<PartitionRegion> splitByStrategy(PartitionRegion region, SplitType splitType,
                                                  SplitStrategy splitStrategy) {
        ToDoubleFunction<Point> coordinateFunction = getCoordinateFunction(splitType);
        List<Point> sortedPoints = region.points().stream().sorted(comparingDouble(coordinateFunction)).toList();
        int size = sortedPoints.size();
        for (int i = 0; i < size - 1; i++) {
            int secondRegionMinIndex = splitStrategy.getSecondRegionMinIndex(i, size);
            checkArgument(secondRegionMinIndex > 0);
            checkArgument(secondRegionMinIndex < size);
            var optionalSplitCoordinate = getSplitCoordinate(sortedPoints, secondRegionMinIndex, coordinateFunction);
            if (optionalSplitCoordinate.isPresent()) {
                return splitAt(splitType, sortedPoints, secondRegionMinIndex, region.envelope(),
                    optionalSplitCoordinate.getAsDouble());
            }
        }
        return List.of(region);
    }

    private List<PartitionRegion> splitAt(SplitType splitType, List<Point> sortedPoints, int secondRegionMinIndex,
                                          Envelope envelope, double splitCoordinate) {
        Envelope firstEnvelope;
        Envelope secondEnvelope;
        switch (splitType) {
            case latitude -> {
                firstEnvelope = new Envelope(envelope.minLongitude(), envelope.maxLongitude(), envelope.minLatitude(),
                    splitCoordinate);
                secondEnvelope = new Envelope(envelope.minLongitude(), envelope.maxLongitude(), splitCoordinate,
                    envelope.maxLatitude());
            }
            case longitude -> {
                firstEnvelope = new Envelope(envelope.minLongitude(), splitCoordinate, envelope.minLatitude(),
                    envelope.maxLatitude());
                secondEnvelope = new Envelope(splitCoordinate, envelope.maxLongitude(), envelope.minLatitude(),
                    envelope.maxLatitude());
            }
            default -> throw new IllegalArgumentException();
        }
        return List.of( //
            new PartitionRegion(firstEnvelope, sortedPoints.subList(0, secondRegionMinIndex)), //
            new PartitionRegion(secondEnvelope, sortedPoints.subList(secondRegionMinIndex, sortedPoints.size())) //
        );
    }

    private ToDoubleFunction<Point> getCoordinateFunction(SplitType splitType) {
        return switch (splitType) {
            case latitude -> Spatial::getLatitude;
            case longitude -> Spatial::getLongitude;
            default -> throw new IllegalArgumentException();
        };
    }

    private OptionalDouble getSplitCoordinate(List<Point> sortedPoints, int secondRegionMinIndex,
                                              ToDoubleFunction<Point> coordinateFunction) {
        double firstRegionMaxCoordinate = coordinateFunction.applyAsDouble(sortedPoints.get(secondRegionMinIndex - 1));
        double secondRegionMinCoordinate = coordinateFunction.applyAsDouble(sortedPoints.get(secondRegionMinIndex));
        if (Math.abs(firstRegionMaxCoordinate - secondRegionMinCoordinate) >= MIN_ALLOWED_DIFFERENCE) {
            return OptionalDouble.of(new BigDecimal((firstRegionMaxCoordinate + secondRegionMinCoordinate) / 2)
                .setScale(ROUND_DIGITS, RoundingMode.HALF_UP).doubleValue());
        } else {
            return OptionalDouble.empty();
        }
    }
}
