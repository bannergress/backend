package com.bannergress.backend.dto;

import java.net.URL;
import java.util.List;

import javax.validation.constraints.NotNull;

public class MissionDto {
	@NotNull
	public String id;

	public String title;

	public URL picture;

	public List<MissionStepDto> steps;
}
