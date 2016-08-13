/*
 * Copyright (c) 2016 Intland Software (support@intland.com)
 */
package com.intland.jenkins.api.dto;

public class TypeDto {
    private String uri;
    private String name;

    public Integer getTypeId() {
        Integer result = null;

        if (uri != null) {
            int index = uri.lastIndexOf("/");
            result = Integer.valueOf(uri.substring(index + 1));
        }

        return result;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
