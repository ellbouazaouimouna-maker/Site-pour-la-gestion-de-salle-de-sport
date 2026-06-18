package com.club.controller.admin;

import com.club.dao.UtilisateurDAO;
import com.club.model.Utilisateur;
import com.club.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ArchivesController {
    @FXML private TableView<Utilisateur> archivesTable;
    @FXML private TableColumn<Utilisateur,String> colNom, colEmail, colTel, colDate;
    @FXML private TableColumn<Utilisateur,Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private final UtilisateurDAO dao = new UtilisateurDAO();
    private ObservableList<Utilisateur> allArchives = FXCollections.observableArrayList();

    @FXML public void initialize() {
        colNom.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNomComplet()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnRestaurer = new Button(" Restaurer");
            private final Button btnVoir = new Button("Détails");
            private final HBox box = new HBox(8, btnVoir, btnRestaurer);
            {
                box.setAlignment(Pos.CENTER);
                btnRestaurer.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:5 12;");
                btnVoir.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:5 12;");
                btnRestaurer.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    if (AlertUtil.confirmer("Restaurer", "Restaurer le compte de " + u.getNomComplet() + " ?\nIl pourra se reconnecter.")) {
                        try { dao.restaurerAdherent(u.getId()); loadData(); AlertUtil.succes("✅ Restauré", u.getNomComplet() + " a été restauré avec succès."); }
                        catch (SQLException ex) { ex.printStackTrace(); }
                    }
                });
                btnVoir.setOnAction(e -> {
                    Utilisateur u = getTableView().getItems().get(getIndex());
                    showDetail(u);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : box);
            }
        });

     archivesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadData();
    }

    private void showDetail(Utilisateur u) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails — " + u.getNomComplet());
        alert.setHeaderText(u.getNomComplet());
        alert.setContentText(
            "Email : " + u.getEmail() + "\n" +
            "Téléphone : " + (u.getTelephone() != null ? u.getTelephone() : "—") + "\n" +
            "Date naissance : " + (u.getDateNaissance() != null ? u.getDateNaissance() : "—") + "\n" +
            "Adresse : " + (u.getAdresse() != null ? u.getAdresse() : "—") + "\n" +
            "Inscrit le : " + (u.getDateInscription() != null ? u.getDateInscription() : "—")
        );
        alert.showAndWait();
    }

    @FXML private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        if (q.isEmpty()) { archivesTable.setItems(allArchives); countLabel.setText(allArchives.size() + " archivé(s)"); return; }
        List<Utilisateur> filtered = allArchives.stream()
            .filter(u -> u.getNomComplet().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q)
                    || (u.getTelephone() != null && u.getTelephone().contains(q)))
            .collect(Collectors.toList());
        archivesTable.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText(filtered.size() + " / " + allArchives.size() + " archivé(s)");
    }

    private void loadData() {
        try {
            List<Utilisateur> archives = dao.getTousLesAdherentsAvecArchives().stream()
                .filter(Utilisateur::isArchive).collect(Collectors.toList());
            allArchives = FXCollections.observableArrayList(archives);
            archivesTable.setItems(allArchives);
            countLabel.setText(archives.size() + " archivé(s)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleRefresh() { loadData(); searchField.clear(); }
}
