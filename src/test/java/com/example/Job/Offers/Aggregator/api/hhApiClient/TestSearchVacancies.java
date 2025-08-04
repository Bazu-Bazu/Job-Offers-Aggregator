package com.example.Job.Offers.Aggregator.api.hhApiClient;

import com.example.Job.Offers.Aggregator.config.HhConfig;
import com.example.Job.Offers.Aggregator.service.VacancyService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestSearchVacancies {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HhConfig hhConfig;

    @InjectMocks
    private VacancyService vacancyService;



}
