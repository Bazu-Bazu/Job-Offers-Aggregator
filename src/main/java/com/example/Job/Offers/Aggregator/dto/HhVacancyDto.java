package com.example.Job.Offers.Aggregator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HhVacancyDto {

    @JsonProperty("alternate_url")
    private String url;
    private String name;
    private HhSalaryDto salary;
    private HhEmployerDto employer;

    public HhVacancyDto(String url, String name, HhSalaryDto salary, HhEmployerDto employer) {
        this.url = url;
        this.name = name;
        this.salary = salary;
        this.employer = employer;
    }

    public HhVacancyDto() {}

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public HhSalaryDto getSalary() {
        return salary;
    }

    public HhEmployerDto getEmployer() {
        return employer;
    }

}
