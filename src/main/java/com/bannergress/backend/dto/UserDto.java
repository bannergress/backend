package com.bannergress.backend.dto;

import com.bannergress.backend.utils.PojoBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import java.util.UUID;

@JsonInclude(Include.NON_NULL)
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class UserDto {
    public String verificationAgent;

    public UUID verificationToken;

    public String agent;
}
