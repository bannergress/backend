package com.bannergress.backend.user;

import com.bannergress.backend.agent.NamedAgentDto;
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

    public String verificationMessage;

    public NamedAgentDto agent;
}
