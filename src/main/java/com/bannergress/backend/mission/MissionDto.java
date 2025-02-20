package com.bannergress.backend.mission;

import com.bannergress.backend.agent.NamedAgentDto;
import com.bannergress.backend.mission.step.MissionStepDto;
import com.bannergress.backend.utils.PojoBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import java.net.URL;
import java.time.Instant;
import java.util.List;

@JsonInclude(Include.NON_NULL)
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class MissionDto {
    @NotNull
    public String id;

    public String title;

    public URL picture;

    public List<MissionStepDto> steps;

    public String description;

    public MissionType type;

    public MissionStatus status;

    public Instant latestUpdateStatus;

    public NamedAgentDto author;

    @Min(0)
    public Long averageDurationMilliseconds;

    @Min(0)
    public Integer lengthMeters;
}
