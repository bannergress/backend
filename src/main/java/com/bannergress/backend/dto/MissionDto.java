package com.bannergress.backend.dto;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.List;

public class MissionDto {
    @NotNull
    public String id;

    public String title;

    public URL picture;

    public List<MissionStepDto> steps;
}
