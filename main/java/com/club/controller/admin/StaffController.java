package com.club.controller.admin;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.club.dao.UtilisateurDAO;
import com.club.model.Utilisateur;
import com.club.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class StaffController {
    @FXML private TableView<Utilisateur> staffTable;
    @FXML private TableColumn<Utilisateur,String> colNom, colEmail, colRole, colStatut;
    @FXML private TableColumn<Utilisateur,Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private final UtilisateurDAO dao = new UtilisateurDAO();
    private ObservableList<Utilisateur> all = FXCollections.observableArrayList();

    @FXML public void initialize() {
        colNom.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNomComplet()));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); return; }
                setText("ADMIN".equals(s) ? " Administrateur" : "🔧 Assistant");
                setStyle("ADMIN".equals(s) ? "-fx-text-fill:#9b59b6;-fx-font-weight:bold;" : "-fx-text-fill:#3498db;");
            }
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); return; }
                setText("ACTIF".equals(s) ? "✅ Actif" : "⛔ Bloqué");
                setStyle("ACTIF".equals(s) ? "-fx-text-fill:#27ae60;" : "-fx-text-fill:#e74c3c;");
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit  = btn("Modifier", "#3498db");
            private final Button btnBlock = btn("🔒 Bloquer",  "#e67e22");
            private final HBox box = new HBox(6, btnEdit, btnBlock);
            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> {
                    Utilisateur u = staffTable.getItems().get(getIndex());
                    new AdherentFormDialog(u).showAndWait(); loadData();
                });
                btnBlock.setOnAction(e -> toggleBlock());
            }

            private void toggleBlock() {
                Utilisateur u = staffTable.getItems().get(getIndex());
                try {
                    if (u.isBloque()) {
                        if (AlertUtil.confirmer("Débloquer", "Débloquer " + u.getNomComplet() + " ?")) {
                            dao.debloquerCompte(u.getId()); loadData();
                        }
                    } else {
                        if (AlertUtil.confirmer("Bloquer", "Bloquer " + u.getNomComplet() + " ?")) {
                            dao.bloquerCompte(u.getId()); loadData();
                        }
                    }
                } catch (SQLException ex) { ex.printStackTrace(); }
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Utilisateur u = staffTable.getItems().get(getIndex());
                btnBlock.setText(u.isBloque() ? "🔓 Débloquer" : "🔒 Bloquer");
                setGraphic(box);
            }
        });

       staffTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadData();
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:5;-fx-padding:4 10;-fx-font-size:12px;");
        return b;
    }

    @FXML private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        if (q.isEmpty()) { staffTable.setItems(all); countLabel.setText(all.size() + " membre(s)"); return; }
        List<Utilisateur> f = all.stream()
            .filter(u -> u.getNomComplet().toLowerCase().contains(q) || u.getEmail().toLowerCase().contains(q))
            .collect(Collectors.toList());
        staffTable.setItems(FXCollections.observableArrayList(f));
        countLabel.setText(f.size() + " / " + all.size() + " membre(s)");
    }

    @FXML private void handleAdd()     { new AdherentFormDialog(null).showAndWait(); loadData(); }
    @FXML private void handleRefresh() { loadData(); searchField.clear(); }

    private void loadData() {
        try {
            all = FXCollections.observableArrayList(dao.getTousLesStaff());
            staffTable.setItems(all);
            countLabel.setText(all.size() + " membre(s)");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
