package com.example.Job.Offers.Aggregator.dto;

import java.util.List;


public class HhResponseDto {

    private final List<HhVacancyDto> items;

    public HhResponseDto(List<HhVacancyDto> items) {
        this.items = items;
    }

    public List<HhVacancyDto> getItems() {
        return items;
    }

}
