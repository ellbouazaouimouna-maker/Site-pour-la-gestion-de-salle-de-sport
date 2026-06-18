package com.club.model;

import java.time.LocalDate;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String role; // ADMIN, ASSISTANT, ADHERENT
    private String statut; // ACTIF, BLOQUE, ARCHIVE
    private String telephone;
    private String dateNaissance;
    private String adresse;
    private String dateInscription;
    private int tentativesConnexion;
    private String dateBlocage;
    private String photoPath;
    private String numeroAdherent;

    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String email, String motDePasse, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.statut = "ACTIF";
    }

    public String getNumeroAdherent() { return numeroAdherent; }
    public void setNumeroAdherent(String numeroAdherent) { this.numeroAdherent = numeroAdherent; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isAssistant() { return "ASSISTANT".equals(role); }
    public boolean isAdherent() { return "ADHERENT".equals(role); }
    public boolean isActif() { return "ACTIF".equals(statut); }
    public boolean isBloque() { return "BLOQUE".equals(statut); }
    public boolean isArchive() { return "ARCHIVE".equals(statut); }
    public boolean hasAdminRights() { return isAdmin() || isAssistant(); }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getDateInscription() { return dateInscription; }
    public void setDateInscription(String dateInscription) { this.dateInscription = dateInscription; }
    public int getTentativesConnexion() { return tentativesConnexion; }
    public void setTentativesConnexion(int tentativesConnexion) { this.tentativesConnexion = tentativesConnexion; }
    public String getDateBlocage() { return dateBlocage; }
    public void setDateBlocage(String dateBlocage) { this.dateBlocage = dateBlocage; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    @Override
    public String toString() {
        return getNomComplet() + " (" + role + ")";
    }
}
