package com.club.dao;

import com.club.config.DatabaseConfig;
import com.club.model.Utilisateur;

import java.sql.*;
import java.util.*;

/**
 * DAO optimisé — charge toutes les données adhérents
 * en UNE SEULE requête au lieu de N requêtes séparées.
 */
public class AdherentRapportDAO {

    /**
     * Retourne une map : adherent_id → liste des noms d'activités
     * Une seule requête pour TOUS les adhérents.
     */
    public Map<Integer, String> getActivitesParAdherents(List<Integer> ids) throws SQLException {
        if (ids.isEmpty()) return new HashMap<>();

        String inClause = ids.stream()
            .map(String::valueOf)
            .reduce((a, b) -> a + "," + b).orElse("0");

        String sql = "SELECT i.adherent_id, a.nom FROM inscriptions i " +
                     "JOIN activites a ON a.id = i.activite_id " +
                     "WHERE i.adherent_id IN (" + inClause + ")";

        Map<Integer, List<String>> map = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                int aid = rs.getInt("adherent_id");
                map.computeIfAbsent(aid, k -> new ArrayList<>()).add(rs.getString("nom"));
            }
        }

        // Convertir en String
        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<Integer, List<String>> e : map.entrySet()) {
            result.put(e.getKey(), String.join(", ", e.getValue()));
        }
        return result;
    }

    /**
     * Retourne une map : adherent_id → statut paiement
     * Une seule requête pour TOUS les adhérents.
     */
    public Map<Integer, String> getPaiementsParAdherents(List<Integer> ids) throws SQLException {
        if (ids.isEmpty()) return new HashMap<>();

        String inClause = ids.stream()
            .map(String::valueOf)
            .reduce((a, b) -> a + "," + b).orElse("0");

        String sql = "SELECT adherent_id, statut FROM paiements " +
                     "WHERE adherent_id IN (" + inClause + ") " +
                     "AND annee = EXTRACT(YEAR FROM CURRENT_DATE)";

        Map<Integer, String> result = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getInt("adherent_id"), rs.getString("statut"));
            }
        }
        return result;
    }
}
