package com.club.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;

public class DatabaseConfig {

    // ══════════════════════════════════════════════════
    //  CONFIGURATION SUPABASE
    // ══════════════════════════════════════════════════
    private static final String DB_HOST     = "aws-0-eu-west-1.pooler.supabase.com";
    private static final String DB_PORT     = "6543";
    private static final String DB_NAME     = "postgres";
    private static final String DB_USER     = "postgres.limnnijaknhgxccadpye";
    private static final String DB_PASSWORD = "PzAQomMlxm9ywxQZ"; // ← remplace ici

    private static HikariDataSource dataSource;

    // ── Initialisation du pool de connexions ──────────
    private static synchronized void initPool() {
        if (dataSource != null && !dataSource.isClosed()) return;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
                          "?sslmode=require&prepareThreshold=0");
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);

        // Pool de 3 connexions réutilisées
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10_000);   // 10 sec max pour obtenir une connexion
        config.setIdleTimeout(300_000);         // Fermer après 5 min d'inactivité
        config.setMaxLifetime(600_000);         // Renouveler après 10 min
        config.setKeepaliveTime(60_000);        // Ping toutes les 60 sec pour garder vivante
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("ClubSportifPool");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        initPool();
        return dataSource.getConnection();
    }

    public static void initializeDatabase() {
        try {
            initPool();
            try (Connection conn = getConnection();
                 Statement s = conn.createStatement()) {
                s.execute("SELECT 1");
            }
            System.out.println("✅ Pool Supabase initialisé.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion Supabase: " + e.getMessage());
            throw new RuntimeException(
                "Impossible de se connecter à la base de données.\n" +
                "Vérifiez votre connexion internet.", e);
        }
    }

    public static String getConfig(String cle) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT valeur FROM config WHERE cle=?")) {
            ps.setString(1, cle);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : "";
        } catch (SQLException e) { return ""; }
    }

    public static void setConfig(String cle, String valeur) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO config (cle, valeur) VALUES (?,?) " +
                "ON CONFLICT (cle) DO UPDATE SET valeur=EXCLUDED.valeur")) {
            ps.setString(1, cle);
            ps.setString(2, valeur);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
