package com.bannergress.backend.dto;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Transports information about a single news item.
 */
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class NewsItemDto {
    public UUID uuid;

    @NotNull
    public String content;

    public Instant created;
}
