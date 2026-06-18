package com.club.model;

public class Activite {
    private int id;
    private String nom;
    private String description;
    private double tarif;
    private String horaire;
    private String jour;
    private String responsable;
    private String statut;

    public Activite() {}

    public Activite(String nom, String description, double tarif, String horaire, String jour, String responsable) {
        this.nom = nom;
        this.description = description;
        this.tarif = tarif;
        this.horaire = horaire;
        this.jour = jour;
        this.responsable = responsable;
        this.statut = "ACTIVE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getTarif() { return tarif; }
    public void setTarif(double tarif) { this.tarif = tarif; }
    public String getHoraire() { return horaire; }
    public void setHoraire(String horaire) { this.horaire = horaire; }
    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getTarifFormate() {
        return String.format("%.2f DH", tarif);
    }

    @Override
    public String toString() { return nom; }
}
