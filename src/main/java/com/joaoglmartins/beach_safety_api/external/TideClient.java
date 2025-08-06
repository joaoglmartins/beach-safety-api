package com.joaoglmartins.beach_safety_api.external;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class TideClient {

	private final WebClient webClient;

	public TideClient(@Value("${stormglass.api.key}") String apiKey) {
		this.webClient = WebClient.builder().baseUrl("https://api.stormglass.io").defaultHeader("Authorization", apiKey)
				.build();
	}

	public Mono<Double> getTideHeightAtTime(double lat, double lon, String isoTime) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/v2/tide/sea-level/point").queryParam("lat", lat)
						.queryParam("lng", lon).queryParam("start", isoTime).queryParam("end", isoTime).build())
				.retrieve().bodyToMono(StormGlassTideResponse.class).map(res -> {
					if (res.hours() != null && !res.hours().isEmpty()) {
						return res.hours().get(0).seaLevel();
					}
					return 0.0;
				});
	}

	public record StormGlassTideResponse(List<TideHour> hours) {
	}

	public record TideHour(String time, Double seaLevel) {
	}
}
