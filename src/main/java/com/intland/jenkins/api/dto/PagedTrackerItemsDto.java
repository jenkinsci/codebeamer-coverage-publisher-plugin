/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedTrackerItemsDto {
	private Integer total;
	private List<TrackerItemDto> items = new ArrayList<>();

	public Integer getTotal() {
		return this.total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public List<TrackerItemDto> getItems() {
		return this.items;
	}

	public void setItems(List<TrackerItemDto> items) {
		this.items = items;
	}
}
