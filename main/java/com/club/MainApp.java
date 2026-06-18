package com.club;

import com.club.config.DatabaseConfig;
import com.club.util.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Init BDD
        DatabaseConfig.initializeDatabase();

        // Config fenêtre principale
        primaryStage.setTitle("Club Sportif");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.centerOnScreen();

        NavigationManager.setPrimaryStage(primaryStage);

        // Démarrer sur la page de connexion
        NavigationManager.naviguerVers("/fxml/login.fxml", "Connexion");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
