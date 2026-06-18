package com.club.controller.admin;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.club.dao.ActiviteDAO;
import com.club.model.Activite;
import com.club.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class ActivitesController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;
    @FXML private TableView<Activite> activitesTable;
    @FXML private TableColumn<Activite,Integer> colId;
    @FXML private TableColumn<Activite,String> colNom, colDescription, colJour, colHoraire, colResponsable, colStatut;
    @FXML private TableColumn<Activite,Double> colTarif;
    @FXML private TableColumn<Activite,Void> colActions;
    @FXML private Label countLabel;

    private final ActiviteDAO dao = new ActiviteDAO();
    private ObservableList<Activite> all = FXCollections.observableArrayList();

    @FXML public void initialize() {
        filterStatut.getItems().addAll("Toutes","Active","Inactive");
        filterStatut.setValue("Toutes");
        filterStatut.setOnAction(e -> applyFilter());
        activitesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupColumns();
        loadData();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colJour.setCellValueFactory(new PropertyValueFactory<>("jour"));
        colHoraire.setCellValueFactory(new PropertyValueFactory<>("horaire"));
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        colTarif.setCellValueFactory(new PropertyValueFactory<>("tarif"));
        colTarif.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : String.format("%.2f DH", t));
                setStyle("-fx-font-weight:bold;-fx-text-fill:#27ae60;");
            }
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText("ACTIVE".equals(s) ? "Active" : " Inactive");
                setStyle("ACTIVE".equals(s) ? "-fx-text-fill:#27ae60;-fx-font-weight:bold;" : "-fx-text-fill:#e74c3c;");
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = btn("Modifier",   "#3498db");
            private final Button btnToggle = btn("Désactiver", "#e67e22");
            private final Button btnDel    = btn("supprimer",             "#e74c3c");
            private final HBox box = new HBox(5, btnEdit, btnToggle, btnDel);
            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> {
                    Activite a = activitesTable.getItems().get(getIndex());
                    new ActiviteFormDialog(a).showAndWait();
                    loadData();
                });
                btnToggle.setOnAction(e -> toggleStatut());
                btnDel.setOnAction(e -> handleDelete());
            }

            private void toggleStatut() {
                Activite a = activitesTable.getItems().get(getIndex());
                String newStatut = "ACTIVE".equals(a.getStatut()) ? "INACTIVE" : "ACTIVE";
                try { a.setStatut(newStatut); dao.modifier(a); loadData(); }
                catch (SQLException ex) { ex.printStackTrace(); }
            }

            private void handleDelete() {
                Activite a = activitesTable.getItems().get(getIndex());
                if (AlertUtil.confirmer("Supprimer", "Supprimer \"" + a.getNom() + "\" ?\nLes adhérents inscrits seront désinscrits.")) {
                    try { dao.supprimer(a.getId()); loadData(); }
                    catch (SQLException ex) { ex.printStackTrace(); }
                }
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Activite a = activitesTable.getItems().get(getIndex());
                btnToggle.setText("ACTIVE".equals(a.getStatut()) ? "Désactiver" : "Activer");
                setGraphic(box);
            }
        });
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:5;-fx-padding:4 9;-fx-font-size:12px;");
        return b;
    }

    @FXML private void handleSearch() { applyFilter(); }

    private void applyFilter() {
        String q = searchField.getText().toLowerCase().trim();
        String statut = filterStatut.getValue();
        List<Activite> filtered = all.stream()
            .filter(a -> q.isEmpty() || a.getNom().toLowerCase().contains(q)
                    || (a.getResponsable() != null && a.getResponsable().toLowerCase().contains(q)))
            .filter(a -> switch(statut) {
                case "Active"   -> "ACTIVE".equals(a.getStatut());
                case "Inactive" -> "INACTIVE".equals(a.getStatut());
                default -> true;
            })
            .collect(Collectors.toList());
        activitesTable.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText(filtered.size() + " / " + all.size() + " activité(s)");
    }

    private void loadData() {
        try {
            all = FXCollections.observableArrayList(dao.getToutesLesActivites());
            activitesTable.setItems(all);
            countLabel.setText(all.size() + " activité(s)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleAdd()     { new ActiviteFormDialog(null).showAndWait(); loadData(); }
    @FXML private void handleRefresh() { loadData(); searchField.clear(); filterStatut.setValue("Toutes"); }
}
