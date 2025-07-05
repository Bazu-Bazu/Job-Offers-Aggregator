package com.example.Job.Offers.Aggregator.dto;


public class HhVacancyDto {

    private final String id;
    private final String name;
    private final String salary;
    private final String employer;

    public HhVacancyDto(String id, String name, String salary, String employer) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.employer = employer;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSalary() {
        return salary;
    }

    public String getEmployer() {
        return employer;
    }

}
