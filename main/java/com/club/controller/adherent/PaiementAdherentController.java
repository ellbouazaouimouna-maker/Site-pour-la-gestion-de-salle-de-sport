package com.club.controller.adherent;

import com.club.dao.ActiviteDAO;
import com.club.dao.PaiementDAO;
import com.club.model.Activite;
import com.club.model.Paiement;
import com.club.util.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.SQLException;
import java.util.List;

public class PaiementAdherentController {
    @FXML private Label montantTotalLabel, montantPayeLabel, montantRestantLabel;
    @FXML private Label fraisLabel, tarifActivitesLabel, datePaiementLabel, notesLabel;
    @FXML private Label statutBadge, noPaiementLabel;

    private final PaiementDAO paiementDAO = new PaiementDAO();
    private final ActiviteDAO activiteDAO = new ActiviteDAO();

    @FXML public void initialize() {
        int id = Session.getInstance().getUtilisateur().getId();
        try {
            Paiement p = paiementDAO.getPaiementParAdherent(id);
            if (p == null) {
                noPaiementLabel.setVisible(true); noPaiementLabel.setManaged(true);
                montantTotalLabel.setText("—"); montantPayeLabel.setText("—"); montantRestantLabel.setText("—");
                return;
            }
            montantTotalLabel.setText(String.format("%.2f DH", p.getMontantTotal()));
            montantPayeLabel.setText(String.format("%.2f DH", p.getMontantPaye()));
            montantRestantLabel.setText(String.format("%.2f DH", p.getMontantRestant()));
            fraisLabel.setText(String.format("%.2f DH", p.getFraisInscription()));

            double tarifActivites = p.getMontantTotal() - p.getFraisInscription();
            tarifActivitesLabel.setText(String.format("%.2f DH", tarifActivites));
            datePaiementLabel.setText(p.getDatePaiement() != null ? p.getDatePaiement() : "Aucun paiement reçu");
            notesLabel.setText(p.getNotes() != null && !p.getNotes().isEmpty() ? p.getNotes() : "—");

            switch (p.getStatut()) {
                case "PAYE" -> { statutBadge.setText("✅ Payé"); statutBadge.setStyle("-fx-background-color:#e8f8f5;-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-padding:6 16;-fx-background-radius:20;-fx-font-size:13px;"); }
                case "PARTIEL" -> { statutBadge.setText("⚠️ Partiel"); statutBadge.setStyle("-fx-background-color:#fff3cd;-fx-text-fill:#e67e22;-fx-font-weight:bold;-fx-padding:6 16;-fx-background-radius:20;-fx-font-size:13px;"); }
                default -> { statutBadge.setText("⏳ En attente"); statutBadge.setStyle("-fx-background-color:#ffeaa7;-fx-text-fill:#d63031;-fx-font-weight:bold;-fx-padding:6 16;-fx-background-radius:20;-fx-font-size:13px;"); }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
