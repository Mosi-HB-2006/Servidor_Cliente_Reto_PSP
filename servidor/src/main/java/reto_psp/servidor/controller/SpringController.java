package reto_psp.servidor.controller;

import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reto_psp.servidor.model.Game;
import reto_psp.servidor.service.SpringService;

@RestController
@RequestMapping("/api/games/")
public class SpringController {
	private final SpringService springService;

	public SpringController(SpringService springService) {
		this.springService = springService;
	}

	@GetMapping
	public ResponseEntity<List<Game>> getAllGames() {
		return ResponseEntity.ok().body(springService.getAllGames());
	}

	@GetMapping(value = "{id}/")
	public ResponseEntity<Game> getGameId(@PathVariable Long id) {
		if (id == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		Game game = springService.getGameId(id);

		if (game != null) {
			return ResponseEntity.ok(game);
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@GetMapping(value = "{id}/apk/")
	public ResponseEntity<byte[]> getGameApkId(@PathVariable Long id) {
		if (id == null) {
			return ResponseEntity.badRequest().build();
		}

		Game game = springService.getGameId(id);

		if (game == null) {
			return ResponseEntity.notFound().build();
		}

		byte[] bytes = springService.getGameFile(game.getApkPath());

		if (bytes == null) {
			return ResponseEntity.notFound().build();
		}

		String filename = Paths.get(game.getApkPath()).getFileName().toString();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());

		return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
	}

	@GetMapping(value = "{id}/apk/hash/")
	public ResponseEntity<String> getGameApkHashId(@PathVariable Long id) {
		if (id == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		Game game = springService.getGameId(id);

		if (game == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		String fileHash = springService.hashGame(game.getApkPath());

		return ResponseEntity.ok(fileHash);
	}

	@PostMapping(value = "create/")
	public ResponseEntity<Game> createGame(@RequestBody Game newGame) {
		if (newGame == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		if (springService.getGameId(newGame.getId()) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

		Game game = springService.createGame(newGame);
		return ResponseEntity.ok(game);
	}

	@PostMapping("{id}/apk/verify/")
	public ResponseEntity<Boolean> verifyGameApk(@PathVariable Long id, @RequestBody byte[] clientApk) {

		Game game = springService.getGameId(id);
		if (game == null) {
			return ResponseEntity.notFound().build();
		}

		String serverHash = springService.hashFile(game.getApkPath());
		if (serverHash == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		String clientHash = springService.hashBytes(clientApk);
		if (clientHash == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		boolean valid = serverHash.equalsIgnoreCase(clientHash);
		return ResponseEntity.ok(valid);
	}

	@PatchMapping(value = "{id}/")
	public ResponseEntity<Game> modifyGame(@PathVariable Long id, @RequestBody Game modifiedGame) {
		if (id == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		Game game = springService.modifyGame(modifiedGame);

		if (game == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		return ResponseEntity.ok(game);
	}

	@DeleteMapping(value = "delete/{id}/")
	public ResponseEntity<Game> deleteGame(@PathVariable Long id) {
		if (id == null) {
			return ResponseEntity.badRequest().build();
		}

		if (springService.deleteGameId(id)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
