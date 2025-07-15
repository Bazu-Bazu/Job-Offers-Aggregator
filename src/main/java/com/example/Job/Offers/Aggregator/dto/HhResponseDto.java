package com.example.Job.Offers.Aggregator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HhResponseDto {

    private List<HhVacancyDto> items;

    public HhResponseDto(List<HhVacancyDto> items) {
        this.items = items;
    }

    public HhResponseDto() {}

    public List<HhVacancyDto> getItems() {
        return items;
    }

    public void setItems(List<HhVacancyDto> items) {
        this.items = items;
    }
}
