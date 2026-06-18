package com.club.util;

import com.club.model.Utilisateur;

public class Session {
    private static Session instance;
    private Utilisateur utilisateurConnecte;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public void connecter(Utilisateur u) { this.utilisateurConnecte = u; }
    public void deconnecter() { this.utilisateurConnecte = null; }
    public Utilisateur getUtilisateur() { return utilisateurConnecte; }
    public boolean isConnecte() { return utilisateurConnecte != null; }
    public boolean isAdmin() { return isConnecte() && utilisateurConnecte.hasAdminRights(); }
    public boolean isAdherent() { return isConnecte() && utilisateurConnecte.isAdherent(); }
}
