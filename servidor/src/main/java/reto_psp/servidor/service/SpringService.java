package reto_psp.servidor.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import reto_psp.servidor.model.Game;

@Service
public class SpringService {
	private List<Game> games = new ArrayList<>();
	public static final Path ROOT = Paths.get("../shared-files/server-files");
	public final Path APK_DIR = Paths.get("../shared-files/server-files/apks");
	public final Path IMAGE_DIR = Paths.get("../shared-files/server-files/images");

	@PostConstruct
	public void init() throws IOException {
		Files.createDirectories(APK_DIR);
		Files.createDirectories(IMAGE_DIR);
		games=generateList();
	}

	public List<Game> getAllGames() {
		return games;
	}

	public Game getGameId(Long id) {
		for (Game game : games) {
			if (game.getId() == id) {
				return game;
			}
		}

		return null;
	}

	public byte[] getGameFile(String relativePath) {
		try {
			Path filePath = ROOT.resolve(relativePath);

			if (!Files.exists(filePath)) {
				return null;
			}

			return Files.readAllBytes(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean deleteGameId(Long id) {
		boolean removed = games.removeIf(game -> game.getId().equals(id));

		if (removed) {
			writeList(games);
		}

		return removed;
	}

	public Game createGame(Game newGame) {
		if (newGame.getId() == null) {
			newGame.setId(Long.valueOf(games.size() + 1));
		}

		games.add(newGame);
		writeList(games);
		return newGame;
	}

	public Game modifyGame(Game modifiedGame) {
		for (Game game : games) {
			if (game.getId() == modifiedGame.getId()) {
				games.remove(games.indexOf(game));
				games.add(modifiedGame);

				writeList(games);

				return modifiedGame;
			}
		}

		return null;
	}

	private List<Game> generateList() {
		ObjectMapper mapper = new ObjectMapper();
		List<Game> games = new ArrayList<>();

		try (InputStream is = getClass().getClassLoader().getResourceAsStream("data.json")) {

			if (is == null) {
				throw new FileNotFoundException("data.json not found in resources");
			}

			games = mapper.readValue(is, new TypeReference<List<Game>>() {
			});
		} catch (IOException e) {
			System.err.println("File not found or empty");
			e.printStackTrace();
			return null;
		}

		return games;
	}

	private void writeList(List<Game> games) {
		ObjectMapper mapper = new ObjectMapper();
		Path filePath = Paths.get("src/main/resources/data.json");

		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), games);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String hashFile(String relativePath) {
	    Path filePath = ROOT.resolve(relativePath);

	    if (!Files.exists(filePath)) {
	        System.err.println("File not found: " + filePath);
	        return null;
	    }

	    try (InputStream is = Files.newInputStream(filePath)) {
	        MessageDigest sha = MessageDigest.getInstance("SHA-256");

	        byte[] buffer = new byte[8192];
	        int bytesRead;

	        while ((bytesRead = is.read(buffer)) != -1) {
	            sha.update(buffer, 0, bytesRead);
	        }

	        byte[] hashBytes = sha.digest();

	        StringBuilder hexStringBuilder = new StringBuilder();
	        for (byte b : hashBytes) {
	            hexStringBuilder.append(String.format("%02X", b));
	        }

	        return hexStringBuilder.toString();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}


	public String hashBytes(byte[] data) {
		if (data == null) {
			return null;
		}

		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-256");

			sha.update(data);
			byte[] hashBytes = sha.digest();

			StringBuilder hexStringBuilder = new StringBuilder();
			for (byte b : hashBytes) {
				hexStringBuilder.append(String.format("%02X", b));
			}

			return hexStringBuilder.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

}
