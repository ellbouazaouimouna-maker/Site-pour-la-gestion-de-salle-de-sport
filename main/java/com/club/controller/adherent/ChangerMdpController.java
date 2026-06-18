package com.club.controller.adherent;

import com.club.dao.UtilisateurDAO;
import com.club.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ChangerMdpController {
    @FXML private PasswordField ancienMdpField, nouveauMdpField, confirmMdpField;
    @FXML private Label msgLabel;
    @FXML private Label matchIndicator;

    private final UtilisateurDAO dao = new UtilisateurDAO();

    @FXML public void initialize() {
        // Vérification en temps réel quand l'utilisateur tape dans confirmMdpField
        confirmMdpField.textProperty().addListener((obs, oldVal, newVal) -> {
            String nouveau = nouveauMdpField.getText();
            if (newVal.isEmpty()) {
                matchIndicator.setVisible(false);
                matchIndicator.setManaged(false);
            } else if (nouveau.equals(newVal)) {
                matchIndicator.setText("✅ Les mots de passe correspondent");
                matchIndicator.setStyle("-fx-text-fill:#27ae60; -fx-font-size:12px;");
                matchIndicator.setVisible(true);
                matchIndicator.setManaged(true);
            } else {
                matchIndicator.setText("❌ Les mots de passe ne correspondent pas");
                matchIndicator.setStyle("-fx-text-fill:#e74c3c; -fx-font-size:12px;");
                matchIndicator.setVisible(true);
                matchIndicator.setManaged(true);
            }
        });

        // Aussi vérifier si l'utilisateur modifie le nouveau mdp après avoir rempli la confirmation
        nouveauMdpField.textProperty().addListener((obs, oldVal, newVal) -> {
            String confirm = confirmMdpField.getText();
            if (confirm.isEmpty()) return;
            if (newVal.equals(confirm)) {
                matchIndicator.setText("✅ Les mots de passe correspondent");
                matchIndicator.setStyle("-fx-text-fill:#27ae60; -fx-font-size:12px;");
                matchIndicator.setVisible(true);
                matchIndicator.setManaged(true);
            } else {
                matchIndicator.setText("❌ Les mots de passe ne correspondent pas");
                matchIndicator.setStyle("-fx-text-fill:#e74c3c; -fx-font-size:12px;");
                matchIndicator.setVisible(true);
                matchIndicator.setManaged(true);
            }
        });
    }

    @FXML private void handleChanger() {
        String ancien = ancienMdpField.getText();
        String nouveau = nouveauMdpField.getText();
        String confirm = confirmMdpField.getText();

        hideMsg();

        // Vérifications
        if (ancien.isEmpty() || nouveau.isEmpty() || confirm.isEmpty()) {
            show("⚠️ Veuillez remplir tous les champs.", false);
            return;
        }
        if (nouveau.length() < 6) {
            show("⚠️ Le nouveau mot de passe doit contenir au moins 6 caractères.", false);
            return;
        }
        if (!nouveau.equals(confirm)) {
            show("❌ Les mots de passe ne correspondent pas. Veuillez vérifier.", false);
            confirmMdpField.clear();
            confirmMdpField.requestFocus();
            return;
        }
        if (nouveau.equals(ancien)) {
            show("⚠️ Le nouveau mot de passe doit être différent de l'ancien.", false);
            return;
        }

        try {
            int id = Session.getInstance().getUtilisateur().getId();
            if (!dao.verifierMotDePasse(id, ancien)) {
                show("❌ Mot de passe actuel incorrect.", false);
                ancienMdpField.requestFocus();
                return;
            }
            dao.changerMotDePasse(id, nouveau);
            show("✅ Mot de passe changé avec succès !", true);
            ancienMdpField.clear();
            nouveauMdpField.clear();
            confirmMdpField.clear();
            matchIndicator.setVisible(false);
            matchIndicator.setManaged(false);
        } catch (Exception e) {
            show("❌ Erreur: " + e.getMessage(), false);
        }
    }

    private void show(String msg, boolean ok) {
        msgLabel.setText(msg);
        msgLabel.setStyle(ok
            ? "-fx-text-fill:#27ae60;-fx-background-color:#e8f8f5;-fx-background-radius:8;-fx-padding:10 14;-fx-font-size:13px;"
            : "-fx-text-fill:#e74c3c;-fx-background-color:#fde8e8;-fx-background-radius:8;-fx-padding:10 14;-fx-font-size:13px;");
        msgLabel.setVisible(true);
        msgLabel.setManaged(true);
    }

    private void hideMsg() {
        msgLabel.setVisible(false);
        msgLabel.setManaged(false);
    }
}
