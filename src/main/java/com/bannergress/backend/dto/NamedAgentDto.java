package com.bannergress.backend.dto;

import com.bannergress.backend.enums.Faction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Transports information about a named agent.
 */
@JsonInclude(Include.NON_NULL)
public class NamedAgentDto {
    public String name;

    public Faction faction;
}
