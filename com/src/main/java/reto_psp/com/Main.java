package reto_psp.com;

import java.util.List;

import reto_psp.com.model.Game;

public class Main {

	public static void main(String[] args) {
		String baseUrl = "http://localhost:8080/api/games/";
		RestClient client = new RestClient(baseUrl);
		
		System.out.println("*****OBTENER JUEGO APK*****");
		byte[] game_apk = client.getGameApk(1).getBody();
		System.out.println(game_apk.length);
		
		System.out.println("*****VERIFICAR JUEGO APK*****");
		Boolean valid = client.verifyGameApk(1, game_apk).getBody();
		System.out.println(valid);
	}

}
