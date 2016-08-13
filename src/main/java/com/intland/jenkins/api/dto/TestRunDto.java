/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class TestRunDto {
    private String name;
    private String tracker;
    private String result;
    private String description;
    private String descFormat;
    private String type;
    private ReferenceDto submitter;
    private ReferenceDto parent;
    private ReferenceDto testConfiguration;
    private ReferenceDto testSet;
    private List<Object[]>  testCases;

    public TestRunDto(){}

    public TestRunDto(String name, String tracker, Integer parentId) {
        this.name = name;
        this.tracker = tracker;

        if (parentId != null) {
            this.parent = new ReferenceDto("/item/" + parentId.toString());
        }
    }

    public TestRunDto(String name, Integer parentId, Integer testRunTrackerId, Collection<Integer> testCaseIds, Integer testConfigurationId, String result) {
        this.name = name;
        this.tracker = String.format("/tracker/" + testRunTrackerId);
        this.result = result;
        this.testConfiguration = new ReferenceDto("/item/" + testConfigurationId);
        this.testCases = getTestCaseRow(testCaseIds);

        if (parentId != null) {
            this.parent = new ReferenceDto("/item/" + parentId);
        }

        this.submitter = new ReferenceDto("/user/self");
    }

    private List<Object[]> getTestCaseRow(Collection<Integer> testCaseIds) {
        List<Object[]> results = new ArrayList<>();
        for (Integer testCaseId : testCaseIds) {
            results.add(new Object[] {new ReferenceDto("/item/" + testCaseId), Boolean.TRUE, Boolean.TRUE, ""});
        }
        return results;
    }

    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescFormat() {
        return descFormat;
    }

    public void setDescFormat(String descFormat) {
        this.descFormat = descFormat;
    }

    public ReferenceDto getTestSet() {
        return testSet;
    }

    public void setTestSet(Integer testSetId) {
        this.testSet = new ReferenceDto("/item/" + testSetId);;
    }

    public String getTracker() {
        return tracker;
    }

    public String getResult() {
        return result;
    }

    public ReferenceDto getParent() {
        return parent;
    }

    public ReferenceDto getTestConfiguration() {
        return testConfiguration;
    }

    public List<Object[]> getTestCases() {
        return testCases;
    }

    public ReferenceDto getSubmitter() {
        return submitter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
