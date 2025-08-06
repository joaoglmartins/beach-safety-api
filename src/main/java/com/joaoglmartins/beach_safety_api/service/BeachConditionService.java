package com.joaoglmartins.beach_safety_api.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;

import com.joaoglmartins.beach_safety_api.external.TideClient;
import com.joaoglmartins.beach_safety_api.external.UvIndexClient;
import com.joaoglmartins.beach_safety_api.model.BeachCondition;
import com.joaoglmartins.beach_safety_api.util.Json;

import reactor.core.publisher.Mono;

@Service
public class BeachConditionService {

	private final UvIndexClient uvClient;
	private final TideClient tideClient;
	private final ReactiveValueOperations<String, String> redisOps;

	@Autowired
	public BeachConditionService(UvIndexClient uvClient, TideClient tideClient,
			ReactiveStringRedisTemplate redisTemplate) {
		this.uvClient = uvClient;
		this.tideClient = tideClient;
		this.redisOps = redisTemplate.opsForValue();
	}

	public Mono<BeachCondition> getCondition(double lat, double lon, String isoTime) {
		String key = "beach:" + lat + ":" + lon + ":" + isoTime;

		return redisOps.get(key).flatMap(cached -> Mono.just(Json.decode(cached, BeachCondition.class)))
				.switchIfEmpty(Mono.zip(uvClient.getUvIndexAtTime(lat, lon, isoTime),
						tideClient.getTideHeightAtTime(lat, lon, isoTime)).map(tuple -> {
							double uv = tuple.getT1();
							double tide = tuple.getT2();
							int stars = rateBeach(uv, tide);
							String message = explainRating(uv, tide, stars);

							return new BeachCondition(uv, tide, stars, message);
						}).flatMap(condition -> redisOps.set(key, Json.encode(condition), Duration.ofHours(2))
								.thenReturn(condition)));
	}

	private int rateBeach(double uv, double tide) {
		if (uv > 8 || tide > 2)
			return 1;
		if (uv > 6 || tide > 1.5)
			return 2;
		if (uv > 4 || tide > 1.2)
			return 3;
		if (uv > 2 || tide > 1.0)
			return 4;
		return 5;
	}

	private String explainRating(double uv, double tide, int stars) {
		if (stars == 5)
			return "Perfect for kids and sunbathing.";
		if (stars == 4)
			return "Very good, but use sunscreen.";
		if (stars == 3)
			return "Moderate UV or light waves — be cautious.";
		if (stars == 2)
			return "Possibly too harsh for kids.";
		return "Unsafe conditions for most people.";
	}
}
