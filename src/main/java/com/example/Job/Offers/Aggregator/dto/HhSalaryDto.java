package com.example.Job.Offers.Aggregator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HhSalaryDto {

    private Integer from;
    private Integer to;
    private String currency;

    public HhSalaryDto(Integer from, Integer to, String currency) {
        this.from = from;
        this.to = to;
        this.currency = currency;
    }

    public HhSalaryDto() {}

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
