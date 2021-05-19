package com.bannergress.backend.dto;

import com.bannergress.backend.enums.MissionType;
import com.bannergress.backend.utils.PojoBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.validation.constraints.NotNull;

import java.net.URL;
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

    public Boolean online;

    public NamedAgentDto author;

    public Long averageDurationMilliseconds;

    public Integer lengthMeters;
}
