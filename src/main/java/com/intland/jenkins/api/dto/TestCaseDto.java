/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class TestCaseDto {
    private String uri;
    private String name;
    private String tracker;
    private String status;
    private Long spentMillis;
    private ReferenceDto parent;

    public TestCaseDto(String name, String tracker, Integer parentId) {
        this.name = name;
        this.tracker = tracker;

        if (parentId != null) {
            this.parent = new ReferenceDto("/item/" + parentId.toString());
        }
    }

    public TestCaseDto(Integer id, String status) {
        this.uri = "/item/" + id;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getTracker() {
        return tracker;
    }

    public String getUri() {
        return uri;
    }

    public String getStatus() {
        return status;
    }

    public ReferenceDto getParent() {
        return parent;
    }

    public Long getSpentMillis() {
        return spentMillis;
    }

    public void setSpentMillis(Long spentMillis) {
        this.spentMillis = spentMillis;
    }
}
