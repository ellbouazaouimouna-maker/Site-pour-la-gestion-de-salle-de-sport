package com.club.dao;

import com.club.config.DatabaseConfig;
import com.club.model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Utilisateur authentifier(String email, String motDePasse) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Utilisateur u = mapResultSet(rs);

            // Compte archivé
            if ("ARCHIVE".equals(u.getStatut())) throw new SecurityException("ARCHIVE");

            // Compte bloqué : vérifier si 30 min écoulées
            if ("BLOQUE".equals(u.getStatut()) && u.getDateBlocage() != null) {
                LocalDateTime deblocage = LocalDateTime.parse(u.getDateBlocage(), FMT).plusMinutes(30);
                if (LocalDateTime.now().isBefore(deblocage)) {
                    throw new SecurityException("BLOQUE_TEMP:" + deblocage.format(FMT));
                } else {
                    debloquerApresExpiration(u.getId());
                    u.setStatut("ACTIF");
                    u.setTentativesConnexion(0);
                }
            }
            // Bloqué manuellement (sans date = blocage permanent admin)
            if ("BLOQUE".equals(u.getStatut()) && u.getDateBlocage() == null) {
                throw new SecurityException("BLOQUE_ADMIN");
            }

            // Compte en attente de validation
            if ("EN_ATTENTE".equals(u.getStatut())) throw new SecurityException("EN_ATTENTE");

            // Vérification mot de passe
            if (BCrypt.checkpw(motDePasse, u.getMotDePasse())) {
                resetTentatives(u.getId());
                return u;
            } else {
                int nouvellesTentatives = u.getTentativesConnexion() + 1;
                incrementerTentatives(u.getId(), nouvellesTentatives);
                int restantes = 3 - nouvellesTentatives;
                if (restantes <= 0) throw new SecurityException("MAX_TENTATIVES");
                throw new SecurityException("MOT_DE_PASSE_INCORRECT:" + restantes);
            }
        }
    }

    private void incrementerTentatives(int id, int n) throws SQLException {
        if (n >= 3) {
            String now = LocalDateTime.now().format(FMT);
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE utilisateurs SET tentatives_connexion=?, statut='BLOQUE', date_blocage=? WHERE id=?")) {
                ps.setInt(1, n); ps.setString(2, now); ps.setInt(3, id);
                ps.executeUpdate();
            }
        } else {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE utilisateurs SET tentatives_connexion=? WHERE id=?")) {
                ps.setInt(1, n); ps.setInt(2, id); ps.executeUpdate();
            }
        }
    }

    private void resetTentatives(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE utilisateurs SET tentatives_connexion=0, date_blocage=NULL WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private void debloquerApresExpiration(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE utilisateurs SET statut='ACTIF', tentatives_connexion=0, date_blocage=NULL WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    // ===== LISTES =====

    public List<Utilisateur> getTousLesAdherents() throws SQLException {
        return getByRoleStatut("ADHERENT", "ACTIF", "BLOQUE", "EN_ATTENTE");
    }

    public List<Utilisateur> getTousLesAdherentsAvecArchives() throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE role='ADHERENT' ORDER BY statut, nom, prenom";
        return executeList(sql);
    }

    public List<Utilisateur> getAdherentsEnAttente() throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE role='ADHERENT' AND statut='EN_ATTENTE' ORDER BY date_inscription DESC";
        return executeList(sql);
    }

    public List<Utilisateur> getTousLesStaff() throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE role IN ('ADMIN','ASSISTANT') ORDER BY role, nom";
        return executeList(sql);
    }

    public List<Utilisateur> rechercher(String terme) throws SQLException {
        String sql = """
            SELECT * FROM utilisateurs
            WHERE role='ADHERENT' AND statut != 'ARCHIVE'
            AND (LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ? OR telephone LIKE ?)
            ORDER BY nom, prenom
        """;
        String q = "%" + terme.toLowerCase() + "%";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q); ps.setString(4, q);
            ResultSet rs = ps.executeQuery();
            List<Utilisateur> list = new ArrayList<>();
            while (rs.next()) list.add(mapResultSet(rs));
            return list;
        }
    }

    public List<Utilisateur> rechercherDansArchives(String terme) throws SQLException {
        String sql = """
            SELECT * FROM utilisateurs WHERE role='ADHERENT' AND statut='ARCHIVE'
            AND (LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ?)
            ORDER BY nom, prenom
        """;
        String q = "%" + terme.toLowerCase() + "%";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            ResultSet rs = ps.executeQuery();
            List<Utilisateur> list = new ArrayList<>();
            while (rs.next()) list.add(mapResultSet(rs));
            return list;
        }
    }

    private List<Utilisateur> getByRoleStatut(String role, String... statuts) throws SQLException {
        String placeholders = String.join(",", java.util.Collections.nCopies(statuts.length, "?"));
        String sql = "SELECT * FROM utilisateurs WHERE role=? AND statut IN (" + placeholders + ") ORDER BY nom, prenom";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            for (int i = 0; i < statuts.length; i++) ps.setString(i + 2, statuts[i]);
            ResultSet rs = ps.executeQuery();
            List<Utilisateur> list = new ArrayList<>();
            while (rs.next()) list.add(mapResultSet(rs));
            return list;
        }
    }

    private List<Utilisateur> executeList(String sql) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            List<Utilisateur> list = new ArrayList<>();
            while (rs.next()) list.add(mapResultSet(rs));
            return list;
        }
    }

    // ===== CRUD =====

    public Utilisateur getById(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM utilisateurs WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapResultSet(rs) : null;
        }
    }

    public boolean emailExiste(String email) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM utilisateurs WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean emailExisteAutreUtilisateur(String email, int excludeId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM utilisateurs WHERE email=? AND id!=?")) {
            ps.setString(1, email); ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public int inserer(Utilisateur u) throws SQLException {
        // Générer numero_adherent si c'est un adhérent
        String numeroAdherent = null;
        if ("ADHERENT".equals(u.getRole())) {
            String sqlNum = "SELECT COUNT(*) FROM utilisateurs WHERE role='ADHERENT'";
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(sqlNum)) {
                int count = rs.next() ? rs.getInt(1) : 0;
                numeroAdherent = "AD-" + (count + 1);
            }
        }

        String sql = """
            INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, statut, telephone, date_naissance, adresse, numero_adherent)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom()); ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, BCrypt.hashpw(u.getMotDePasse(), BCrypt.gensalt()));
            ps.setString(5, u.getRole()); ps.setString(6, u.getStatut());
            ps.setString(7, u.getTelephone()); ps.setString(8, u.getDateNaissance());
            ps.setString(9, u.getAdresse());
            ps.setString(10, numeroAdherent);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public void modifier(Utilisateur u) throws SQLException {
        String sql = """
            UPDATE utilisateurs SET nom=?, prenom=?, email=?, telephone=?, date_naissance=?, adresse=?, role=?
            WHERE id=?
        """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNom()); ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail()); ps.setString(4, u.getTelephone());
            ps.setString(5, u.getDateNaissance()); ps.setString(6, u.getAdresse());
            ps.setString(7, u.getRole()); ps.setInt(8, u.getId());
            ps.executeUpdate();
        }
    }

    public void changerMotDePasse(int id, String nouveauMdp) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE utilisateurs SET mot_de_passe=? WHERE id=?")) {
            ps.setString(1, BCrypt.hashpw(nouveauMdp, BCrypt.gensalt()));
            ps.setInt(2, id); ps.executeUpdate();
        }
    }

    public boolean verifierMotDePasse(int id, String mdp) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT mot_de_passe FROM utilisateurs WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && BCrypt.checkpw(mdp, rs.getString(1));
        }
    }

    public void validerCompte(int id) throws SQLException { changerStatut(id, "ACTIF"); }
    public void archiverAdherent(int id) throws SQLException { changerStatut(id, "ARCHIVE"); }
    public void restaurerAdherent(int id) throws SQLException { changerStatut(id, "ACTIF"); }

    public void bloquerCompte(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE utilisateurs SET statut='BLOQUE', date_blocage=NULL WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public void debloquerCompte(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE utilisateurs SET statut='ACTIF', tentatives_connexion=0, date_blocage=NULL WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    private void changerStatut(int id, String statut) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE utilisateurs SET statut=? WHERE id=?")) {
            ps.setString(1, statut); ps.setInt(2, id); ps.executeUpdate();
        }
    }

    // ===== STATS =====
    public int countAdherents() throws SQLException {
        return countWhere("role='ADHERENT' AND statut='ACTIF'");
    }
    public int countEnAttente() throws SQLException {
        return countWhere("role='ADHERENT' AND statut='EN_ATTENTE'");
    }
    public int countBloques() throws SQLException {
        return countWhere("role='ADHERENT' AND statut='BLOQUE'");
    }
    public Utilisateur getParEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        }
        return null;
    }

    private int countWhere(String where) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM utilisateurs WHERE " + where)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id")); u.setNom(rs.getString("nom")); u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email")); u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(rs.getString("role")); u.setStatut(rs.getString("statut"));
        u.setTelephone(rs.getString("telephone")); u.setDateNaissance(rs.getString("date_naissance"));
        u.setAdresse(rs.getString("adresse")); u.setDateInscription(rs.getString("date_inscription"));
        u.setTentativesConnexion(rs.getInt("tentatives_connexion"));
        u.setDateBlocage(rs.getString("date_blocage")); u.setPhotoPath(rs.getString("photo_path"));
        u.setNumeroAdherent(rs.getString("numero_adherent"));
        return u;
    }
}
