package com.club.controller.adherent;

import com.club.dao.ActiviteDAO;
import com.club.model.Activite;
import com.club.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.util.List;

public class ActivitesAdherentController {
    @FXML private FlowPane activitesPane;
    @FXML private Label countLabel, emptyLabel;

    private final ActiviteDAO dao = new ActiviteDAO();

    @FXML public void initialize() {
        try {
            int id = Session.getInstance().getUtilisateur().getId();
            List<Activite> activites = dao.getActivitesParAdherent(id);
            countLabel.setText(activites.size() + " activité(s) inscrite(s)");
            if (activites.isEmpty()) {
                emptyLabel.setVisible(true); emptyLabel.setManaged(true);
            } else {
                for (Activite a : activites) activitesPane.getChildren().add(buildCard(a));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private VBox buildCard(Activite a) {
        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
            -fx-padding: 20;
            -fx-min-width: 240;
            -fx-max-width: 280;
        """);

        String[] emojis = {"⚽","🏊","🏋️","🎾","🏀","🏐","🤸","🥊","🏃","🚴"};
        String emoji = emojis[Math.abs(a.getNom().hashCode()) % emojis.length];

        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 36px;");

        Label nomLabel = new Label(a.getNom());
        nomLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
        nomLabel.setWrapText(true);

        Label tarifLabel = new Label("💰 " + a.getTarifFormate() + " / an");
        tarifLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        String jourHoraire = "";
        if (a.getJour() != null && !a.getJour().isEmpty()) jourHoraire += "📅 " + a.getJour();
        if (a.getHoraire() != null && !a.getHoraire().isEmpty()) jourHoraire += "  🕐 " + a.getHoraire();
        Label planningLabel = new Label(jourHoraire.isEmpty() ? "Horaire à confirmer" : jourHoraire);
        planningLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #636e72;");
        planningLabel.setWrapText(true);

        if (a.getResponsable() != null && !a.getResponsable().isEmpty()) {
            Label respLabel = new Label("👨‍🏫 " + a.getResponsable());
            respLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #636e72;");
            card.getChildren().addAll(iconLabel, nomLabel, tarifLabel, planningLabel, respLabel);
        } else {
            card.getChildren().addAll(iconLabel, nomLabel, tarifLabel, planningLabel);
        }

        Label badge = new Label("✅ Inscrit");
        badge.setStyle("-fx-background-color: #e8f8f5; -fx-text-fill: #27ae60; -fx-font-size:11px; -fx-font-weight:bold; -fx-padding:4 10; -fx-background-radius:20;");
        card.getChildren().add(badge);
        return card;
    }
}
