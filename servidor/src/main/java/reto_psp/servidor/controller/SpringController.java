package reto_psp.servidor.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

	@GetMapping(value = "{id}/image/")
	public ResponseEntity<byte[]> getGameImage(@PathVariable Long id) {
		if (id == null) {
			return ResponseEntity.badRequest().build();
		}

		Game game = springService.getGameId(id);

		if (game == null) {
			return ResponseEntity.notFound().build();
		}

		byte[] bytes = springService.getGameFile(game.getImagePath());

		if (bytes == null) {
			return ResponseEntity.notFound().build();
		}

		String filename = Paths.get(game.getImagePath()).getFileName().toString();
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

		String fileHash = springService.hashFile(game.getApkPath());

		return ResponseEntity.ok(fileHash);
	}

	@PostMapping("create/")
	public ResponseEntity<Game> createGame(@RequestPart("game") Game newGame,
			@RequestPart("apk") MultipartFile apkFile, @RequestPart("image") MultipartFile imageFile) {
		if (newGame == null || apkFile == null || imageFile == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
		
		Path apkPath = springService.APK_DIR.resolve(timestamp + "-" + newGame.getTitle() + ".apk");
		try {
			Files.write(apkPath, apkFile.getBytes());
		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		}
		newGame.setApkPath("apks/" + timestamp + "-" + newGame.getTitle() + ".apk");

		Path imagePath = springService.IMAGE_DIR.resolve(timestamp + "-" + newGame.getTitle() + ".png");
		try {
			Files.write(imagePath, imageFile.getBytes());
		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		}
		newGame.setImagePath("images/" + timestamp + "-" + newGame.getTitle() + ".png");

		Game createdGame = springService.createGame(newGame);

		return ResponseEntity.ok(createdGame);
	}

	@PostMapping("{id}/apk/")
	public ResponseEntity<Game> uploadApk(@PathVariable Long id, @RequestBody byte[] apkBytes) {
		Game game = springService.getGameId(id);

		if (game == null) {
			return ResponseEntity.notFound().build();
		}

		Path apkPath = springService.APK_DIR.resolve(game.getId() + "-" + game.getTitle() + ".apk");

		try {
			Files.write(apkPath, apkBytes);
		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		}

		game.setApkPath("apks/" + game.getTitle() + ".apk");

		springService.modifyGame(game);

		return ResponseEntity.ok(game);
	}

	@PostMapping("{id}/image/")
	public ResponseEntity<Game> uploadImage(@PathVariable Long id, @RequestBody byte[] imageBytes) {
		Game game = springService.getGameId(id);

		if (game == null) {
			return ResponseEntity.notFound().build();
		}

		Path imagePath = springService.IMAGE_DIR.resolve(game.getId() + "-" + game.getTitle() + ".png");

		try {
			Files.write(imagePath, imageBytes);
		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		}

		game.setImagePath("images/" + game.getTitle() + ".png");

		springService.modifyGame(game);

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
