package com.club.controller.admin;

import com.club.config.DatabaseConfig;
import com.club.dao.PaiementDAO;
import com.club.dao.UtilisateurDAO;
import com.club.model.Utilisateur;
import com.club.util.AlertUtil;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import java.sql.SQLException;

public class AdherentFormDialog extends Dialog<Utilisateur> {
    private final TextField prenomField = new TextField();
    private final TextField nomField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField telephoneField = new TextField();
    private final TextField dateNaissanceField = new TextField();
    private final TextField adresseField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<String> roleBox = new ComboBox<>();
    private final ComboBox<String> statutBox = new ComboBox<>();

    private final UtilisateurDAO dao = new UtilisateurDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();
    private final Utilisateur utilisateur;

    public AdherentFormDialog(Utilisateur u) {
        this.utilisateur = u;
        initModality(Modality.APPLICATION_MODAL);
        setTitle(u == null ? "Nouvel adhérent / Staff" : "Modifier : " + u.getNomComplet());
        getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        getDialogPane().setPrefWidth(560);
        buildUI();
        if (u != null) fillForm(u);

        ButtonType saveBtn = new ButtonType(u == null ? "✅  Créer" : "💾  Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);
        styleBtn(getDialogPane().lookupButton(saveBtn), "#3498db");
        styleBtn(getDialogPane().lookupButton(cancelBtn), "#95a5a6");

        setResultConverter(btn -> btn == saveBtn ? handleSave() : null);
    }

    private void buildUI() {
        roleBox.getItems().addAll("ADHERENT", "ASSISTANT", "ADMIN");
        roleBox.setValue("ADHERENT");
        statutBox.getItems().addAll("ACTIF", "EN_ATTENTE", "BLOQUE", "ARCHIVE");
        statutBox.setValue("ACTIF");

        String fs = "-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:#dfe6e9;-fx-background-color:#f8f9fa;-fx-padding:9;-fx-font-size:13px;";
        String cs = "-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:#dfe6e9;-fx-background-color:#f8f9fa;";
        for (TextField f : new TextField[]{prenomField,nomField,emailField,telephoneField,dateNaissanceField,adresseField,passwordField}) {
            f.setStyle(fs); f.setMaxWidth(Double.MAX_VALUE);
        }
        roleBox.setStyle(cs); roleBox.setMaxWidth(Double.MAX_VALUE);
        statutBox.setStyle(cs); statutBox.setMaxWidth(Double.MAX_VALUE);
        dateNaissanceField.setPromptText("AAAA-MM-JJ");
        telephoneField.setPromptText("+212 6XX XXX XXX");

        GridPane grid = new GridPane(); grid.setHgap(16); grid.setVgap(14);
        grid.setPadding(new Insets(24)); grid.setPrefWidth(520);

        addRow(grid, 0, "Prénom *", prenomField, "Nom *", nomField);
        addRow(grid, 1, "Email *", emailField, "Téléphone", telephoneField);
        addRow(grid, 2, "Date de naissance", dateNaissanceField, "Adresse", adresseField);
        addRow(grid, 3, "Rôle", roleBox, utilisateur == null ? "Mot de passe *" : "Nouveau mot de passe", passwordField);
        if (utilisateur != null) addRow(grid, 4, "Statut du compte", statutBox, "", new Label(""));

        getDialogPane().setContent(grid);
    }

    private void addRow(GridPane g, int row, String l1, javafx.scene.Node f1, String l2, javafx.scene.Node f2) {
        Label la = lbl(l1); Label lb = lbl(l2);
        VBox b1 = new VBox(5, la, f1); VBox b2 = new VBox(5, lb, f2);
        GridPane.setHgrow(b1, Priority.ALWAYS); GridPane.setHgrow(b2, Priority.ALWAYS);
        g.add(b1, 0, row); g.add(b2, 1, row);
    }

    private Label lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#636e72;");
        return l;
    }

    private void fillForm(Utilisateur u) {
        prenomField.setText(u.getPrenom()); nomField.setText(u.getNom());
        emailField.setText(u.getEmail());
        telephoneField.setText(u.getTelephone() != null ? u.getTelephone() : "");
        dateNaissanceField.setText(u.getDateNaissance() != null ? u.getDateNaissance() : "");
        adresseField.setText(u.getAdresse() != null ? u.getAdresse() : "");
        roleBox.setValue(u.getRole()); statutBox.setValue(u.getStatut());
    }

    private Utilisateur handleSave() {
        String prenom = prenomField.getText().trim(), nom = nomField.getText().trim();
        String email = emailField.getText().trim(), mdp = passwordField.getText();

        if (prenom.isEmpty() || nom.isEmpty() || email.isEmpty()) {
            AlertUtil.erreur("Validation", "Prénom, Nom et Email sont obligatoires."); return null;
        }
        if (!email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            AlertUtil.erreur("Validation", "Adresse email invalide."); return null;
        }
        if (utilisateur == null && mdp.isEmpty()) {
            AlertUtil.erreur("Validation", "Le mot de passe est obligatoire."); return null;
        }

        try {
            if (utilisateur == null) {
                if (dao.emailExiste(email)) { AlertUtil.erreur("Email", "Cet email est déjà utilisé."); return null; }
                Utilisateur u = new Utilisateur(nom, prenom, email, mdp, roleBox.getValue());
                u.setStatut("ACTIF");
                u.setTelephone(telephoneField.getText().trim());
                u.setDateNaissance(dateNaissanceField.getText().trim());
                u.setAdresse(adresseField.getText().trim());
                int id = dao.inserer(u); u.setId(id);
                if ("ADHERENT".equals(u.getRole())) {
                    double frais = Double.parseDouble(DatabaseConfig.getConfig("frais_inscription").isEmpty() ? "200" : DatabaseConfig.getConfig("frais_inscription"));
                    paiementDAO.creerOuMettreAJour(id, frais, frais);
                }
                AlertUtil.succes("✅ Succès", "Compte créé avec succès !");
                return u;
            } else {
                if (dao.emailExisteAutreUtilisateur(email, utilisateur.getId())) {
                    AlertUtil.erreur("Email", "Cet email est déjà utilisé."); return null;
                }
                utilisateur.setPrenom(prenom); utilisateur.setNom(nom); utilisateur.setEmail(email);
                utilisateur.setTelephone(telephoneField.getText().trim());
                utilisateur.setDateNaissance(dateNaissanceField.getText().trim());
                utilisateur.setAdresse(adresseField.getText().trim());
                utilisateur.setRole(roleBox.getValue());
                dao.modifier(utilisateur);
                // Changer statut si modifié
                String nouveauStatut = statutBox.getValue();
                if (!nouveauStatut.equals(utilisateur.getStatut())) {
                    switch (nouveauStatut) {
                        case "ACTIF" -> dao.validerCompte(utilisateur.getId());
                        case "BLOQUE" -> dao.bloquerCompte(utilisateur.getId());
                        case "ARCHIVE" -> dao.archiverAdherent(utilisateur.getId());
                    }
                    utilisateur.setStatut(nouveauStatut);
                }
                if (!mdp.isEmpty()) dao.changerMotDePasse(utilisateur.getId(), mdp);
                AlertUtil.succes("✅ Succès", "Informations modifiées avec succès !");
                return utilisateur;
            }
        } catch (SQLException e) { AlertUtil.erreur("Erreur BDD", e.getMessage()); return null; }
    }

    private void styleBtn(javafx.scene.Node btn, String color) {
        btn.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:8;-fx-padding:9 22;-fx-font-size:13px;");
    }
}
