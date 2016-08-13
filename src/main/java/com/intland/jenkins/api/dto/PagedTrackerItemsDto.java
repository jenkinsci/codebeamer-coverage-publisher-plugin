/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PagedTrackerItemsDto {
	private Integer total;
	private TrackerItemDto[] items;

	public Integer getTotal() {
		return this.total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public TrackerItemDto[] getItems() {
		return this.items;
	}

	public void setItems(TrackerItemDto[] items) {
		this.items = items;
	}
}
