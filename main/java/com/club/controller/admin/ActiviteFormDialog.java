package com.club.controller.admin;

import com.club.dao.ActiviteDAO;
import com.club.model.Activite;
import com.club.util.AlertUtil;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import java.sql.SQLException;

public class ActiviteFormDialog extends Dialog<Activite> {

    private final TextField nomField = new TextField();
    private final TextArea descField = new TextArea();
    private final TextField tarifField = new TextField();
    private final TextField horaireField = new TextField();
    private final ComboBox<String> jourBox = new ComboBox<>();
    private final TextField responsableField = new TextField();
    private final ComboBox<String> statutBox = new ComboBox<>();

    private final ActiviteDAO dao = new ActiviteDAO();
    private final Activite activite;

    public ActiviteFormDialog(Activite a) {
        this.activite = a;
        initModality(Modality.APPLICATION_MODAL);
        setTitle(a == null ? "Nouvelle activité" : "Modifier l'activité");
        getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        buildUI();
        if (a != null) fillForm(a);

        ButtonType saveBtn = new ButtonType(a == null ? "Créer" : "Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);
        getDialogPane().lookupButton(saveBtn).setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:8 20;");
        getDialogPane().lookupButton(cancelBtn).setStyle("-fx-background-color:#95a5a6;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:6;-fx-padding:8 20;");

        setResultConverter(btn -> { if (btn == saveBtn) return handleSave(); return null; });
    }

    private void buildUI() {
        jourBox.getItems().addAll("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche");
        statutBox.getItems().addAll("ACTIVE","INACTIVE"); statutBox.setValue("ACTIVE");
        descField.setPrefRowCount(3); descField.setWrapText(true);

        String fieldStyle = "-fx-background-radius:6;-fx-border-radius:6;-fx-border-color:#dfe6e9;-fx-background-color:white;-fx-padding:8;-fx-font-size:13px;";
        nomField.setStyle(fieldStyle); tarifField.setStyle(fieldStyle);
        horaireField.setStyle(fieldStyle); responsableField.setStyle(fieldStyle);
        descField.setStyle(fieldStyle);
        String cbStyle = "-fx-background-radius:6;-fx-border-radius:6;-fx-border-color:#dfe6e9;-fx-background-color:white;";
        jourBox.setStyle(cbStyle); jourBox.setMaxWidth(Double.MAX_VALUE);
        statutBox.setStyle(cbStyle); statutBox.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane(); grid.setHgap(16); grid.setVgap(12); grid.setPadding(new Insets(20)); grid.setPrefWidth(460);

        addField(grid, 0, "Nom de l'activité *", nomField);
        addField(grid, 1, "Tarif annuel (DH) *", tarifField);
        addDoubleField(grid, 2, "Jour", jourBox, "Horaire", horaireField);
        addField(grid, 3, "Responsable", responsableField);
        addField(grid, 4, "Description", descField);
        if (activite != null) addField(grid, 5, "Statut", statutBox);

        getDialogPane().setContent(grid);
    }

    private void addField(GridPane g, int row, String label, javafx.scene.Node field) {
        Label l = new Label(label); l.setStyle("-fx-font-size:13px;-fx-text-fill:#2d3436;-fx-font-weight:bold;");
        VBox b = new VBox(4, l, field); GridPane.setHgrow(b, Priority.ALWAYS); GridPane.setColumnSpan(b, 2); g.add(b, 0, row);
    }

    private void addDoubleField(GridPane g, int row, String l1, javafx.scene.Node f1, String l2, javafx.scene.Node f2) {
        Label la = new Label(l1); la.setStyle("-fx-font-size:13px;-fx-text-fill:#2d3436;-fx-font-weight:bold;");
        Label lb = new Label(l2); lb.setStyle("-fx-font-size:13px;-fx-text-fill:#2d3436;-fx-font-weight:bold;");
        VBox b1 = new VBox(4, la, f1); VBox b2 = new VBox(4, lb, f2);
        GridPane.setHgrow(b1, Priority.ALWAYS); GridPane.setHgrow(b2, Priority.ALWAYS);
        g.add(b1, 0, row); g.add(b2, 1, row);
    }

    private void fillForm(Activite a) {
        nomField.setText(a.getNom()); tarifField.setText(String.valueOf(a.getTarif()));
        if (a.getJour() != null) jourBox.setValue(a.getJour());
        if (a.getHoraire() != null) horaireField.setText(a.getHoraire());
        if (a.getResponsable() != null) responsableField.setText(a.getResponsable());
        if (a.getDescription() != null) descField.setText(a.getDescription());
        statutBox.setValue(a.getStatut());
    }

    private Activite handleSave() {
        String nom = nomField.getText().trim(); String tarifStr = tarifField.getText().trim();
        if (nom.isEmpty() || tarifStr.isEmpty()) { AlertUtil.erreur("Validation", "Nom et Tarif sont obligatoires."); return null; }
        double tarif;
        try { tarif = Double.parseDouble(tarifStr.replace(",",".")); }
        catch (NumberFormatException e) { AlertUtil.erreur("Validation", "Le tarif doit être un nombre valide."); return null; }

        try {
            if (activite == null) {
                Activite a = new Activite(nom, descField.getText().trim(), tarif, horaireField.getText().trim(), jourBox.getValue(), responsableField.getText().trim());
                dao.inserer(a); AlertUtil.succes("Succès", "Activité créée !"); return a;
            } else {
                activite.setNom(nom); activite.setTarif(tarif); activite.setDescription(descField.getText().trim());
                activite.setHoraire(horaireField.getText().trim()); activite.setJour(jourBox.getValue());
                activite.setResponsable(responsableField.getText().trim()); activite.setStatut(statutBox.getValue());
                dao.modifier(activite); AlertUtil.succes("Succès", "Activité modifiée !"); return activite;
            }
        } catch (SQLException e) { AlertUtil.erreur("Erreur", e.getMessage()); return null; }
    }
}
