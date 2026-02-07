package reto_psp.cliente;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import reto_psp.cliente.model.Game;

public class GameCreateDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField txtTitle;
	private JTextField txtVideoLink;
	private JTextArea txtDescription;

	private JTextField txtImage;
	private JTextField txtApk;

	private File selectedImage;
	private File selectedApk;

	private Game createdGame;
	private boolean submitted = false;

	public GameCreateDialog(JFrame parent) {
		super(parent, "Create Game", true); // modal
		initUI();
		pack();
		setLocationRelativeTo(parent);
	}

	private void initUI() {
		setLayout(new BorderLayout(10, 10));

		JPanel formPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		int row = 0;

		gbc.gridx = 0;
		gbc.gridy = row;
		formPanel.add(new JLabel("Title:"), gbc);

		txtTitle = new JTextField(20);
		gbc.gridx = 1;
		formPanel.add(txtTitle, gbc);
		row++;

		gbc.gridx = 0;
		gbc.gridy = row;
		formPanel.add(new JLabel("Video link:"), gbc);

		txtVideoLink = new JTextField(20);
		gbc.gridx = 1;
		formPanel.add(txtVideoLink, gbc);
		row++;

		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.NORTH;
		formPanel.add(new JLabel("Description:"), gbc);

		txtDescription = new JTextArea(4, 20);
		txtDescription.setLineWrap(true);
		txtDescription.setWrapStyleWord(true);
		gbc.gridx = 1;
		formPanel.add(new JScrollPane(txtDescription), gbc);
		row++;

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 0;
		gbc.gridy = row;
		formPanel.add(new JLabel("Image:"), gbc);

		JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
		txtImage = new JTextField();
		txtImage.setEditable(false);
		JButton btnImage = new JButton("Browse...");
		btnImage.addActionListener(e -> chooseImage());

		imagePanel.add(txtImage, BorderLayout.CENTER);
		imagePanel.add(btnImage, BorderLayout.EAST);

		gbc.gridx = 1;
		formPanel.add(imagePanel, gbc);
		row++;

		gbc.gridx = 0;
		gbc.gridy = row;
		formPanel.add(new JLabel("APK:"), gbc);

		JPanel apkPanel = new JPanel(new BorderLayout(5, 0));
		txtApk = new JTextField();
		txtApk.setEditable(false);
		JButton btnApk = new JButton("Browse...");
		btnApk.addActionListener(e -> chooseApk());

		apkPanel.add(txtApk, BorderLayout.CENTER);
		apkPanel.add(btnApk, BorderLayout.EAST);

		gbc.gridx = 1;
		formPanel.add(apkPanel, gbc);

		add(formPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton btnCancel = new JButton("Cancel");
		JButton btnCreate = new JButton("Create");

		btnCancel.addActionListener(e -> dispose());
		btnCreate.addActionListener(e -> submit());

		buttonPanel.add(btnCancel);
		buttonPanel.add(btnCreate);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void chooseImage() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);


		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			selectedImage = chooser.getSelectedFile();
			txtImage.setText(selectedImage.getName());
		}
	}

	private void chooseApk() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			selectedApk = chooser.getSelectedFile();
			txtApk.setText(selectedApk.getName());
		}
	}

	private void submit() {
		setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
		if (txtTitle.getText().isBlank()) {
			JOptionPane.showMessageDialog(this, "Title is required");
			return;
		}

		createdGame = new Game();
		createdGame.setTitle(txtTitle.getText());
		createdGame.setVideoLink(txtVideoLink.getText());
		createdGame.setDescription(txtDescription.getText());

		submitted = true;
		dispose();
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public Game getCreatedGame() {
		return createdGame;
	}

	public File getSelectedImage() {
		return selectedImage;
	}

	public File getSelectedApk() {
		return selectedApk;
	}
}
