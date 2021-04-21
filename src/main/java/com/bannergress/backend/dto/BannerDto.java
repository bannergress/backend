package com.bannergress.backend.dto;

import com.bannergress.backend.utils.PojoBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class BannerDto {
    /**
     * Internal UUID without further meaning.
     */
    public UUID uuid;

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
     * Number of missions.
     */
    public int numberOfMissions;

    /**
     * Map between the zero-based mission position and the mission. The mission
     * position must be less than {@link #numberOfMissions}. The map may be sparse,
     * i.e. not every position is necessarily mapped to a mission.
     */
    public Map<@NotNull @Min(0) Integer, @NotNull MissionDto> missions;

    /**
     * Latitude of the start portal of the first mission.
     */
    public Double startLatitude;

    /**
     * Longitude of the start portal of the first mission.
     */
    public Double startLongitude;

    /**
     * Length in meters.
     */
    public Double lengthMeters;

    /**
     * Address.
     */
    public String formattedAddress;

    /**
     * path to banner's picture
     */
    public String picture;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BannerDto bannerDto = (BannerDto) o;
        return Objects.equals(uuid, bannerDto.uuid) && numberOfMissions == bannerDto.numberOfMissions
            && Objects.equals(title, bannerDto.title) && Objects.equals(description, bannerDto.description)
            && Objects.equals(missions, bannerDto.missions) && Objects.equals(startLatitude, bannerDto.startLatitude)
            && Objects.equals(startLongitude, bannerDto.startLongitude)
            && Objects.equals(lengthMeters, bannerDto.lengthMeters)
            && Objects.equals(formattedAddress, bannerDto.formattedAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, title, description, numberOfMissions, missions, startLatitude, startLongitude,
            lengthMeters, formattedAddress);
    }
}
