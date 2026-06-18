#  Club Sportif

Application de gestion de club sportif développée en **Java 17**, **JavaFX 21**, **PostgreSQL**, **Supabase**, **Maven**, **HikariCP** et **jBCrypt**.

---

#  Prérequis

## 1. Java JDK 17

Télécharger et installer Java 17.

Vérification :

```bash
java -version
```

Doit afficher Java 17.

---

## 2. Apache Maven

Installer Maven puis ajouter le dossier `bin` dans la variable d'environnement `PATH`.

Vérification :

```bash
mvn -version
```

---

#  Technologies utilisées

## Frontend

- JavaFX 21
- FXML
- CSS

## Backend

- Java 17
- Architecture MVC
- DAO (Data Access Object)
- JDBC
- HikariCP
- jBCrypt

## Base de données

- PostgreSQL
- Supabase (Cloud Database)

## Gestion des dépendances

- Maven

---

#  Structure du projet

```text
sport-club-app/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/club/
│       │   ├── MainApp.java
│       │   ├── config/
│       │   │   └── DatabaseConfig.java
│       │   ├── model/
│       │   ├── dao/
│       │   ├── controller/
│       │   │   ├── auth/
│       │   │   ├── admin/
│       │   │   └── adherent/
│       │   ├── services/
│       │   └── util/
│       └── resources/
│           ├── css/
│           ├── images/
│           └── fxml/
```

---

#  Configuration de la base de données

Le projet utilise PostgreSQL hébergé sur Supabase.

Fichier de configuration :

```text
src/main/java/com/club/config/DatabaseConfig.java
```

Exemple :

```java
private static final String URL =
"jdbc:postgresql://xxxx.supabase.co:5432/postgres";

private static final String USER = "postgres";

private static final String PASSWORD = "********";
```

 Ne jamais publier les identifiants réels sur GitHub.

---

#  Compilation du projet

Depuis la racine du projet :

```bash
cd sport-club-app
```

Compiler :

```bash
mvn clean package
```

Le fichier généré :

```text
target/ClubSportif.jar
```

---

#  Exécution

```bash
java -jar target/ClubSportif.jar
```

---

#  Génération d'un exécutable Windows

## Avec jpackage

```bash
jpackage ^
--input target/ ^
--name "Club Sportif" ^
--main-jar ClubSportif.jar ^
--main-class com.club.MainApp ^
--type exe ^
--app-version 1.0 ^
--vendor "Club Sportif" ^
--win-shortcut ^
--win-menu
```

---

# Architecture MVC

## Model

Contient les entités :

- Utilisateur
- Activite
- Paiement

## View

Interfaces JavaFX :

- login.fxml
- dashboard.fxml
- activites.fxml
- paiements.fxml

## Controller

Gestion des actions utilisateur :

- LoginController
- DashboardController
- ActivitesController
- PaiementsController

## DAO

Accès aux données :

- UtilisateurDAO
- ActiviteDAO
- PaiementDAO

---

# Sécurité

## BCrypt

Hashage des mots de passe :

```java
BCrypt.hashpw(password, BCrypt.gensalt());
```

Vérification :

```java
BCrypt.checkpw(password, hash);
```

## HikariCP

Pool de connexions PostgreSQL.

Avantages :

- Réutilisation des connexions
- Amélioration des performances
- Réduction du temps d'accès à la base de données

---

#  Fonctionnalités

## Administrateur

- Gestion des adhérents
- Gestion des activités
- Gestion des paiements
- Gestion du personnel
- Consultation des statistiques
- Archivage des comptes

## Adhérent

- Consultation du profil
- Consultation des activités
- Consultation des paiements
- Modification du mot de passe

---

#  Flux de fonctionnement

```text
Interface JavaFX
       ↓
Controller
       ↓
DAO
       ↓
DatabaseConfig
       ↓
HikariCP
       ↓
PostgreSQL (Supabase)
```

---

#  Sécurité supplémentaire

- Mots de passe protégés par BCrypt
- Gestion des rôles (Admin / Adhérent)
- Validation des données
- Contrôle d'accès aux fonctionnalités

---

#  Dépendances Maven principales

- JavaFX
- PostgreSQL JDBC Driver
- HikariCP
- jBCrypt
- JavaMail
- Ikonli

Toutes les dépendances sont déclarées dans :

```text
pom.xml
```

---

##  Développé avec

- Java 17
- JavaFX 21
- PostgreSQL
- Supabase
- Maven
- HikariCP
- jBCrypt

Projet réalisé dans le cadre d'une application de gestion de club sportif.
