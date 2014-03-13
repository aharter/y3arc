package de.andreas.harter.yaaarc;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Yaaarc extends Application {

	private static final String FXML_FILE = "/fxml/Yaaarc.fxml";

	private static final double STAGE_WIDTH = 800.0;
	private static final double STAGE_HEIGHT = 630.0;

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	public void start(Stage stage) throws Exception {
		Stage mainStage = getMainStage(getParent());
		mainStage.show();
	}

	private Parent getParent() throws IOException {
		URL url = getClass().getResource(FXML_FILE);
		FXMLLoader loader = new FXMLLoader(url);
		Parent parent = (Parent) loader.load();
		return parent;
	}

	private Stage getMainStage(Parent parent) {
		Scene scene = new Scene(parent);
		Stage stage = new Stage();
		stage.initStyle(StageStyle.UTILITY);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle("Y3EARC!");
		stage.setResizable(false);
		stage.setMinHeight(STAGE_HEIGHT);
		stage.setMaxHeight(STAGE_HEIGHT);
		stage.setMinWidth(STAGE_WIDTH);
		stage.setMaxWidth(STAGE_WIDTH);
		stage.setScene(scene);
		return stage;
	}
}
