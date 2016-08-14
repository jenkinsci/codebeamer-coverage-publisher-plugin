/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TrackerItemDto {

	private Integer id;
	private String name;
	private String uri;
	private String status;
	private TrackerItemDto tracker;
	private TrackerItemDto parent;
	private TrackerItemDto[] verifies;
	private TrackerItemDto[] children;
	private List<Object[]> testCases;

	public TrackerItemDto() {
	}

	public TrackerItemDto(String uri) {
		this.setUri(uri);
	}

	public TrackerItemDto(String uri, TrackerItemDto[] verifies) {
		this.setUri(uri);
		this.setVerifies(verifies);
	}

	public Integer getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUri(String uri) {
		this.uri = uri;
		this.id = this.parseIdFromUri(uri);
	}

	public String getUri() {
		return this.uri;
	}

	public TrackerItemDto[] getChildren() {
		return this.children;
	}

	public void setChildren(TrackerItemDto[] children) {
		this.children = children;
	}

	public TrackerItemDto getParent() {
		return this.parent;
	}

	public void setParent(TrackerItemDto parent) {
		this.parent = parent;
	}

	public TrackerItemDto[] getVerifies() {
		return this.verifies;
	}

	public void setVerifies(TrackerItemDto[] verifies) {
		this.verifies = verifies;
	}

	public List<Object[]> getTestCases() {
		return this.testCases;
	}

	public void setTestCases(List<Object[]> testCases) {
		this.testCases = testCases;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TrackerItemDto getTracker() {
		return this.tracker;
	}

	public void setTracker(TrackerItemDto tracker) {
		this.tracker = tracker;
	}

	private Integer parseIdFromUri(String name) {
		int lastIndex = name.lastIndexOf("/");
		Integer result = null;

		if (lastIndex > -1) {
			result = Integer.parseInt(name.substring(lastIndex + 1));
		}

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		TrackerItemDto other = (TrackerItemDto) obj;
		if (!Arrays.equals(this.children, other.children)) {
			return false;
		}
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!this.parent.equals(other.parent)) {
			return false;
		}
		if (this.testCases == null) {
			if (other.testCases != null) {
				return false;
			}
		} else if (!this.testCases.equals(other.testCases)) {
			return false;
		}
		if (this.tracker == null) {
			if (other.tracker != null) {
				return false;
			}
		} else if (!this.tracker.equals(other.tracker)) {
			return false;
		}
		if (this.uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!this.uri.equals(other.uri)) {
			return false;
		}
		if (!Arrays.equals(this.verifies, other.verifies)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(this.children);
		result = (prime * result) + ((this.id == null) ? 0 : this.id.hashCode());
		result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
		result = (prime * result) + ((this.parent == null) ? 0 : this.parent.hashCode());
		result = (prime * result) + ((this.testCases == null) ? 0 : this.testCases.hashCode());
		result = (prime * result) + ((this.tracker == null) ? 0 : this.tracker.hashCode());
		result = (prime * result) + ((this.uri == null) ? 0 : this.uri.hashCode());
		result = (prime * result) + Arrays.hashCode(this.verifies);
		return result;
	}

	@Override
	public String toString() {
		return "TrackerItemDto [id=" + this.id + ", name=" + this.name + ", uri=" + this.uri + ", tracker="
				+ this.tracker + ", parent=" + this.parent + ", verifies=" + Arrays.toString(this.verifies)
				+ ", children=" + Arrays.toString(this.children) + ", testCases=" + this.testCases + "]";
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
