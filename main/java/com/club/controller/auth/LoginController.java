package com.club.controller.auth;

import com.club.dao.UtilisateurDAO;
import com.club.model.Utilisateur;
import com.club.util.EmailService;
import com.club.util.NavigationManager;
import com.club.util.Session;
import com.club.config.DatabaseConfig;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private CheckBox sesouvenir;
    @FXML private Label telephoneLabel;
    @FXML private Label emailContactLabel;

    private final UtilisateurDAO dao = new UtilisateurDAO();
    private static final Path REMEMBER_FILE = Paths.get(System.getProperty("user.home"), ".clubsportif_remember");

    // Pour le reset mot de passe : stocke email → {code, timestamp}
    private static final Map<String, String[]> resetCodes = new HashMap<>();

    @FXML public void initialize() {
        passwordField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
        emailField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) passwordField.requestFocus(); });

        String tel  = DatabaseConfig.getConfig("telephone_club");
        String mail = DatabaseConfig.getConfig("email_club");
        if (telephoneLabel != null)   telephoneLabel.setText("📞  " + (tel.isEmpty()  ? "06XXXXXXXX" : tel));
        if (emailContactLabel != null) emailContactLabel.setText("✉  " + (mail.isEmpty() ? "contact@club.com" : mail));

        try {
            if (Files.exists(REMEMBER_FILE)) {
                String saved = Files.readString(REMEMBER_FILE).trim();
                if (!saved.isEmpty()) { emailField.setText(saved); sesouvenir.setSelected(true); passwordField.requestFocus(); }
            }
        } catch (Exception ignored) {}
    }

    @FXML private void handleLogin() {
        String email = emailField.getText().trim();
        String mdp   = passwordField.getText();
        if (email.isEmpty() || mdp.isEmpty()) { showError("⚠️ Veuillez remplir tous les champs."); return; }

        loginButton.setDisable(true);
        loginButton.setText("Connexion...");

        try {
            Utilisateur user = dao.authentifier(email, mdp);
            if (user != null) {
                if (sesouvenir.isSelected()) Files.writeString(REMEMBER_FILE, email);
                else Files.deleteIfExists(REMEMBER_FILE);
                Session.getInstance().connecter(user);
                hideError();
                if (user.hasAdminRights())
                    NavigationManager.naviguerVers("/fxml/admin/dashboard.fxml", "Tableau de bord");
                else
                    NavigationManager.naviguerVers("/fxml/adherent/dashboard.fxml", "Mon Espace");
            } else {
                showError("❌ Email ou mot de passe incorrect.");
            }
        } catch (SecurityException e) {
            handleSecurityException(e, email);
        } catch (Exception e) {
            showError("❌ Erreur de connexion. Vérifiez votre connexion internet.");
        } finally {
            loginButton.setDisable(false);
            loginButton.setText("Se connecter");
        }
    }

    private void handleSecurityException(SecurityException e, String email) {
        String msg = e.getMessage();
        if (msg.equals("MAX_TENTATIVES") || msg.startsWith("BLOQUE_TEMP:")) {
            // Envoyer email de blocage automatique
            try {
                Utilisateur u = dao.getParEmail(email);
                if (u != null) EmailService.sendCompteBloquéAuto(u.getEmail(), u.getPrenom());
            } catch (Exception ignored) {}
            showError("⛔ Compte bloqué après 3 tentatives.\nContactez l'administration ou réessayez dans 30 minutes.");
        } else if (msg.startsWith("MOT_DE_PASSE_INCORRECT:")) {
            int r = Integer.parseInt(msg.split(":")[1]);
            showError("❌ Mot de passe incorrect. " + r + " tentative(s) restante(s).");
        } else if (msg.equals("BLOQUE_ADMIN")) {
            showError("⛔ Compte suspendu par l'administration.\nContactez-nous pour plus d'informations.");
        } else if (msg.equals("EN_ATTENTE")) {
            showError("⏳ Compte en attente de validation.\nContactez l'administration.");
        } else if (msg.equals("ARCHIVE")) {
            showError("📦 Ce compte est archivé. Contactez l'administration.");
        }
    }

    @FXML private void handleForgotPassword() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) { showError("⚠️ Entrez votre email pour réinitialiser le mot de passe."); return; }

        try {
            Utilisateur u = dao.getParEmail(email);
            if (u == null) { showError("❌ Aucun compte trouvé avec cet email."); return; }

            // Générer et envoyer le code
            String code = EmailService.genererCode();
            resetCodes.put(email, new String[]{code, String.valueOf(System.currentTimeMillis())});
            EmailService.sendResetCode(email, u.getPrenom(), code);

            showSuccess("✅ Code envoyé sur " + email + " !\nVérifiez votre boîte mail.");

            // Afficher dialog pour saisir le code
            afficherDialogReset(u, email);

        } catch (Exception e) {
            showError("❌ Erreur lors de l'envoi. Vérifiez votre connexion.");
        }
    }

    private void afficherDialogReset(Utilisateur u, String email) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Réinitialiser le mot de passe");
        dialog.setHeaderText("Entrez le code reçu par email et votre nouveau mot de passe");

        TextField codeField = new TextField();
        codeField.setPromptText("Code à 6 chiffres");

        PasswordField newPass    = new PasswordField();
        newPass.setPromptText("Nouveau mot de passe (min. 6 caractères)");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirmer le mot de passe");

        Label matchLabel = new Label();
        confirmPass.textProperty().addListener((obs, o, nv) -> {
            if (nv.equals(newPass.getText())) {
                matchLabel.setText("✅ Les mots de passe correspondent");
                matchLabel.setStyle("-fx-text-fill:#27ae60;");
            } else {
                matchLabel.setText("❌ Les mots de passe ne correspondent pas");
                matchLabel.setStyle("-fx-text-fill:#e74c3c;");
            }
        });

        Label errLabel = new Label();
        errLabel.setStyle("-fx-text-fill:#e74c3c;");

        VBox content = new VBox(10, 
            new Label("Code reçu par email :"), codeField,
            new Label("Nouveau mot de passe :"), newPass,
            new Label("Confirmer :"), confirmPass, matchLabel, errLabel);
        content.setPadding(new javafx.geometry.Insets(10));
        dialog.getDialogPane().setContent(content);

        ButtonType btnOk     = new ButtonType("Changer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, btnCancel);

        // Empêcher fermeture si erreur
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnOk);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String[] stored = resetCodes.get(email);
            if (stored == null) { errLabel.setText("❌ Code expiré. Recommencez."); event.consume(); return; }

            // Vérifier expiration 10 min
            long elapsed = System.currentTimeMillis() - Long.parseLong(stored[1]);
            if (elapsed > 10 * 60 * 1000) {
                errLabel.setText("❌ Code expiré (10 min). Recommencez.");
                resetCodes.remove(email); event.consume(); return;
            }
            if (!stored[0].equals(codeField.getText().trim())) {
                errLabel.setText("❌ Code incorrect."); event.consume(); return;
            }
            if (newPass.getText().length() < 6) {
                errLabel.setText("⚠️ Mot de passe trop court (min. 6 caractères)."); event.consume(); return;
            }
            if (!newPass.getText().equals(confirmPass.getText())) {
                errLabel.setText("❌ Les mots de passe ne correspondent pas."); event.consume(); return;
            }

            try {
                dao.changerMotDePasse(u.getId(), newPass.getText());
                resetCodes.remove(email);
                showSuccess("✅ Mot de passe réinitialisé avec succès !");
            } catch (Exception ex) {
                errLabel.setText("❌ Erreur lors de la réinitialisation."); event.consume();
            }
        });

        dialog.setResultConverter(bt -> null);
        dialog.showAndWait();
    }

    @FXML private void handleRegister() {
        NavigationManager.naviguerVers("/fxml/auth/register.fxml", "Créer un compte");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill:#e74c3c;-fx-background-color:#fde8e8;-fx-background-radius:6;-fx-padding:8 12;");
        errorLabel.setVisible(true); errorLabel.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void showSuccess(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill:#27ae60;-fx-background-color:#e8f8f5;-fx-background-radius:6;-fx-padding:8 12;");
        errorLabel.setVisible(true); errorLabel.setManaged(true);
    }

    private void hideError() { errorLabel.setVisible(false); errorLabel.setManaged(false); }
}
