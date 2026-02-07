package reto_psp.com;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reto_psp.com.model.Game;

public class RestClient {

	private final WebClient webClient;

	public RestClient(String baseUrl) {
		this.webClient = WebClient.builder()
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(500 * 1024 * 1024)) // 16 MB
				.baseUrl(baseUrl).build();
	}

	public ResponseEntity<List<Game>> getAllGames() {
		return webClient.get().retrieve().toEntity(new ParameterizedTypeReference<List<Game>>() {
		}).block();
	}

	public ResponseEntity<Game> getGameId(long id) {
		return webClient.get().uri("{id}/", id).retrieve().toEntity(Game.class).block();
	}

	public ResponseEntity<byte[]> getGameApk(long id) {
		return webClient.get().uri("{id}/apk/", id).retrieve().toEntity(byte[].class).block();
	}

	public ResponseEntity<String> getGameHash(long id) {
		return webClient.get().uri("{id}/apk/hash/", id).retrieve().toEntity(String.class).block();
	}

	public ResponseEntity<Boolean> verifyGameApk(long id, byte[] clientApk) {
		return webClient.post().uri("{id}/apk/verify/", id).bodyValue(clientApk).retrieve().toEntity(Boolean.class)
				.block();
	}

	public ResponseEntity<Game> createGame(Game game) {
		return webClient.post().uri("create/").contentType(MediaType.APPLICATION_JSON) // Important!
				.bodyValue(game).retrieve().toEntity(Game.class).block();
	}

	public ResponseEntity<Game> modifyGame(Game modifiedGame) {
		return webClient.patch().uri("{id}/", modifiedGame.getId()).contentType(MediaType.APPLICATION_JSON) // Important!
				.bodyValue(modifiedGame).retrieve().toEntity(Game.class).block();
	}

	public ResponseEntity<Void> deleteGame(Long id) {
		return webClient.delete().uri("{id}/", id).retrieve().toBodilessEntity().block();
	}
}