package com.club.controller.auth;

import com.club.config.DatabaseConfig;
import com.club.dao.PaiementDAO;
import com.club.dao.UtilisateurDAO;
import com.club.model.Utilisateur;
import com.club.util.AlertUtil;
import com.club.util.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;

public class RegisterController {
    @FXML private TextField prenomField, nomField, emailField, telephoneField, dateNaissanceField, adresseField;
    @FXML private PasswordField passwordField, confirmField;
    @FXML private Label errorLabel;

    private final UtilisateurDAO dao = new UtilisateurDAO();

    @FXML private void handleRegister() {
        String prenom = prenomField.getText().trim();
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String mdp = passwordField.getText();
        String confirm = confirmField.getText();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            showError("⚠️ Les champs marqués * sont obligatoires."); return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            showError("⚠️ Adresse email invalide."); return;
        }
        if (mdp.length() < 6) {
            showError("⚠️ Mot de passe : minimum 6 caractères."); return;
        }
        if (!mdp.equals(confirm)) {
            showError("⚠️ Les mots de passe ne correspondent pas."); return;
        }

        try {
            if (dao.emailExiste(email)) { showError("⚠️ Cet email est déjà utilisé."); return; }

            boolean validationAuto = "true".equals(DatabaseConfig.getConfig("validation_auto"));
            String statut = validationAuto ? "ACTIF" : "EN_ATTENTE";

            Utilisateur u = new Utilisateur(nom, prenom, email, mdp, "ADHERENT");
            u.setStatut(statut);
            u.setTelephone(telephoneField.getText().trim());
            u.setDateNaissance(dateNaissanceField.getText().trim());
            u.setAdresse(adresseField.getText().trim());
            int id = dao.inserer(u);

            if (validationAuto && id > 0) {
                double frais = Double.parseDouble(DatabaseConfig.getConfig("frais_inscription").isEmpty() ? "200" : DatabaseConfig.getConfig("frais_inscription"));
                new PaiementDAO().creerOuMettreAJour(id, frais, frais);
            }

            String msg = validationAuto
                ? "✅ Compte créé ! Vous pouvez vous connecter."
                : "✅ Compte créé avec succès !\n\n⏳ Votre compte est en attente de validation par l'administration.\nVous serez informé(e) dès que votre accès sera activé.";
            AlertUtil.succes("Inscription réussie", msg);
            NavigationManager.naviguerVers("/fxml/login.fxml", "Connexion");
        } catch (SQLException e) {
            showError("❌ Erreur lors de la création du compte."); e.printStackTrace();
        }
    }

    @FXML private void handleBackToLogin() {
        NavigationManager.naviguerVers("/fxml/login.fxml", "Connexion");
    }

    private void showError(String msg) {
        errorLabel.setText(msg); errorLabel.setVisible(true); errorLabel.setManaged(true);
    }
}
