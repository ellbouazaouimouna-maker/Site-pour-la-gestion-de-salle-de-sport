package com.club.controller.admin;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.club.config.DatabaseConfig;
import com.club.dao.PaiementDAO;
import com.club.dao.UtilisateurDAO;
import com.club.model.Utilisateur;
import com.club.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class EnAttenteController {
    @FXML private TableView<Utilisateur> table;
    @FXML private TableColumn<Utilisateur,String> colNom, colEmail, colTel, colDate;
    @FXML private TableColumn<Utilisateur,Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private final UtilisateurDAO dao = new UtilisateurDAO();
    private final PaiementDAO paiementDAO = new PaiementDAO();
    private ObservableList<Utilisateur> all = FXCollections.observableArrayList();

    @FXML public void initialize() {
        colNom.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNomComplet()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnValider = new Button("✅  Valider");
            private final Button btnRefuser = new Button("❌  Refuser");
            private final Button btnVoir = new Button("Details");
            private final HBox box = new HBox(6, btnVoir, btnValider, btnRefuser);
            {
                box.setAlignment(Pos.CENTER);
                btnValider.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:5 12;-fx-font-size:12px;");
                btnRefuser.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:5 12;-fx-font-size:12px;");
                btnVoir.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:5 10;-fx-font-size:12px;");

                btnValider.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    if (AlertUtil.confirmer("Valider", "Activer le compte de " + u.getNomComplet() + " ?")) {
                        try {
                            dao.validerCompte(u.getId());
                            double frais = Double.parseDouble(DatabaseConfig.getConfig("frais_inscription").isEmpty() ? "200" : DatabaseConfig.getConfig("frais_inscription"));
                            paiementDAO.creerOuMettreAJour(u.getId(), frais, frais);
                            AlertUtil.succes("✅ Validé", u.getNomComplet() + " peut maintenant se connecter.");
                            loadData();
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
                btnRefuser.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    if (AlertUtil.confirmer("Refuser", "Archiver la demande de " + u.getNomComplet() + " ?\nIl ne pourra pas se connecter.")) {
                        try { dao.archiverAdherent(u.getId()); loadData(); }
                        catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
                btnVoir.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Détails"); a.setHeaderText(u.getNomComplet());
                    a.setContentText("Email: " + u.getEmail() + "\nTél: " + (u.getTelephone()!=null?u.getTelephone():"—") +
                        "\nNaissance: " + (u.getDateNaissance()!=null?u.getDateNaissance():"—") +
                        "\nAdresse: " + (u.getAdresse()!=null?u.getAdresse():"—") +
                        "\nDemande le: " + u.getDateInscription());
                    a.showAndWait();
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : box);
            }
        });

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        loadData();
    }

    @FXML private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        if (q.isEmpty()) { table.setItems(all); return; }
        List<Utilisateur> f = all.stream()
            .filter(u -> u.getNomComplet().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q))
            .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(f));
        countLabel.setText(f.size() + " / " + all.size() + " en attente");
    }

    @FXML private void handleValiderTous() {
        if (all.isEmpty()) { AlertUtil.avertissement("Info", "Aucun compte en attente."); return; }
        if (!AlertUtil.confirmer("Valider tous", "Valider les " + all.size() + " compte(s) en attente ?")) return;
        try {
            double frais = Double.parseDouble(DatabaseConfig.getConfig("frais_inscription").isEmpty() ? "200" : DatabaseConfig.getConfig("frais_inscription"));
            for (Utilisateur u : all) {
                dao.validerCompte(u.getId());
                paiementDAO.creerOuMettreAJour(u.getId(), frais, frais);
            }
            AlertUtil.succes("✅ Tous validés", all.size() + " compte(s) activé(s).");
            loadData();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleRefresh() { loadData(); searchField.clear(); }

    private void loadData() {
        try {
            all = FXCollections.observableArrayList(dao.getAdherentsEnAttente());
            table.setItems(all);
            countLabel.setText(all.size() + " en attente");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
