package com.club.controller.admin;

import com.club.dao.ActiviteDAO;
import com.club.dao.PaiementDAO;
import com.club.model.Activite;
import com.club.model.Utilisateur;
import com.club.util.AlertUtil;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import java.sql.SQLException;
import java.util.List;

public class AdherentActivitesDialog extends Dialog<Void> {

    private final ActiviteDAO activiteDAO = new ActiviteDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();
    private final Utilisateur adherent;
    private VBox activitesBox;

    public AdherentActivitesDialog(Utilisateur u) {
        this.adherent = u;
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Activités de " + u.getNomComplet());
        setHeaderText("Sélectionnez les activités pour cet adhérent");
        getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        buildUI();
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);
        getDialogPane().lookupButton(saveBtn).setStyle("-fx-background-color:#3498db;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:8 20;");

        setResultConverter(btn -> {
            if (btn == saveBtn) handleSave();
            return null;
        });
    }

    private void buildUI() {
        activitesBox = new VBox(10);
        activitesBox.setPadding(new Insets(16));
        activitesBox.setPrefWidth(420);

        try {
            List<Activite> toutes = activiteDAO.getActivitesActives();
            List<Integer> inscrites = activiteDAO.getActiviteIdsParAdherent(adherent.getId());

            if (toutes.isEmpty()) {
                activitesBox.getChildren().add(new Label("Aucune activité disponible."));
            } else {
                for (Activite a : toutes) {
                    CheckBox cb = new CheckBox();
                    cb.setSelected(inscrites.contains(a.getId()));
                    cb.setUserData(a.getId());

                    HBox row = new HBox(12);
                    row.setStyle("-fx-background-color:white;-fx-background-radius:8;-fx-padding:10 14;-fx-border-color:#dfe6e9;-fx-border-radius:8;");
                    row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    VBox info = new VBox(2);
                    Label nom = new Label(a.getNom()); nom.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#2d3436;");
                    Label tarif = new Label(a.getTarifFormate() + " / an  •  " + (a.getJour() != null ? a.getJour() : "") + " " + (a.getHoraire() != null ? a.getHoraire() : ""));
                    tarif.setStyle("-fx-font-size:12px;-fx-text-fill:#636e72;");
                    info.getChildren().addAll(nom, tarif);

                    Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
                    row.getChildren().addAll(info, spacer, cb);
                    activitesBox.getChildren().add(row);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        ScrollPane scroll = new ScrollPane(activitesBox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        getDialogPane().setContent(scroll);
    }

    private void handleSave() {
        try {
            List<Integer> anciennes = activiteDAO.getActiviteIdsParAdherent(adherent.getId());
            // Désinscrire toutes
            for (int id : anciennes) activiteDAO.desinscrireAdherent(adherent.getId(), id);
            // Réinscrire sélectionnées
            double totalTarifs = 0;
            for (var node : activitesBox.getChildren()) {
                if (node instanceof HBox hbox) {
                    for (var child : hbox.getChildren()) {
                        if (child instanceof CheckBox cb && cb.isSelected()) {
                            int actId = (int) cb.getUserData();
                            activiteDAO.inscrireAdherent(adherent.getId(), actId);
                            List<Activite> all = activiteDAO.getActivitesActives();
                            for (Activite a : all) {
                                if (a.getId() == actId) { totalTarifs += a.getTarif(); break; }
                            }
                        }
                    }
                }
            }
            double frais = 200.0;
            paiementDAO.creerOuMettreAJour(adherent.getId(), frais + totalTarifs, frais);
            AlertUtil.succes("Succès", "Activités mises à jour.\nMontant total: " + String.format("%.2f DH", frais + totalTarifs));
        } catch (SQLException e) {
            AlertUtil.erreur("Erreur", "Impossible de mettre à jour les activités."); e.printStackTrace();
        }
    }
}
