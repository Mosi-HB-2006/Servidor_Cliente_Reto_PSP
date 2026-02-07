package reto_psp.cliente;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reto_psp.cliente.model.Game;

public class SwingApplication extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final RestClient client;

	private final DefaultListModel<Game> listModel = new DefaultListModel<>();
	private final JList<Game> gameList = new JList<>(listModel);

	private final JButton btnGetAll = new JButton("Get All Games");
	private final JButton btnGetById = new JButton("Get Game by ID");
	private final JButton btnGetApk = new JButton("Get Game APK");
	private final JButton btnGetImage = new JButton("Get Game Image");
	private final JButton btnGetHash = new JButton("Get Game Hash");
	private final JButton btnVerifyApk = new JButton("Verify Game APK");
	private final JButton btnCreate = new JButton("Create Game");
	private final JButton btnModify = new JButton("Modify Game");
	private final JButton btnDelete = new JButton("Delete Game");

	private final JTextField txtId = new JTextField(5);
	
	public static final Path ROOT = Paths.get("../shared-files/client-files");
	public static final Path APK_DIR = Paths.get("../shared-files/client-files/apks");
	public static final Path IMAGE_DIR = Paths.get("../shared-files/client-files/images");

	public SwingApplication(RestClient client) {
		super("Cliente REST - Games");
		this.client = client;
		initUI();
	}

	private void initUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700, 400);
		setLocationRelativeTo(null);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));

		topPanel.add(new JLabel("Game ID:"));
		topPanel.add(txtId);

		topPanel.add(btnGetAll);
		topPanel.add(btnGetById);
		topPanel.add(btnGetApk);
		topPanel.add(btnGetImage);
		topPanel.add(btnGetHash);
		topPanel.add(btnVerifyApk);
		topPanel.add(btnCreate);
		topPanel.add(btnModify);
		topPanel.add(btnDelete);

		JScrollPane topScrollPane = new JScrollPane(topPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		topScrollPane.getHorizontalScrollBar().setUnitIncrement(20);

		add(topScrollPane, BorderLayout.NORTH);
		add(new JScrollPane(gameList), BorderLayout.CENTER);

		btnGetAll.addActionListener(e -> loadAllGames());
		btnGetById.addActionListener(e -> loadGameById());
		btnGetApk.addActionListener(e -> loadGameApk());
		btnGetImage.addActionListener(e -> loadGameImage());
		btnGetHash.addActionListener(e -> loadGameHash());
		btnVerifyApk.addActionListener(e -> verifyGameApk());
		btnCreate.addActionListener(e -> createGame());
		btnModify.addActionListener(e -> modifyGame());
		btnDelete.addActionListener(e -> deleteGame());
	}

	private Long getIdFromField() {
		if (txtId.getText().equals("")) {
			return null;
		} else {
			return Long.parseLong(txtId.getText());
		}
	}

	private void loadAllGames() {
		listModel.clear();

		SwingWorker<ResponseEntity<List<Game>>, Void> worker = new SwingWorker<>() {
			@Override
			protected ResponseEntity<List<Game>> doInBackground() {
				return client.getAllGames();
			}

			@Override
			protected void done() {
				try {
					ResponseEntity<List<Game>> response = get();
					List<Game> games = response.getBody();

					if (games != null) {
						for (Game game : games) {
							listModel.addElement(game);
						}
					}
				} catch (Exception ex) {
					System.out.println(ex);
				}
			}
		};

		worker.execute();
	}

	private void loadGameById() {
		listModel.clear();

		Long id = getIdFromField();
		if (id == null) {
			JOptionPane.showMessageDialog(this, "Please enter a valid Game ID", "Invalid input",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		SwingWorker<ResponseEntity<Game>, Void> worker = new SwingWorker<>() {
			@Override
			protected ResponseEntity<Game> doInBackground() {
				return client.getGameId(id);
			}

			@Override
			protected void done() {
				try {
					ResponseEntity<Game> response = get();

					Game game = response.getBody();
					if (game != null) {
						listModel.addElement(game);
					}

				} catch (Exception ex) {
					showError(ex);
				}
			}
		};

		worker.execute();
	}

	private void loadGameApk() {
	    Long id = getIdFromField();
	    if (id == null) {
	        JOptionPane.showMessageDialog(this, "Please enter a valid Game ID", "Invalid input",
	                JOptionPane.WARNING_MESSAGE);
	        return;
	    }

	    SwingWorker<Void, Void> worker = new SwingWorker<>() {
	        @Override
	        protected Void doInBackground() throws Exception {
	            ResponseEntity<byte[]> response = client.getGameApk(id);
	            byte[] apkBytes = response.getBody();
	            
	            if (apkBytes != null) {
	                String filename = "game-" + id + ".apk";
	                Path savePath = APK_DIR.resolve(filename);
	                Files.write(savePath, apkBytes);
	            }
	            
	            return null;
	        }

	        @Override
	        protected void done() {
	            try {
	                get();
	                JOptionPane.showMessageDialog(SwingApplication.this,
	                        "APK downloaded successfully to: " + APK_DIR.toAbsolutePath());
	            } catch (Exception ex) {
	                showError(ex);
	            }
	        }
	    };

	    worker.execute();
	}


	private void loadGameImage() {
		listModel.clear();

		Long id = getIdFromField();
		if (id == null) {
			JOptionPane.showMessageDialog(this, "Please enter a valid Game ID", "Invalid input",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		SwingWorker<ResponseEntity<byte[]>, Void> worker = new SwingWorker<>() {
			@Override
			protected ResponseEntity<byte[]> doInBackground() {
				return client.getGameImage(getIdFromField());
			}

			@Override
			protected void done() {
				try {
					byte[] apk = get().getBody();
					JOptionPane.showMessageDialog(SwingApplication.this,
							"Image recieved: " + (apk != null ? apk.length : 0) + " bytes");
				} catch (Exception ex) {
					showError(ex);
				}
			}
		};

		worker.execute();
	}

	private void loadGameHash() {
		listModel.clear();

		Long id = getIdFromField();
		if (id == null) {
			JOptionPane.showMessageDialog(this, "Please enter a valid Game ID", "Invalid input",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		SwingWorker<ResponseEntity<String>, Void> worker = new SwingWorker<>() {
			@Override
			protected ResponseEntity<String> doInBackground() {
				return client.getGameHash(getIdFromField());
			}

			@Override
			protected void done() {
				try {
					String hash = get().getBody();
					JOptionPane.showMessageDialog(SwingApplication.this, "Hash del APK:\n" + hash);
				} catch (Exception ex) {
					showError(ex);
				}
			}
		};
		worker.execute();
	}

	private void verifyGameApk() {
		listModel.clear();

		Long id = getIdFromField();
		if (id == null) {
			JOptionPane.showMessageDialog(this, "Please enter a valid Game ID", "Invalid input",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		SwingWorker<ResponseEntity<Boolean>, Void> worker = new SwingWorker<>() {
			@Override
			protected ResponseEntity<Boolean> doInBackground() {
				byte[] apk = client.getGameApk(getIdFromField()).getBody();
				return client.verifyGameApk(getIdFromField(), apk);
			}

			@Override
			protected void done() {
				try {
					Boolean valid = get().getBody();
					JOptionPane.showMessageDialog(SwingApplication.this, "Valid APK: " + valid);
				} catch (Exception ex) {
					showError(ex);
				}
			}
		};
		worker.execute();
	}

	private void createGame() {
		GameCreateDialog dialog = new GameCreateDialog(this);
		dialog.setVisible(true);

		if (dialog.isSubmitted()) {
			Game game = dialog.getCreatedGame();
			File image = dialog.getSelectedImage();
			File apk = dialog.getSelectedApk();

			SwingWorker<ResponseEntity<Game>, Void> worker = new SwingWorker<>() {
				@Override
			    protected ResponseEntity<Game> doInBackground() {
					
					try {
						return client.createGame(game, apk, image);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println(e);
						return ResponseEntity.internalServerError().build();
					}
			    }

				@Override
				protected void done() {
					try {
						Game created = get().getBody();
						JOptionPane.showMessageDialog(SwingApplication.this, "Game created:\n" + created);
					} catch (Exception ex) {
						showError(ex);
					}
				}
			};

			worker.execute();
		}
	}

	private void modifyGame() {
		SwingWorker<ResponseEntity<Game>, Void> worker = new SwingWorker<>() {
			@Override
			protected ResponseEntity<Game> doInBackground() {
				Game game = new Game();
				game.setId(getIdFromField());
				game.setTitle("Juego Modificado");
				return client.modifyGame(game);
			}

			@Override
			protected void done() {
				try {
					Game modified = get().getBody();
					JOptionPane.showMessageDialog(SwingApplication.this, "Game modified:\n" + modified);
				} catch (Exception ex) {
					showError(ex);
				}
			}
		};
		worker.execute();
	}

	private void deleteGame() {
		listModel.clear();

		Long id = getIdFromField();
		if (id == null) {
			JOptionPane.showMessageDialog(this, "Please enter a valid Game ID", "Invalid input",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		SwingWorker<ResponseEntity<Void>, Void> worker = new SwingWorker<>() {
			@Override
			protected ResponseEntity<Void> doInBackground() {
				return client.deleteGame(getIdFromField());
			}

			@Override
			protected void done() {
				try {
					get();
					JOptionPane.showMessageDialog(SwingApplication.this, "Juego eliminado correctamente");
				} catch (Exception ex) {
					showError(ex);
				}
			}
		};
		worker.execute();
	}

	private void showError(Exception e) {
		String errorText;

		if (e.getCause() instanceof WebClientResponseException webError) {
			errorText = webError.getStatusCode().value() + " " + webError.getStatusText() + " from "
					+ webError.getRequest().getMethod() + " " + webError.getRequest().getURI();
		} else {
			errorText = e.getMessage();
		}

		JOptionPane.showMessageDialog(this, "ERROR: " + errorText, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) {
		String baseUrl = "http://localhost:8080/api/games/";
		RestClient client = new RestClient(baseUrl);
		
		try {
			Files.createDirectories(APK_DIR);
			Files.createDirectories(IMAGE_DIR);
		} catch (IOException e) {
			System.out.println("Error occurred creating local files.");
		}

		EventQueue.invokeLater(() -> new SwingApplication(client).setVisible(true));
	}
}
