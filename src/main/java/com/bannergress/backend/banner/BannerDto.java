package com.bannergress.backend.banner;

import com.bannergress.backend.utils.PojoBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class BannerDto {
    /**
     * Slug (ID which is suitable for use in URLs).
     */
    public String id;

    /**
     * Title.
     */
    @NotEmpty
    public String title;

    /**
     * Description.
     */
    public String description;

    /**
     * Width.
     */
    @Min(1)
    @Max(6)
    @NotNull
    public Integer width;

    /**
     * Number of missions.
     */
    @Min(1)
    @Max(3000)
    public Integer numberOfMissions;

    /**
     * Number of submitted missions.
     */
    @Min(0)
    @Max(3000)
    public Integer numberOfSubmittedMissions;

    /**
     * Number of disabled missions.
     */
    @Min(0)
    @Max(3000)
    public Integer numberOfDisabledMissions;

    /**
     * Map between the zero-based mission position and the mission. The mission
     * position must be less than {@link #numberOfMissions}. The map may be sparse,
     * i.e. not every position is necessarily mapped to a mission.
     */
    public Map<@NotNull @Min(0) @Max(2999) Integer, com.bannergress.backend.mission.MissionDto> missions;

    /**
     * Latitude of the start portal of the first mission.
     */
    @Min(-90)
    @Max(90)
    public Double startLatitude;

    /**
     * Longitude of the start portal of the first mission.
     */
    @Min(-180)
    @Max(180)
    public Double startLongitude;

    /**
     * Slug of the start place of the banner.
     */
    public String startPlaceId;

    /**
     * Length in meters.
     */
    @Min(0)
    public Integer lengthMeters;

    /**
     * Address.
     */
    public String formattedAddress;

    /**
     * path to banner's picture
     */
    public String picture;

    /**
     * Banner type (sequential or any order).
     */
    @NotNull
    public BannerType type;

    /**
     * Type of list the banner is on.
     */
    public BannerListType listType;

    /**
     * Flag that indicates whether the user owns the banner.
     */
    @Hidden
    public Boolean owner;

    /**
     * Warning text.
     */
    public String warning;

    /**
     * Planned date when the banner will be put offline.
     */
    public LocalDate plannedOfflineDate;

    /**
     * Start date for the corresponding event (inclusive).
     */
    public LocalDate eventStartDate;

    /**
     * End date for the corresponding event (inclusive).
     */
    public LocalDate eventEndDate;

    /**
     * Banner UUID.
     */
    public UUID uuid;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BannerDto bannerDto = (BannerDto) o;
        return Objects.equals(id, bannerDto.id) && numberOfMissions == bannerDto.numberOfMissions
            && Objects.equals(title, bannerDto.title) && Objects.equals(description, bannerDto.description)
            && Objects.equals(missions, bannerDto.missions) && Objects.equals(startLatitude, bannerDto.startLatitude)
            && Objects.equals(startLongitude, bannerDto.startLongitude)
            && Objects.equals(lengthMeters, bannerDto.lengthMeters)
            && Objects.equals(formattedAddress, bannerDto.formattedAddress) && type == bannerDto.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, numberOfMissions, missions, startLatitude, startLongitude,
            lengthMeters, formattedAddress, type);
    }
}
