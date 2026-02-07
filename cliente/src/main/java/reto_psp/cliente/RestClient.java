package reto_psp.cliente;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reto_psp.cliente.model.Game;

public class RestClient {

	private final WebClient webClient;

	public RestClient(String baseUrl) {
		this.webClient = WebClient.builder()
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10000 * 1024 * 1024)) // 10 GB
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
	
	public ResponseEntity<byte[]> getGameImage(long id) {
		return webClient.get().uri("{id}/image/", id).retrieve().toEntity(byte[].class).block();
	}

	public ResponseEntity<String> getGameHash(long id) {
		return webClient.get().uri("{id}/apk/hash/", id).retrieve().toEntity(String.class).block();
	}

	public ResponseEntity<Boolean> verifyGameApk(long id, byte[] clientApk) {
		return webClient.post().uri("{id}/apk/verify/", id).bodyValue(clientApk).retrieve().toEntity(Boolean.class)
				.block();
	}

	public ResponseEntity<Game> createGame(Game game, File apk, File image) throws IOException {
	    MultipartBodyBuilder builder = new MultipartBodyBuilder();
	    builder.part("game", game, MediaType.APPLICATION_JSON);
	    builder.part("apk", new FileSystemResource(apk));
	    builder.part("image", new FileSystemResource(image));

	    return webClient.post()
	            .uri("create/")
	            .contentType(MediaType.MULTIPART_FORM_DATA)
	            .body(BodyInserters.fromMultipartData(builder.build()))
	            .retrieve()
	            .toEntity(Game.class)
	            .block();
	}
	
	public ResponseEntity<Game> createGameApk(Long id, byte[] apk) {
		return webClient.post().uri("{id}/apk/", id).contentType(MediaType.APPLICATION_OCTET_STREAM)
				.bodyValue(apk).retrieve().toEntity(Game.class).block();
	}
	
	public ResponseEntity<Game> createGameImage(Long id, byte[] image) {
		return webClient.post().uri("{id}/image/", id).contentType(MediaType.APPLICATION_OCTET_STREAM)
				.bodyValue(image).retrieve().toEntity(Game.class).block();
	}

	public ResponseEntity<Game> modifyGame(Game modifiedGame) {
		return webClient.patch().uri("{id}/", modifiedGame.getId()).contentType(MediaType.APPLICATION_JSON)
				.bodyValue(modifiedGame).retrieve().toEntity(Game.class).block();
	}

	public ResponseEntity<Void> deleteGame(Long id) {
		return webClient.delete().uri("delete/{id}/", id).retrieve().toBodilessEntity().block();
	}
}