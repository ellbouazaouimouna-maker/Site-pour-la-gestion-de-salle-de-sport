package com.club.controller.admin;

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

public class AdminDashboardController {
    @FXML private Label userRoleLabel, userNameLabel, userEmailLabel, userAvatarLabel;
    @FXML private Label pageTitle, dateLabel;
    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard, btnAdherents, btnActivites, btnPaiements,
                         btnStaff, btnArchives, btnParametres, btnEnAttente;
    private Button activeButton;

    @FXML public void initialize() {
        var user = Session.getInstance().getUtilisateur();
        userNameLabel.setText(user.getNomComplet());
        userEmailLabel.setText(user.getEmail());
        userRoleLabel.setText(user.isAdmin() ? "Administrateur" : "Assistant");
        userAvatarLabel.setText(user.getPrenom().substring(0, 1).toUpperCase());
        dateLabel.setText(LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)));
        // Masquer Paramètres pour les assistants
        if (!user.isAdmin()) {
            btnParametres.setVisible(false);
            btnParametres.setManaged(false);
            btnStaff.setVisible(false);
            btnStaff.setManaged(false);
        }
        activeButton = btnDashboard;
        showDashboard();

       
    }

    @FXML public void showDashboard()  { nav(btnDashboard,  "Tableau de bord",       "/fxml/admin/home.fxml"); }
    @FXML public void showAdherents()  { nav(btnAdherents,  "Gestion des Adhérents", "/fxml/admin/adherents.fxml"); }
    @FXML public void showActivites()  { nav(btnActivites,  "Gestion des Activités", "/fxml/admin/activites.fxml"); }
    @FXML public void showPaiements()  { nav(btnPaiements,  "Gestion des Paiements", "/fxml/admin/paiements.fxml"); }
    @FXML public void showStaff()      { nav(btnStaff,      "Équipe de gestion",     "/fxml/admin/staff.fxml"); }
    @FXML public void showArchives()   { nav(btnArchives,   "Archives",              "/fxml/admin/archives.fxml"); }
    @FXML public void showEnAttente()  { nav(btnEnAttente,  "Comptes en attente",    "/fxml/admin/en_attente.fxml"); }
    @FXML public void showParametres() { nav(btnParametres, "Paramètres",            "/fxml/admin/parametres.fxml"); }

    private void nav(Button btn, String titre, String fxml) {
        setActive(btn);
        pageTitle.setText(titre);
        loadContent(fxml);
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
            Label err = new Label("❌ Impossible de charger : " + fxmlPath + "\n" + e.getMessage());
            err.setStyle("-fx-font-size:13px;-fx-text-fill:#e74c3c;-fx-padding:20;");
            contentArea.getChildren().setAll(err);
        }
    }

    private void setActive(Button btn) {
        if (activeButton != null) activeButton.getStyleClass().remove("nav-active");
        btn.getStyleClass().add("nav-active");
        activeButton = btn;
    }

    @FXML private void handleLogout() {
        if (AlertUtil.confirmer("Déconnexion", "Voulez-vous vraiment vous déconnecter ?")) {
            Session.getInstance().deconnecter();
            NavigationManager.naviguerVers("/fxml/login.fxml", "Connexion");
        }
    }
}
