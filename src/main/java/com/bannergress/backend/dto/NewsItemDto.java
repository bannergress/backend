package com.bannergress.backend.dto;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Transports information about a single news item.
 */
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class NewsItemDto {
    public long id;

    @NotNull
    public String content;

    public Instant created;
}
