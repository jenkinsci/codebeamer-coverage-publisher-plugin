/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */

package com.intland.jenkins.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TrackerSchemaDto {
	private TrackerPropertiesDto properties;

	public TrackerPropertiesDto getProperties() {
		return this.properties;
	}

	public void setProperties(TrackerPropertiesDto properties) {
		this.properties = properties;
	}

	public boolean doesTypeContain(String type) {
		boolean result = false;

		if ((this.properties != null) && (this.properties.getType() != null)
				&& (this.properties.getType().getSettings() != null)) {
			TrackerTypeSettingDto[] settings = this.properties.getType().getSettings();
			for (TrackerTypeSettingDto setting : settings) {
				if (setting.getName().equals(type)) {
					result = true;
					break;
				}
			}
		}

		return result;
	}
}