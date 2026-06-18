package com.club.controller.admin;

import com.club.config.DatabaseConfig;
import com.club.dao.UtilisateurDAO;
import com.club.util.AlertUtil;
import com.club.util.Session;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import java.sql.SQLException;

public class ParametresController {
    @FXML private TextField nomClubField, fraisField;
    @FXML private TextField telephoneClubField, emailClubField;
    @FXML private CheckBox validationAutoCheck;
    @FXML private PasswordField ancienMdpField, nouveauMdpField, confirmMdpField;
    @FXML private Label mdpLabel, saveLabel;

    private final UtilisateurDAO dao = new UtilisateurDAO();

    @FXML public void initialize() {
        nomClubField.setText(DatabaseConfig.getConfig("nom_club"));
        String frais = DatabaseConfig.getConfig("frais_inscription");
        fraisField.setText(frais.isEmpty() ? "200" : frais);
        validationAutoCheck.setSelected("true".equals(DatabaseConfig.getConfig("validation_auto")));

        String tel = DatabaseConfig.getConfig("telephone_club");
        telephoneClubField.setText(tel.isEmpty() ? "06XXXXXXXX" : tel);
        String email = DatabaseConfig.getConfig("email_club");
        emailClubField.setText(email.isEmpty() ? "gymfitness@gmail.com" : email);
    }

    @FXML private void handleSave() {
        String nom = nomClubField.getText().trim();
        String fraisStr = fraisField.getText().trim();
        if (nom.isEmpty()) { AlertUtil.erreur("Validation", "Le nom du club est obligatoire."); return; }
        try {
            double frais = Double.parseDouble(fraisStr.replace(",", "."));
            if (frais < 0) throw new NumberFormatException();
            DatabaseConfig.setConfig("nom_club", nom);
            DatabaseConfig.setConfig("frais_inscription", String.valueOf(frais));
            DatabaseConfig.setConfig("validation_auto", validationAutoCheck.isSelected() ? "true" : "false");
            DatabaseConfig.setConfig("telephone_club", telephoneClubField.getText().trim());
            DatabaseConfig.setConfig("email_club", emailClubField.getText().trim());
            saveLabel.setText("✅ Paramètres enregistrés !");
            saveLabel.setStyle("-fx-text-fill:#27ae60;-fx-font-size:13px;-fx-font-weight:bold;");
            saveLabel.setVisible(true); saveLabel.setManaged(true);
            FadeTransition ft = new FadeTransition(Duration.seconds(3), saveLabel);
            ft.setFromValue(1); ft.setToValue(0);
            ft.setDelay(Duration.seconds(1));
            ft.setOnFinished(e -> { saveLabel.setVisible(false); saveLabel.setManaged(false); });
            ft.play();
        } catch (NumberFormatException e) {
            AlertUtil.erreur("Validation", "Les frais d'inscription doivent être un nombre valide.");
        }
    }

    @FXML private void handleChangerMdp() {
        String ancien = ancienMdpField.getText();
        String nouveau = nouveauMdpField.getText();
        String confirm = confirmMdpField.getText();
        hideMdpMsg();
        if (ancien.isEmpty() || nouveau.isEmpty() || confirm.isEmpty()) {
            showMdpMsg("⚠️ Remplissez tous les champs.", false); return;
        }
        if (nouveau.length() < 6) {
            showMdpMsg("⚠️ Minimum 6 caractères.", false); return;
        }
        if (!nouveau.equals(confirm)) {
            showMdpMsg("⚠️ Les mots de passe ne correspondent pas.", false); return;
        }
        try {
            int id = Session.getInstance().getUtilisateur().getId();
            if (!dao.verifierMotDePasse(id, ancien)) {
                showMdpMsg("❌ Mot de passe actuel incorrect.", false); return;
            }
            dao.changerMotDePasse(id, nouveau);
            showMdpMsg("✅ Mot de passe changé avec succès !", true);
            ancienMdpField.clear(); nouveauMdpField.clear(); confirmMdpField.clear();
        } catch (SQLException e) {
            showMdpMsg("❌ Erreur: " + e.getMessage(), false);
        }
    }

    private void showMdpMsg(String msg, boolean success) {
        mdpLabel.setText(msg);
        mdpLabel.setStyle(success
            ? "-fx-text-fill:#27ae60;-fx-background-color:#e8f8f5;-fx-background-radius:8;-fx-padding:10 14;"
            : "-fx-text-fill:#e74c3c;-fx-background-color:#fde8e8;-fx-background-radius:8;-fx-padding:10 14;");
        mdpLabel.setVisible(true); mdpLabel.setManaged(true);
    }
    private void hideMdpMsg() { mdpLabel.setVisible(false); mdpLabel.setManaged(false); }
}
