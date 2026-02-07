package reto_psp.servidor.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import reto_psp.servidor.model.Game;

@Service
public class SpringService {
	public List<Game> getAllGames() {
		List<Game> games = generateList();
		return games;
	}

	public Game getGameId(Long id) {
		List<Game> games = generateList();

		for (Game game : games) {
			if (game.getId() == id) {
				return game;
			}
		}

		return null;
	}

	public byte[] getGameFile(String resourcePath) {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {

			if (is == null) {
				throw new FileNotFoundException("Resource not found: " + resourcePath);
			}

			return is.readAllBytes();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String hashGame(String filePath) {
		if (filePath != null) {
			String fileHash = hashFile(filePath);

			return fileHash;
		}

		return null;
	}

	public boolean deleteGameId(Long id) {
		List<Game> games = generateList();
		boolean removed = games.removeIf(game -> game.getId().equals(id));

		if (removed) {
			writeList(games);
		}

		return removed;
	}

	public Game createGame(Game newGame) {
		List<Game> games = generateList();
		games.add(newGame);
		writeList(games);
		return newGame;
	}

	public Game modifyGame(Game modifiedGame) {
		List<Game> games = generateList();

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

	public String hashText(String text) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] hashBytes = md5.digest(text.getBytes());

			// Convert the byte array to a hexadecimal representation
			StringBuilder hexStringBuilder = new StringBuilder();
			for (byte b : hashBytes) {
				String hex = String.format("%02X", b);
				hexStringBuilder.append(hex);
			}

			return hexStringBuilder.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String hashFile(String resourcePath) {
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {

			if (is == null) {
				throw new FileNotFoundException("Resource not found: " + resourcePath);
			}

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
