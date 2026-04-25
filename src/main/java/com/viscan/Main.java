package com.viscan;

import com.viscan.controller.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/viscan/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 900, 500);
        scene.getStylesheets().add(
                getClass().getResource("/com/viscan/css/listview.css").toExternalForm()
        );
        primaryStage.setTitle("Viscan");
        primaryStage.getIcons().add(new Image(
                getClass().getResourceAsStream("/com/viscan/images/icon.png")
        ));
        primaryStage.setScene(scene);
        primaryStage.show();

        MainController mainController = loader.getController();
        primaryStage.setOnCloseRequest(event -> {
            try {
                FileItemStorage.save(
                        new ArrayList<>(mainController.getFileItems()),
                        Path.of("file-items.json") //todo
                );
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // terminalfx may cause app unable to exit idky
                Platform.exit();
                System.exit(0);
            }
        });


    }

    public static void main(String[] args) {
        launch();
    }
}
