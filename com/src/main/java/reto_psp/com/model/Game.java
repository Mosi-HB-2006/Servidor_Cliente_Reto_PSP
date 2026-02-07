package reto_psp.com.model;

public class Game {
	private Long id;
	private String title;
	private String imagePath;
	private String apkPath;
	private String videoLink;
	private String description;

	public Game() {
	}

	public Game(Long id, String title, String imagePath, String apkPath, String videoLink, String description) {
		super();
		this.id = id;
		this.title = title;
		this.imagePath = imagePath;
		this.apkPath = apkPath;
		this.videoLink = videoLink;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getApkPath() {
		return apkPath;
	}

	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}

	public String getVideoLink() {
		return videoLink;
	}

	public void setVideoLink(String videoLink) {
		this.videoLink = videoLink;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Game [id=" + id + ", title=" + title + ", imagePath=" + imagePath + ", apkPath=" + apkPath
				+ ", videoLink=" + videoLink + ", description=" + description + "]";
	}
}
