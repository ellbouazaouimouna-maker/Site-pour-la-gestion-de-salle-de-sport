package com.club.dao;

import com.club.config.DatabaseConfig;
import com.club.model.Paiement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementDAO {

    public Paiement getPaiementParAdherent(int adherentId) throws SQLException {
        String sql = """
            SELECT p.*, (u.prenom || ' ' || u.nom) as adherent_nom
            FROM paiements p
            JOIN utilisateurs u ON p.adherent_id = u.id
            WHERE p.adherent_id = ? AND p.annee = EXTRACT(YEAR FROM CURRENT_DATE)
            ORDER BY p.id DESC LIMIT 1
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adherentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        }
        return null;
    }

    public List<Paiement> getTousLesPaiements() throws SQLException {
        List<Paiement> liste = new ArrayList<>();
        String sql = """
            SELECT p.*, (u.prenom || ' ' || u.nom) as adherent_nom
            FROM paiements p
            JOIN utilisateurs u ON p.adherent_id = u.id
            ORDER BY p.annee DESC, adherent_nom
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) liste.add(mapResultSet(rs));
        }
        return liste;
    }

    public void creerOuMettreAJour(int adherentId, double montantTotal, double fraisInscription) throws SQLException {
        Paiement existing = getPaiementParAdherent(adherentId);
        if (existing == null) {
            String sql = "INSERT INTO paiements (adherent_id, montant_total, frais_inscription, statut) VALUES (?,?,?,'EN_ATTENTE')";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, adherentId);
                ps.setDouble(2, montantTotal);
                ps.setDouble(3, fraisInscription);
                ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE paiements SET montant_total=?, frais_inscription=? WHERE id=?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, montantTotal);
                ps.setDouble(2, fraisInscription);
                ps.setInt(3, existing.getId());
                ps.executeUpdate();
            }
        }
    }

    public void enregistrerPaiement(int adherentId, double montantPaye, String notes) throws SQLException {
        Paiement p = getPaiementParAdherent(adherentId);
        if (p == null) return;
        double total = p.getMontantPaye() + montantPaye;
        String statut = total >= p.getMontantTotal() ? "PAYE" : "PARTIEL";
        String sql = """
            UPDATE paiements SET montant_paye=?, statut=?, date_paiement=CURRENT_DATE, notes=? WHERE id=?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, total);
            ps.setString(2, statut);
            ps.setString(3, notes);
            ps.setInt(4, p.getId());
            ps.executeUpdate();
        }
    }

    public double getTotalPaiementsRecus() throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant_paye),0) FROM paiements WHERE annee=EXTRACT(YEAR FROM CURRENT_DATE)";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public int countPaiementsEnAttente() throws SQLException {
        String sql = "SELECT COUNT(*) FROM paiements WHERE statut != 'PAYE' AND annee=EXTRACT(YEAR FROM CURRENT_DATE)";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Paiement mapResultSet(ResultSet rs) throws SQLException {
        Paiement p = new Paiement();
        p.setId(rs.getInt("id"));
        p.setAdherentId(rs.getInt("adherent_id"));
        p.setAdherentNom(rs.getString("adherent_nom"));
        p.setMontantTotal(rs.getDouble("montant_total"));
        p.setMontantPaye(rs.getDouble("montant_paye"));
        p.setFraisInscription(rs.getDouble("frais_inscription"));
        p.setStatut(rs.getString("statut"));
        p.setDatePaiement(rs.getString("date_paiement"));
        p.setAnnee(rs.getInt("annee"));
        p.setNotes(rs.getString("notes"));
        return p;
    }
}
