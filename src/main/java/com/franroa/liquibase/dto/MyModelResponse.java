package com.franroa.liquibase.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MyModelResponse {
    @JsonProperty("name")
    private String name;

    public MyModelResponse(String name) {
        this.name = name;
    }
}
