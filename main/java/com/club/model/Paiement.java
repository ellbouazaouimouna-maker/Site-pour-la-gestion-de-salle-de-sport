package com.club.model;

public class Paiement {
    private int id;
    private int adherentId;
    private String adherentNom;
    private double montantTotal;
    private double montantPaye;
    private double fraisInscription;
    private String statut; // PAYE, EN_ATTENTE, PARTIEL
    private String datePaiement;
    private int annee;
    private String notes;

    public Paiement() {}

    public double getMontantRestant() {
        return montantTotal - montantPaye;
    }

    public String getStatutLabel() {
        return switch (statut) {
            case "PAYE" -> "✅ Payé";
            case "EN_ATTENTE" -> "⏳ En attente";
            case "PARTIEL" -> "⚠️ Partiel";
            default -> statut;
        };
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAdherentId() { return adherentId; }
    public void setAdherentId(int adherentId) { this.adherentId = adherentId; }
    public String getAdherentNom() { return adherentNom; }
    public void setAdherentNom(String adherentNom) { this.adherentNom = adherentNom; }
    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }
    public double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(double montantPaye) { this.montantPaye = montantPaye; }
    public double getFraisInscription() { return fraisInscription; }
    public void setFraisInscription(double fraisInscription) { this.fraisInscription = fraisInscription; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getDatePaiement() { return datePaiement; }
    public void setDatePaiement(String datePaiement) { this.datePaiement = datePaiement; }
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
