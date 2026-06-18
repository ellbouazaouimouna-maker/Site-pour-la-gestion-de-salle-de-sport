package com.club.controller.adherent;

import com.club.util.AlertUtil;
import com.club.util.NavigationManager;
import com.club.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AdherentDashboardController {
    @FXML private Label nameLabel, emailLabel, avatarLabel, pageTitle, dateLabel;
    @FXML private StackPane contentArea;
    @FXML private Button btnProfil, btnProfilPage, btnActivites, btnPaiement, btnMdp;
    private Button activeBtn;

    @FXML public void initialize() {
        var u = Session.getInstance().getUtilisateur();
        nameLabel.setText(u.getNomComplet());
        emailLabel.setText(u.getEmail());
        avatarLabel.setText(u.getPrenom().substring(0,1).toUpperCase());
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)));
        activeBtn = btnProfil;
        showAccueil();
    }

    @FXML public void showAccueil()     { nav(btnProfil,     "Tableau de bord",           "/fxml/adherent/accueil.fxml"); }
    @FXML public void showProfil()      { nav(btnProfilPage, "Mon Profil",                "/fxml/adherent/profil.fxml"); }
    @FXML public void showActivites()   { nav(btnActivites, "Mes Activités",             "/fxml/adherent/activites.fxml"); }
    @FXML public void showPaiement()    { nav(btnPaiement,  "Mon Paiement",              "/fxml/adherent/paiement.fxml"); }
    @FXML public void showChangerMdp()  { nav(btnMdp,       "Changer mon mot de passe",  "/fxml/adherent/changer_mdp.fxml"); }

    private void nav(Button b, String titre, String fxml) {
        setActive(b); pageTitle.setText(titre); load(fxml);
    }

    private void load(String path) {
        try {
            Node n = FXMLLoader.load(getClass().getResource(path));
            contentArea.getChildren().setAll(n);
        } catch (IOException e) {
            e.printStackTrace();
            Label err = new Label("❌ Erreur de chargement.");
            err.setStyle("-fx-text-fill:#e74c3c;-fx-font-size:14px;");
            contentArea.getChildren().setAll(err);
        }
    }

    private void setActive(Button b) {
        if (activeBtn != null) activeBtn.getStyleClass().remove("nav-active");
        b.getStyleClass().add("nav-active"); activeBtn = b;
    }

    @FXML private void handleLogout() {
        if (AlertUtil.confirmer("Déconnexion", "Voulez-vous vous déconnecter ?")) {
            Session.getInstance().deconnecter();
            NavigationManager.naviguerVers("/fxml/login.fxml", "Connexion");
        }
    }
}
