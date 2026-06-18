package com.club.controller.admin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.club.dao.AdherentRapportDAO;
import com.club.dao.UtilisateurDAO;
import com.club.model.Utilisateur;
import com.club.util.AlertUtil;
import com.club.util.EmailService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class AdherentsController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;
    @FXML private TableView<Utilisateur> adherentsTable;
    @FXML private TableColumn<Utilisateur,String> colId;
    @FXML private TableColumn<Utilisateur,String> colNom, colEmail, colTel, colActivites, colPaiement, colStatut;
    @FXML private TableColumn<Utilisateur,Void> colActions;
    @FXML private Label countLabel;
    @FXML private StackPane loadingPane;

    private final UtilisateurDAO userDAO         = new UtilisateurDAO();
    private final AdherentRapportDAO rapportDAO  = new AdherentRapportDAO();
    private ObservableList<Utilisateur> allAdherents = FXCollections.observableArrayList();

    // Cache des données
    private Map<Integer, String> activitesCache  = new HashMap<>();
    private Map<Integer, String> paiementsCache  = new HashMap<>();

    @FXML public void initialize() {
        filterStatut.getItems().addAll("Tous","Actif","Bloqué","En attente");
        filterStatut.setValue("Tous");
        filterStatut.setOnAction(e -> applyFilter());
        adherentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getNumeroAdherent() != null ? d.getValue().getNumeroAdherent() : "—"));
        colNom.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNomComplet()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Activités depuis cache
        colActivites.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setText(null); return; }
                Utilisateur u = getTableView().getItems().get(getIndex());
                String val = activitesCache.getOrDefault(u.getId(), "—");
                setText(val.isEmpty() ? "—" : val);
                setStyle(val.isEmpty() || val.equals("—") ? "-fx-text-fill:#95a5a6;" : "-fx-text-fill:#2d3436;");
            }
        });

        // Paiements depuis cache
        colPaiement.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setText(null); setStyle(""); return; }
                Utilisateur u = getTableView().getItems().get(getIndex());
                String statut = paiementsCache.get(u.getId());
                if (statut == null) { setText("—"); setStyle(""); return; }
                setText(switch(statut) {
                    case "PAYE"    -> "✅ Payé";
                    case "PARTIEL" -> "⚠️ Partiel";
                    default        -> "⏳ En attente";
                });
                setStyle(switch(statut) {
                    case "PAYE"    -> "-fx-text-fill:#27ae60;-fx-font-weight:bold;";
                    case "PARTIEL" -> "-fx-text-fill:#e67e22;";
                    default        -> "-fx-text-fill:#e74c3c;";
                });
            }
        });

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setText(null); setStyle(""); return; }
                Utilisateur u = getTableView().getItems().get(getIndex());
                switch (u.getStatut()) {
                    case "ACTIF"      -> { setText("✅ Actif");      setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;"); }
                    case "BLOQUE"     -> { setText("⛔ Bloqué");     setStyle("-fx-text-fill:#e74c3c;-fx-font-weight:bold;"); }
                    case "EN_ATTENTE" -> { setText("⏳ En attente"); setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;"); }
                    default           -> { setText("📦 Archivé");    setStyle("-fx-text-fill:#95a5a6;"); }
                }
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit    = btn("modifier", "#3498db");
            private final Button btnActs    = btn("⚽", "#9b59b6");
            private final Button btnBlock   = btn("🔒", "#e67e22");
            private final Button btnValider = btn("✅", "#27ae60");
            private final Button btnArchive = btn("📦", "#7f8c8d");
            private final HBox box = new HBox(4, btnEdit, btnActs, btnBlock, btnValider, btnArchive);
            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> { Utilisateur u = getTableView().getItems().get(getIndex()); new AdherentFormDialog(u).showAndWait(); loadData(); });
                btnActs.setOnAction(e -> { Utilisateur u = getTableView().getItems().get(getIndex()); new AdherentActivitesDialog(u).showAndWait(); loadData(); });
                btnBlock.setOnAction(e -> toggleBlock());
                btnValider.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    if (AlertUtil.confirmer("Valider", "Activer le compte de " + u.getNomComplet() + " ?")) {
                        try { userDAO.validerCompte(u.getId()); EmailService.sendCompteValidé(u.getEmail(), u.getPrenom()); loadData(); }
                        catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
                btnArchive.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    if (AlertUtil.confirmer("Archiver", "Archiver " + u.getNomComplet() + " ?")) {
                        try { userDAO.archiverAdherent(u.getId()); loadData(); }
                        catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
            }

            private void toggleBlock() {
                Utilisateur u = getTableView().getItems().get(getIndex());
                try {
                    if (u.isBloque()) {
                        if (AlertUtil.confirmer("Débloquer", "Débloquer " + u.getNomComplet() + " ?")) {
                            userDAO.debloquerCompte(u.getId());
                            EmailService.sendCompteDébloqué(u.getEmail(), u.getPrenom());
                            loadData();
                        }
                    } else {
                        if (AlertUtil.confirmer("Bloquer", "Bloquer " + u.getNomComplet() + " ?\nIl ne pourra plus se connecter.")) {
                            userDAO.bloquerCompte(u.getId());
                            EmailService.sendCompteBloquéAdmin(u.getEmail(), u.getPrenom());
                            loadData();
                        }
                    }
                } catch (SQLException ex) { ex.printStackTrace(); }
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Utilisateur u = getTableView().getItems().get(getIndex());
                btnBlock.setText(u.isBloque() ? "🔓" : "🔒");
                btnValider.setVisible("EN_ATTENTE".equals(u.getStatut()));
                btnValider.setManaged("EN_ATTENTE".equals(u.getStatut()));
                setGraphic(box);
            }
        });
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:5;-fx-padding:4 8;-fx-font-size:12px;");
        return b;
    }

    @FXML private void handleSearch() { applyFilter(); }

    private void applyFilter() {
        String q = searchField.getText().toLowerCase().trim();
        String statut = filterStatut.getValue();
        List<Utilisateur> filtered = allAdherents.stream()
            .filter(u -> q.isEmpty()
                || u.getNomComplet().toLowerCase().contains(q)
                || u.getEmail().toLowerCase().contains(q)
                || (u.getTelephone() != null && u.getTelephone().contains(q)))
            .filter(u -> switch(statut) {
                case "Actif"      -> "ACTIF".equals(u.getStatut());
                case "Bloqué"     -> "BLOQUE".equals(u.getStatut());
                case "En attente" -> "EN_ATTENTE".equals(u.getStatut());
                default -> true;
            })
            .collect(Collectors.toList());
        adherentsTable.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText(filtered.size() + " / " + allAdherents.size() + " adhérent(s)");
    }

    // ── Chargement en arrière-plan ────────────────────
    private void loadData() {
        if (loadingPane != null) { loadingPane.setVisible(true); }
        adherentsTable.setDisable(true);

        Task<Void> task = new Task<>() {
            List<Utilisateur> adherents;
            Map<Integer, String> activites;
            Map<Integer, String> paiements;

            @Override protected Void call() throws Exception {
                // 1. Charger les adhérents
                adherents = userDAO.getTousLesAdherents();

                // 2. Récupérer tous les IDs
                List<Integer> ids = adherents.stream().map(Utilisateur::getId).collect(Collectors.toList());

                // 3. Une seule requête pour activités + paiements
                activites = rapportDAO.getActivitesParAdherents(ids);
                paiements = rapportDAO.getPaiementsParAdherents(ids);
                return null;
            }

            @Override protected void succeeded() {
                activitesCache  = activites;
                paiementsCache  = paiements;
                allAdherents = FXCollections.observableArrayList(adherents);
                adherentsTable.setItems(allAdherents);
                countLabel.setText(allAdherents.size() + " adhérent(s)");
                adherentsTable.setDisable(false);
                if (loadingPane != null) loadingPane.setVisible(false);
                applyFilter();
            }

            @Override protected void failed() {
                adherentsTable.setDisable(false);
                if (loadingPane != null) loadingPane.setVisible(false);
                getException().printStackTrace();
            }
        };

        new Thread(task).start();
    }

    @FXML private void handleAdd()     { new AdherentFormDialog(null).showAndWait(); loadData(); }
    @FXML private void handleRefresh() { loadData(); searchField.clear(); filterStatut.setValue("Tous"); }
}
