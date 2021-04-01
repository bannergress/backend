package com.bannergress.backend.dto;

import com.bannergress.backend.enums.Objective;
import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;

@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class MissionStepDto {
    public PoiDto poi;

    public Objective objective;
}
