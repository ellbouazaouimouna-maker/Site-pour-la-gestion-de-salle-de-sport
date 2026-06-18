package com.club.dao;

import com.club.config.DatabaseConfig;
import com.club.model.Activite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteDAO {

    public List<Activite> getToutesLesActivites() throws SQLException {
        List<Activite> liste = new ArrayList<>();
        String sql = "SELECT * FROM activites ORDER BY nom";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) liste.add(mapResultSet(rs));
        }
        return liste;
    }

    public List<Activite> getActivitesActives() throws SQLException {
        List<Activite> liste = new ArrayList<>();
        String sql = "SELECT * FROM activites WHERE statut='ACTIVE' ORDER BY nom";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) liste.add(mapResultSet(rs));
        }
        return liste;
    }

    public List<Activite> getActivitesParAdherent(int adherentId) throws SQLException {
        List<Activite> liste = new ArrayList<>();
        String sql = """
            SELECT a.* FROM activites a
            INNER JOIN inscriptions i ON a.id = i.activite_id
            WHERE i.adherent_id = ?
            ORDER BY a.nom
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adherentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSet(rs));
        }
        return liste;
    }

    public int inserer(Activite a) throws SQLException {
        String sql = "INSERT INTO activites (nom, description, tarif, horaire, jour, responsable) VALUES (?,?,?,?,?,?) RETURNING id";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setDouble(3, a.getTarif());
            ps.setString(4, a.getHoraire());
            ps.setString(5, a.getJour());
            ps.setString(6, a.getResponsable());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public void modifier(Activite a) throws SQLException {
        String sql = "UPDATE activites SET nom=?, description=?, tarif=?, horaire=?, jour=?, responsable=?, statut=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getDescription());
            ps.setDouble(3, a.getTarif());
            ps.setString(4, a.getHoraire());
            ps.setString(5, a.getJour());
            ps.setString(6, a.getResponsable());
            ps.setString(7, a.getStatut());
            ps.setInt(8, a.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        // Supprimer inscriptions liées
        String sqlI = "DELETE FROM inscriptions WHERE activite_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlI)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        String sql = "DELETE FROM activites WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void inscrireAdherent(int adherentId, int activiteId) throws SQLException {
        String sql = "INSERT INTO inscriptions (adherent_id, activite_id) VALUES (?,?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adherentId);
            ps.setInt(2, activiteId);
            ps.executeUpdate();
        }
    }

    public void desinscrireAdherent(int adherentId, int activiteId) throws SQLException {
        String sql = "DELETE FROM inscriptions WHERE adherent_id=? AND activite_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adherentId);
            ps.setInt(2, activiteId);
            ps.executeUpdate();
        }
    }

    public List<Integer> getActiviteIdsParAdherent(int adherentId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT activite_id FROM inscriptions WHERE adherent_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adherentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("activite_id"));
        }
        return ids;
    }

    public int countActivites() throws SQLException {
        String sql = "SELECT COUNT(*) FROM activites WHERE statut='ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Activite mapResultSet(ResultSet rs) throws SQLException {
        Activite a = new Activite();
        a.setId(rs.getInt("id"));
        a.setNom(rs.getString("nom"));
        a.setDescription(rs.getString("description"));
        a.setTarif(rs.getDouble("tarif"));
        a.setHoraire(rs.getString("horaire"));
        a.setJour(rs.getString("jour"));
        a.setResponsable(rs.getString("responsable"));
        a.setStatut(rs.getString("statut"));
        return a;
    }
}
