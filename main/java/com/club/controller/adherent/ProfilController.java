package com.club.controller.adherent;

import com.club.model.Utilisateur;
import com.club.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfilController {
    @FXML private Label welcomeLabel, avatarBig, nameLabel, roleLabel;
    @FXML private Label prenomLabel, nomLabel, emailLabel, telLabel, dateNaissLabel, adresseLabel, dateInscriptionLabel;

    @FXML public void initialize() {
        Utilisateur u = Session.getInstance().getUtilisateur();
        welcomeLabel.setText("Bonjour, " + u.getPrenom() + " 👋");
        avatarBig.setText(u.getPrenom().substring(0,1).toUpperCase());
        nameLabel.setText(u.getNomComplet());
        roleLabel.setText("Adhérent");
        prenomLabel.setText(u.getPrenom());
        nomLabel.setText(u.getNom());
        emailLabel.setText(u.getEmail());
        telLabel.setText(u.getTelephone() != null ? u.getTelephone() : "—");
        dateNaissLabel.setText(u.getDateNaissance() != null ? u.getDateNaissance() : "—");
        adresseLabel.setText(u.getAdresse() != null ? u.getAdresse() : "—");
        dateInscriptionLabel.setText(u.getDateInscription() != null ? u.getDateInscription() : "—");
    }
}
