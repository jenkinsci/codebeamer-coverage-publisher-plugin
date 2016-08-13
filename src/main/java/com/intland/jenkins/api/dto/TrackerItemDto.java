/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class TrackerItemDto {
    Integer id;
    String name;
    String uri;
    TrackerItemDto tracker;
    TrackerItemDto parent;
    TrackerItemDto[] verifies;
    TrackerItemDto[] children;
    List<Object[]> testCases;

    public TrackerItemDto() {
    }

    public TrackerItemDto(String uri) {
        setUri(uri);
    }

    public TrackerItemDto(String uri, TrackerItemDto[] verifies) {
        setUri(uri);
        setVerifies(verifies);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUri(String uri) {
        this.uri = uri;
        this.id = parseIdFromUri(uri);
    }

    public String getUri() {
        return uri;
    }

    public TrackerItemDto[] getChildren() {
        return children;
    }

    public void setChildren(TrackerItemDto[] children) {
        this.children = children;
    }

    public TrackerItemDto getParent() {
        return parent;
    }

    public void setParent(TrackerItemDto parent) {
        this.parent = parent;
    }

    public TrackerItemDto[] getVerifies() {
        return verifies;
    }

    public void setVerifies(TrackerItemDto[] verifies) {
        this.verifies = verifies;
    }

    public List<Object[]> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<Object[]> testCases) {
        this.testCases = testCases;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TrackerItemDto getTracker() {
        return tracker;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackerItemDto that = (TrackerItemDto) o;

        if (!id.equals(that.id)) return false;
        if (!name.equals(that.name)) return false;
        return uri.equals(that.uri);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + uri.hashCode();
        return result;
    }
}
