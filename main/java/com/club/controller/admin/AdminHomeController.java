package com.club.controller.admin;

import com.club.dao.ActiviteDAO;
import com.club.dao.PaiementDAO;
import com.club.dao.UtilisateurDAO;
import com.club.model.Utilisateur;
import com.club.util.AlertUtil;
import com.club.util.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.util.List;

public class AdminHomeController {
    @FXML private Label welcomeLabel, totalAdherents, totalActivites, paiementsEnAttente, totalPaiements;
    @FXML private Label enAttenteValidation, bloques;
    @FXML private TableView<Utilisateur> recentAdherentsTable;
    @FXML private TableColumn<Utilisateur,String> colNom, colEmail, colDate, colStatut;

    private final UtilisateurDAO userDAO     = new UtilisateurDAO();
    private final ActiviteDAO activiteDAO    = new ActiviteDAO();
    private final PaiementDAO paiementDAO    = new PaiementDAO();

    @FXML public void initialize() {
        welcomeLabel.setText("Bonjour, " + Session.getInstance().getUtilisateur().getPrenom() + " 👋");
        recentAdherentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTable();
        loadStats();
    }

    private void setupTable() {
        colNom.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNomComplet()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                switch (s) {
                    case "ACTIF"      -> { setText("✅ Actif");      setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;"); }
                    case "BLOQUE"     -> { setText("⛔ Bloqué");     setStyle("-fx-text-fill:#e74c3c;-fx-font-weight:bold;"); }
                    case "EN_ATTENTE" -> { setText("⏳ En attente"); setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;"); }
                    default           -> { setText("📦 Archivé");    setStyle("-fx-text-fill:#95a5a6;"); }
                }
            }
        });
    }

    private void loadStats() {
        // Afficher des valeurs provisoires pendant le chargement
        totalAdherents.setText("...");
        totalActivites.setText("...");
        paiementsEnAttente.setText("...");
        totalPaiements.setText("...");
        enAttenteValidation.setText("...");
        bloques.setText("...");

        Task<Void> task = new Task<>() {
            int nbAdherents, nbActivites, nbEnAttente, nbBloques, nbPaiementsAttente;
            double totalPaie;
            List<Utilisateur> recents;

            @Override protected Void call() throws Exception {
                // Toutes les requêtes en parallèle via un seul thread background
                nbAdherents      = userDAO.countAdherents();
                nbActivites      = activiteDAO.countActivites();
                nbPaiementsAttente = paiementDAO.countPaiementsEnAttente();
                totalPaie        = paiementDAO.getTotalPaiementsRecus();
                nbEnAttente      = userDAO.countEnAttente();
                nbBloques        = userDAO.countBloques();
                recents          = userDAO.getTousLesAdherents();
                return null;
            }

            @Override protected void succeeded() {
                Platform.runLater(() -> {
                    totalAdherents.setText(String.valueOf(nbAdherents));
                    totalActivites.setText(String.valueOf(nbActivites));
                    paiementsEnAttente.setText(String.valueOf(nbPaiementsAttente));
                    totalPaiements.setText(String.format("%.0f DH", totalPaie));
                    enAttenteValidation.setText(String.valueOf(nbEnAttente));
                    bloques.setText(String.valueOf(nbBloques));
                    int from = Math.max(0, recents.size() - 8);
                    recentAdherentsTable.setItems(FXCollections.observableArrayList(
                        recents.subList(from, recents.size())));
                });
            }

            @Override protected void failed() {
                getException().printStackTrace();
            }
        };

        new Thread(task).start();
    }

    @FXML private void quickAddAdherent() { new AdherentFormDialog(null).showAndWait(); loadStats(); }
    @FXML private void quickAddActivite() { new ActiviteFormDialog(null).showAndWait(); loadStats(); }
    @FXML private void quickPaiement() {
        AlertUtil.avertissement("Info", "Allez dans la section 'Paiements' pour enregistrer un paiement.");
    }
}
