package com.bannergress.backend.dto;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.validation.constraints.NotNull;

import java.net.URL;
import java.util.List;

@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class MissionDto {
    @NotNull
    public String id;

    public String title;

    public URL picture;

    public List<MissionStepDto> steps;
}
