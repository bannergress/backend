package com.bannergress.backend.dto;

import com.bannergress.backend.enums.Objective;
import com.bannergress.backend.utils.PojoBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.karneim.pojobuilder.GeneratePojoBuilder;

@JsonInclude(Include.NON_NULL)
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class MissionStepDto {
    public PoiDto poi;

    public Objective objective;
}
