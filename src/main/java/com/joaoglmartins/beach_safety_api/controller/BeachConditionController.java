package com.joaoglmartins.beach_safety_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.joaoglmartins.beach_safety_api.model.BeachCondition;
import com.joaoglmartins.beach_safety_api.service.BeachConditionService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/beach")
public class BeachConditionController {

    private final BeachConditionService beachConditionService;

    @Autowired
    public BeachConditionController(BeachConditionService beachConditionService) {
        this.beachConditionService = beachConditionService;
    }

    @GetMapping("/conditions")
    public Mono<BeachCondition> getBeachConditions(
        @RequestParam double lat,
        @RequestParam double lon,
        @RequestParam String time // ISO format like "2025-08-06T15:00:00Z"
    ) {
        return beachConditionService.getCondition(lat, lon, time);
    }
}
