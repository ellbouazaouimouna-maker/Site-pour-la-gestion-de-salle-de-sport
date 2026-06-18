package com.club.util;

import com.club.config.DatabaseConfig;
import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    
    private static final String EMAIL_EXPEDITEUR  = "clubsportif767@gmail.com";
    private static final String EMAIL_MOT_DE_PASSE = "gpgxyfgbagpwbzae"; // code 16 car sans espaces

   

    /** Envoie un code de réinitialisation de mot de passe */
    public static void sendResetCode(String destinataire, String prenom, String code) {
        String nomClub = DatabaseConfig.getConfig("nom_club");
        String sujet = "🔑 Code de réinitialisation — " + nomClub;
        String corps =
            "Bonjour " + prenom + ",\n\n" +
            "Votre code de réinitialisation de mot de passe est :\n\n" +
            "        [ " + code + " ]\n\n" +
            " Ce code expire dans 10 minutes.\n" +
            "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
            "Cordialement,\n" +
            "L'équipe " + nomClub;
        envoyerEmail(destinataire, sujet, corps);
    }

    /** Envoie un email quand le compte est bloqué automatiquement (3 tentatives) */
    public static void sendCompteBloquéAuto(String destinataire, String prenom) {
        String nomClub  = DatabaseConfig.getConfig("nom_club");
        String emailClub = DatabaseConfig.getConfig("email_club");
        String telClub   = DatabaseConfig.getConfig("telephone_club");
        String sujet = " Compte bloqué — " + nomClub;
        String corps =
            "Bonjour " + prenom + ",\n\n" +
            "Votre compte a été bloqué automatiquement suite à 3 tentatives " +
            "de connexion incorrectes.\n\n" +
            "Pour débloquer votre compte, veuillez contacter l'administration :\n" +
             emailClub + "\n" +
             telClub + "\n\n" +
            "Cordialement,\n" +
            "L'équipe " + nomClub;
        envoyerEmail(destinataire, sujet, corps);
    }

    /** Envoie un email quand l'admin bloque manuellement un compte */
    public static void sendCompteBloquéAdmin(String destinataire, String prenom) {
        String nomClub   = DatabaseConfig.getConfig("nom_club");
        String emailClub = DatabaseConfig.getConfig("email_club");
        String telClub   = DatabaseConfig.getConfig("telephone_club");
        String sujet = " Compte suspendu — " + nomClub;
        String corps =
            "Bonjour " + prenom + ",\n\n" +
            "Votre compte a été suspendu par l'administration.\n\n" +
            "Pour plus d'informations, veuillez nous contacter :\n" +
             emailClub + "\n" +
            telClub + "\n\n" +
            "Cordialement,\n" +
            "L'équipe " + nomClub;
        envoyerEmail(destinataire, sujet, corps);
    }

    /** Envoie un email quand l'admin débloque un compte */
    public static void sendCompteDébloqué(String destinataire, String prenom) {
        String nomClub   = DatabaseConfig.getConfig("nom_club");
        String emailClub = DatabaseConfig.getConfig("email_club");
        String sujet = " Compte réactivé — " + nomClub;
        String corps =
            "Bonjour " + prenom + ",\n\n" +
            "Bonne nouvelle ! Votre compte a été réactivé par l'administration.\n" +
            "Vous pouvez maintenant vous connecter à votre espace.\n\n" +
              emailClub + "\n\n" +
            "Cordialement,\n" +
            "L'équipe " + nomClub;
        envoyerEmail(destinataire, sujet, corps);
    }

    /** Envoie un email quand l'admin valide un nouveau compte */
    public static void sendCompteValidé(String destinataire, String prenom) {
        String nomClub   = DatabaseConfig.getConfig("nom_club");
        String emailClub = DatabaseConfig.getConfig("email_club");
        String telClub   = DatabaseConfig.getConfig("telephone_club");
        String sujet = " Compte activé — " + nomClub;
        String corps =
            "Bonjour " + prenom + ",\n\n" +
            "Votre compte a été validé par l'administration.\n" +
            "Vous pouvez maintenant vous connecter à votre espace membre.\n\n" +
            "Pour toute question :\n" +
              emailClub + "\n" +
              telClub + "\n\n" +
            "Cordialement,\n" +
            "L'équipe " + nomClub;
        envoyerEmail(destinataire, sujet, corps);
    }

    /** Génère un code aléatoire à 6 chiffres */
    public static String genererCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /** Méthode interne d'envoi — ne pas modifier */
    private static void envoyerEmail(String destinataire, String sujet, String corps) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

                javax.mail.Session mailSession = javax.mail.Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_EXPEDITEUR, EMAIL_MOT_DE_PASSE);
                    }
                });

                Message message = new MimeMessage(mailSession);
                message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
                message.setSubject(sujet);
                message.setText(corps);
                Transport.send(message);
                System.out.println(" Email envoyé à : " + destinataire);
            } catch (Exception e) {
                System.err.println(" Erreur envoi email : " + e.getMessage());
            }
        }).start();
    }
}
