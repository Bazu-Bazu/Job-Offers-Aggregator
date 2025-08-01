package com.example.Job.Offers.Aggregator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HhEmployerDto {

    private String name;

    public HhEmployerDto(String name) {
        this.name = name;
    }

    public HhEmployerDto() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
