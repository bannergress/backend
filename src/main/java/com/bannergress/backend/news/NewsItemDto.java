package com.bannergress.backend.news;

import com.bannergress.backend.utils.PojoBuilder;
import jakarta.validation.constraints.NotNull;
import net.karneim.pojobuilder.GeneratePojoBuilder;

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
