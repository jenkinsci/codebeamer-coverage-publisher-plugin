/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */

package com.intland.jenkins.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TrackerTypeDto {
	private String title;
	@JsonProperty(value = "enum")
	private List<TrackerTypeSettingDto> settings = new ArrayList<>();

	public List<TrackerTypeSettingDto> getSettings() {
		return this.settings;
	}

	public void setSettings(List<TrackerTypeSettingDto> settings) {
		this.settings = settings;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}