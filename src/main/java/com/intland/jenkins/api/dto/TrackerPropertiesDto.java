/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TrackerPropertiesDto {
	private TrackerTypeDto type;

	public TrackerTypeDto getType() {
		return this.type;
	}

	public void setType(TrackerTypeDto type) {
		this.type = type;
	}
}