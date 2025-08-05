package com.joaoglmartins.beach_safety_api.external;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class UvIndexClient {

	private final WebClient webClient;

	public UvIndexClient(WebClient.Builder builder) {
		this.webClient = builder.baseUrl("https://api.open-meteo.com").build();
	}

	/**
	 * Fetches the UV index for a specific geographic location and time.
	 *
	 * @param lat     Latitude.
	 * @param lon     Longitude.
	 * @param isoTime Time in ISO format - because its the format that the external
	 *                api uses. .
	 * @return A Mono emitting the UV index as a Double at the specified time.
	 *         Returns 0.0 if the time is not found in the API response.
	 */

	public Mono<Double> getUvIndexAtTime(double lat, double lon, String isoTime) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/v1/forecast").queryParam("latitude", lat)
						.queryParam("longitude", lon).queryParam("hourly", "uv_index").queryParam("forecast_days", 1)
						.queryParam("timezone", "auto").build())
				.retrieve().bodyToMono(OpenMeteoHourlyResponse.class).map(res -> {
					List<String> times = res.time();
					List<Double> values = res.uv_index();

					for (int i = 0; i < times.size(); i++) {
						if (times.get(i).equals(isoTime)) {
							return values.get(i);
						}
					}

					return 0.0;
				});
	}

	public record OpenMeteoHourlyResponse(List<String> time, List<Double> uv_index) {
	}
}
