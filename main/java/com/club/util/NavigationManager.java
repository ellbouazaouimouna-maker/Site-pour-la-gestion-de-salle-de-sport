package com.club.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }
    public static Stage getPrimaryStage() { return primaryStage; }

    public static void naviguerVers(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(NavigationManager.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setTitle("Club Sportif - " + titre);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.erreur("Erreur de navigation", "Impossible de charger la page: " + fxmlPath);
        }
    }

    public static <T> T chargerControleur(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }
}
