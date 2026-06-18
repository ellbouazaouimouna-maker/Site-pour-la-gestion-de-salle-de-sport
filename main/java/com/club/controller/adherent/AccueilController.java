package com.club.controller.adherent;

import com.club.dao.ActiviteDAO;
import com.club.dao.PaiementDAO;
import com.club.model.Activite;
import com.club.model.Paiement;
import com.club.model.Utilisateur;
import com.club.util.Session;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class AccueilController {

    @FXML private Label nbActivitesLabel;
    @FXML private Label joursRestantsLabel;
    @FXML private Label statutPaiementLabel;
    @FXML private Label welcomeTitleLabel;
    @FXML private Label welcomeSubLabel;
    @FXML private VBox activitesContainer;
    @FXML private Label noActivitesLabel;

    private final ActiviteDAO activiteDAO = new ActiviteDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();

    @FXML
    public void initialize() {
        Utilisateur u = Session.getInstance().getUtilisateur();

        // Bienvenue
        welcomeTitleLabel.setText("Bienvenue, " + u.getPrenom() + " !");

        String since = u.getDateInscription() != null ? "Membre depuis " + formatDate(u.getDateInscription()) : "Membre actif";
        welcomeSubLabel.setText(since);

        // Activités de l'adhérent
        try {
            List<Activite> activites = activiteDAO.getActivitesParAdherent(u.getId());
            nbActivitesLabel.setText(String.valueOf(activites.size()));

            if (activites.isEmpty()) {
                noActivitesLabel.setVisible(true);
                noActivitesLabel.setManaged(true);
            } else {
                for (int i = 0; i < activites.size(); i++) {
                    Activite a = activites.get(i);
                    HBox row = createActiviteRow(a);
                    if (i < activites.size() - 1) {
                        row.setStyle(row.getStyle() + "-fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0;");
                    }
                    activitesContainer.getChildren().add(row);
                }
            }
        } catch (Exception e) {
            nbActivitesLabel.setText("—");
        }

        // Paiement
        try {
            Paiement p = paiementDAO.getPaiementParAdherent(u.getId());
            if (p != null) {
                statutPaiementLabel.setText(p.getStatutLabel());
                // Jours restants basé sur l'année en cours
                java.time.LocalDate fin = java.time.LocalDate.of(java.time.LocalDate.now().getYear(), 12, 31);
                long jours = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), fin);
                joursRestantsLabel.setText(String.valueOf(jours));
                welcomeSubLabel.setText(since + " • Abonnement " + getTypeAbonnement(p));
            } else {
                statutPaiementLabel.setText("En attente");
                joursRestantsLabel.setText("—");
            }
        } catch (Exception e) {
            statutPaiementLabel.setText("—");
        }
    }

    private HBox createActiviteRow(Activite a) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 8, 14, 8));
        row.setStyle("-fx-cursor: hand;");

        VBox info = new VBox(3);
        Label nom = new Label(a.getNom());
        nom.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#0f172a;");
        String horaire = (a.getJour() != null ? a.getJour() : "") +
                         (a.getHoraire() != null ? " • " + a.getHoraire() : "");
        Label details = new Label(horaire.isEmpty() ? "—" : horaire);
        details.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b;");
        info.getChildren().addAll(nom, details);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label arrow = new Label("›");
        arrow.setStyle("-fx-font-size:18px; -fx-text-fill:#94a3b8;");

        row.getChildren().addAll(info, spacer, arrow);
        return row;
    }

    private String getTypeAbonnement(Paiement p) {
        double montant = p.getMontantTotal();
        if (montant >= 500) return "Premium";
        if (montant >= 200) return "Standard";
        return "Basic";
    }

    private String formatDate(String date) {
        try {
            String[] parts = date.split("-");
            String[] mois = {"", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                             "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
            return mois[Integer.parseInt(parts[1])] + " " + parts[0];
        } catch (Exception e) {
            return date;
        }
    }
}
