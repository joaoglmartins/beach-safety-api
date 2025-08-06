package com.joaoglmartins.beach_safety_api.model;

public record BeachCondition(
    double uvIndex,
    double tideHeight,
    int starRating, // 1 to 5
    String safetyMessage
) {}
