package com.club.controller.admin;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import com.club.dao.PaiementDAO;
import com.club.model.Paiement;
import com.club.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class PaiementsController {

    @FXML private Label totalEncaisseLabel, enAttenteLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatut;
    @FXML private TableView<Paiement> paiementsTable;
    @FXML private TableColumn<Paiement,String> colAdherent, colStatut, colDate;
    @FXML private TableColumn<Paiement,Double> colFrais, colTotal, colPaye, colRestant;
    @FXML private TableColumn<Paiement,Void> colActions;

    private final PaiementDAO dao = new PaiementDAO();
    private ObservableList<Paiement> all = FXCollections.observableArrayList();

    @FXML public void initialize() {
        filterStatut.getItems().addAll("Tous","PAYE","EN_ATTENTE","PARTIEL");
        filterStatut.setValue("Tous");
      paiementsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns(); loadData();
    }

    private void setupColumns() {
        colAdherent.setCellValueFactory(new PropertyValueFactory<>("adherentNom"));
        colFrais.setCellValueFactory(new PropertyValueFactory<>("fraisInscription"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        colPaye.setCellValueFactory(new PropertyValueFactory<>("montantPaye"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("datePaiement"));

        for (TableColumn<Paiement,Double> col : List.of(colFrais, colTotal, colPaye)) {
            col.setCellFactory(c -> new TableCell<>() {
                @Override protected void updateItem(Double v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? null : String.format("%.2f DH", v));
                }
            });
        }
        colRestant.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setText(null); setStyle(""); return; }
                Paiement p = getTableView().getItems().get(getIndex());
                double r = p.getMontantRestant();
                setText(String.format("%.2f DH", r));
                setStyle(r <= 0 ? "-fx-text-fill:#27ae60;-fx-font-weight:bold;" : "-fx-text-fill:#e74c3c;-fx-font-weight:bold;");
            }
        });
        colStatut.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(switch (s) { case "PAYE" -> "✅ Payé"; case "EN_ATTENTE" -> "⏳ En attente"; default -> "⚠️ Partiel"; });
                setStyle(switch (s) { case "PAYE" -> "-fx-text-fill:#27ae60;-fx-font-weight:bold;"; case "EN_ATTENTE" -> "-fx-text-fill:#e67e22;"; default -> "-fx-text-fill:#e74c3c;"; });
            }
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colActions.setCellFactory(c -> new TableCell<>() {
            private final Button btn = new Button(" Payer");
            { btn.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:4;-fx-padding:4 10;");
              btn.setOnAction(e -> { Paiement p = getTableView().getItems().get(getIndex()); showPaiementDialog(p); }); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Paiement p = getTableView().getItems().get(getIndex());
                btn.setDisable("PAYE".equals(p.getStatut()));
                setGraphic(btn);
            }
        });
    }

    private void showPaiementDialog(Paiement p) {
        Dialog<String> dlg = new Dialog<>();
        dlg.setTitle("Enregistrer un paiement");
        dlg.setHeaderText("Adhérent: " + p.getAdherentNom() + "\nRestant: " + String.format("%.2f DH", p.getMontantRestant()));
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        VBox box = new VBox(12); box.setPadding(new Insets(20)); box.setPrefWidth(340);
        TextField montantField = new TextField(String.format("%.2f", p.getMontantRestant()));
        montantField.setStyle("-fx-background-radius:6;-fx-border-radius:6;-fx-border-color:#dfe6e9;-fx-padding:8;");
        TextField notesField = new TextField(); notesField.setPromptText("Notes (optionnel)");
        notesField.setStyle("-fx-background-radius:6;-fx-border-radius:6;-fx-border-color:#dfe6e9;-fx-padding:8;");
        box.getChildren().addAll(new Label("Montant reçu (DH):"), montantField, new Label("Notes:"), notesField);
        dlg.getDialogPane().setContent(box);

        ButtonType ok = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, cancel);
        dlg.getDialogPane().lookupButton(ok).setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:8 16;");

        dlg.setResultConverter(btn -> btn == ok ? montantField.getText() : null);
        dlg.showAndWait().ifPresent(val -> {
            try {
                double montant = Double.parseDouble(val.replace(",","."));
                dao.enregistrerPaiement(p.getAdherentId(), montant, notesField.getText());
                AlertUtil.succes("Paiement enregistré", "Paiement de " + String.format("%.2f DH", montant) + " enregistré.");
                loadData();
            } catch (NumberFormatException e) { AlertUtil.erreur("Erreur", "Montant invalide.");
            } catch (SQLException e) { AlertUtil.erreur("Erreur", e.getMessage()); }
        });
    }

    private void loadData() {
        try {
            all = FXCollections.observableArrayList(dao.getTousLesPaiements());
            paiementsTable.setItems(all);
            totalEncaisseLabel.setText(String.format("%.0f DH", dao.getTotalPaiementsRecus()));
            enAttenteLabel.setText(String.valueOf(dao.countPaiementsEnAttente()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleSearch() {
        String q = searchField.getText().toLowerCase().trim();
        applyFilter(q, filterStatut.getValue());
    }
    @FXML private void handleFilter() { applyFilter(searchField.getText().toLowerCase().trim(), filterStatut.getValue()); }

    private void applyFilter(String q, String statut) {
        List<Paiement> filtered = all.stream()
            .filter(p -> (q.isEmpty() || p.getAdherentNom().toLowerCase().contains(q)))
            .filter(p -> ("Tous".equals(statut) || p.getStatut().equals(statut)))
            .collect(Collectors.toList());
        paiementsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML private void handleAddPaiement() {
        AlertUtil.avertissement("Info", "Sélectionnez un adhérent dans le tableau et cliquez sur '💳 Payer'.");
    }
    @FXML private void handleRefresh() { loadData(); }
}
