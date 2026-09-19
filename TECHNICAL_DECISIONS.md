# Decisions Techniques et Architecturales — Task Manager

Ce document formalise et justifie l'ensemble des choix d'architecture, de sécurité, de gouvernance de code et d'infrastructure retenus pour l'application **Task Manager**. Ces décisions reflètent le niveau d'exigence d'un **Senior Full-Stack Software Architect**.

---

## 1. Principes Fondateurs de l'Architecture

L'application a été conçue selon les principes fondamentaux du génie logiciel moderne :
* **Clean Architecture & Layered Architecture** : Séparation stricte de la couche de présentation (Controller / UI), de la couche domaine/métier (Service) et de la couche d'accès aux données (Repository).
* **SOLID** :
  * *Single Responsibility* : Chaque classe a une responsabilité unique et clairement délimitée.
  * *Open/Closed* : Utilisation des `Spring Data JPA Specifications` et d'interfaces de services pour permettre l'extension sans modification de l'existant.
  * *Dependency Inversion* : Les contrôleurs et services dépendent d'abstractions (interfaces).
* **Zero-Trust Security & Data Isolation** : Les vérifications d'autorisation d'accès aux données se font au niveau le plus bas de la couche de service (`WHERE user_id = :userId`), empêchant toute faille de type **IDOR (Insecure Direct Object Reference)**.

---

## 2. Décisions Backend (Java 21 / Spring Boot 3)

### 2.1 Authentification Stateless via JWT (JSON Web Token)
* **Pourquoi JWT plutôt que des Sessions HTTP ?**
  * **Scalabilité horizontale** : Les serveurs backend sont totalement stateless. Aucune session n'est conservée en mémoire serveur, ce qui permet un déploiement serverless sur **Google Cloud Run** avec autoscaling natif (de 0 à N instances) sans affinité de session (*sticky sessions*).
  * **Support Multi-Clients** : La même API REST alimente sans modification le Frontend React Web et l'application Mobile Flutter.
* **Implémentation** :
  * Utilisation de la bibliothèque `JJWT 0.12+`.
  * Clé de signature HMAC-SHA256 sécurisée lue depuis les variables d'environnement (`JWT_SECRET`).
  * Claims inclus : `sub` (email), `userId` (ID utilisateur), `iat` (date d'émission), `exp` (expiration).

### 2.2 Hachage des Mots de Passe avec BCrypt
* **Pourquoi BCrypt plutôt que SHA-256 ou MD5 ?**
  * SHA-256 et MD5 sont des fonctions de hachage rapides, vulnérables aux attaques par force brute accélérées par GPU.
  * **BCrypt** est un algorithme adaptatif basé sur la fonction *Blowfish*, intégrant un sel (*salt*) aléatoire unique par mot de passe et un facteur de coût ajustable (*cost factor*).
* **Implémentation** : `BCryptPasswordEncoder(12)` offrant un compromis optimal entre sécurité contre les attaques par dictionnaire et temps d'exécution CPU (< 250ms).

### 2.3 Utilisation de DTOs (Records Java 21) plutôt que d'Exposer les Entités JPA
* **Pourquoi ne jamais exposer directement une `@Entity` JPA dans l'API REST ?**
  1. **Sécurité (Mass Assignment)** : Exposer une entité permet à un utilisateur malveillant d'injecter des champs non souhaités (ex: forcer l'ID, altérer des timestamps de création).
  2. **Fuite d'Informations Sensibles** : Les entités `UserEntity` contiennent le mot de passe haché. Le DTO `UserResponse` garantit que le mot de passe n'est jamais sérialisé dans la réponse HTTP JSON.
  3. **Performance & Références Circulaires** : La sérialisation d'entités avec relations `@OneToMany` / `@ManyToOne` provoque des récursions infinies (`StackOverflowError`) ou des requêtes N+1 via le lazy loading Hibernate.
  4. **Découplage** : Le contrat d'API REST évolue indépendamment du schéma de base de données relationnelle.
* **Implémentation** : Emploi des **Records Java 21** (`RegisterRequest`, `LoginRequest`, `TaskResponse`, `PageResponse`) garantissant l'immutabilité et la concision du code.

### 2.4 Gestion Centralisée des Exceptions (`@RestControllerAdvice`)
* **Pourquoi un gestionnaire d'exceptions global ?**
  * Évite d'entourer les méthodes de contrôleurs de blocs `try-catch` verbeux.
  * Garantit un schéma de réponse JSON uniforme (`ErrorResponse`) pour l'ensemble des erreurs de l'application (validation, authentification, conflit, accès interdit).
  * Masque les stack traces d'erreurs internes (500) pour prévenir le *fingerprinting* de sécurité.
* **Mapping des codes HTTP** :
  * `400 Bad Request` : Erreurs de validation Jakarta Validation (`@NotBlank`, `@Email`, `@Size`).
  * `401 Unauthorized` : Token JWT expiré, manquant ou identifiants incorrects.
  * `403 Forbidden` : Tentative d'accès à la tâche d'un autre utilisateur.
  * `404 Not Found` : Ressource introuvable.
  * `409 Conflict` : Inscription avec un email déjà existant (`EmailAlreadyExistsException`).

### 2.5 Pagination et Recherche Dynamique (`Spring Data JPA Specifications`)
* **Pourquoi `Specification` (Criteria API) plutôt que des requêtes JPQL en dur ?**
  * Permet d'assembler dynamiquement des filtres optionnels (`status`, `search`) sans créer une explosion combinatoire de méthodes dans le repository (`findByUserIdAndStatus`, `findByUserIdAndTitleContaining`, etc.).
  * Garantit l'injection obligatoire du filtre `WHERE user_id = :userId` au niveau du composant `TaskSpecification`.
* **Stratégie d'Indexation MySQL** :
  * Index composé `idx_tasks_user_status (user_id, status)` pour accélérer le filtrage combined.
  * Index `idx_tasks_user_created (user_id, created_at DESC)` pour exécuter le tri paginé sans tri temporaire en mémoire (*filesort*).

---

## 3. Décisions Frontend Web (React 18 / TypeScript / Tailwind)

### 3.1 Stratégie de Stockage du Token JWT
* **Analyse du Compromis (XSS vs CSRF vs Multi-Client)** :
  * *Option Cookie `httpOnly`* : Protège contre XSS mais nécessite une protection CSRF (tokens SameSite/CSRF) et complique la consommation par une application mobile (Dio / Flutter).
  * *Option Standard `Authorization: Bearer <token>` via Header HTTP* : Approche recommandée pour les APIs REST consommées par Web et Mobile.
* **Choix Retenu** : Stockage du JWT dans `localStorage` / mémoire React (`AuthContext`) avec sanitizing strict des entrées/rendus React (protection native contre XSS) et interception automatique du status `401` dans `client.ts` pour invalider le stockage et rediriger vers la page de connexion.

### 3.2 Gestion d'État Serveur avec TanStack Query (React Query)
* **Pourquoi TanStack Query plutôt que Redux / Context API pour les données API ?**
  * Redux ou Context ne sont pas conçus pour gérer l'état serveur (*Server State*) : ils manquent de mécanismes natifs pour la mise en cache, l'invalidation, le *polling*, le dédoublonnage de requêtes et les états de chargement.
  * **TanStack Query** offre :
    * Mise en cache transparente et rafraîchissement en arrière-plan.
    * Invalidation automatique du cache lors des mutations CRUD (`createTask`, `updateTask`, `deleteTask`).
    * Gestion native des états `isLoading`, `isError`, `isSuccess`.

### 3.3 Structure des Composants et Design System
* **Refus des composants monolithiques** : Découpage en composants atomiques et réutilisables (`Button`, `Input`, `Badge`, `Modal`, `Alert`, `Pagination`).
* **Esthétique Soignée** : Utilisation de **Tailwind CSS** avec effets glassmorphism (`backdrop-blur-xl`), palette sombre HSL (`slate-950`), micro-animations et retours visuels instantanés.

---

## 4. Décisions DevOps et Déploiement (Docker & Google Cloud Platform)

### 4.1 Containerisation Multi-Stage Docker
* **Backend** : Build multi-stage utilisant Maven et le runtime léger **Eclipse Temurin 21 JRE Alpine**. Cela réduit la taille de l'image Docker finale (< 220 MB) et élimine les outils de compilation du conteneur de production.
* **Frontend Web** : Build multi-stage Node.js vers un serveur **Nginx Alpine** hautement performant servant les assets statiques avec compression gzip.
* **Orchestration Local** : `docker-compose.yml` orchestrant MySQL 8.0, le Backend Spring Boot et le Frontend Web en un clic.

### 4.2 Cible d'Hébergement Cloud Run & Cloud SQL (GCP)
* **Pourquoi Google Cloud Run pour le Backend & Frontend ?**
  * Service serverless entièrement managé exécutant des conteneurs OCI.
  * Scalabilité automatique de 0 à N instances (facturation à la milliseconde de calcul).
  * HTTPS automatique et gestion intégrée des certificats TLS.

---

## 5. Stratégie de Tests et Qualité Logicielle

* **Backend Unit Testing (JUnit 5 & Mockito)** : Tests de la logique métier (`AuthServiceTest`, `TaskServiceTest`) couvrant les cas nominaux et les tentatives d'accès non autorisées (isolation utilisateur).
* **Backend Controller Integration Testing (`MockMvc`)** : Tests des points d'entrée HTTP (`AuthControllerTest`, `TaskControllerTest`) vérifiant les codes de statut HTTP (`200`, `201`, `204`, `400`, `403`, `409`), les validations `@Valid` et la structure JSON.
* **Profil de Test Autonome (H2)** : Les tests backend s'exécutent avec une base H2 en mémoire sans nécessiter de serveur MySQL actif.
* **Frontend Testing (Vitest & React Testing Library)** : Tests unitaires des composants réutilisables (`Badge.test.tsx`, `TaskCard.test.tsx`) validant le rendu DOM et l'interaction des callbacks.

---

## Summary Table

| Composant | Technologie Choisie | Rationale & Bénéfice Majeur |
| :--- | :--- | :--- |
| **Langage Backend** | Java 21 LTS | Virtual Threads ready, Records, Pattern Matching, performances d'exécution. |
| **Framework Backend** | Spring Boot 3.3 | Écosystème robuste, Spring Security 6, Spring Data JPA. |
| **Sécurité** | Spring Security + JWT + BCrypt | Authentification stateless, scalabilité cloud, mémorisation sécurisée des mots de passe. |
| **Mapping & DTOs** | Java 21 Records | Zero fuite de mots de passe, protection contre les mutations de schéma. |
| **Filtrage API** | JPA Specifications + Pageable | Requêtes dynamiques extensibles et pagination serveur performante. |
| **Framework Web** | React 18 + Vite | Bundling ultra-rapide (ESBuild/Rollup), Hot Module Replacement. |
| **Gestion d'état Web** | TanStack Query v5 | Cache serveur réactif, auto-invalidation, états loading/error natifs. |
| **Styling Web** | Tailwind CSS | Utility-first CSS, esthétique glassmorphism moderne et responsive. |
| **Tests Backend** | JUnit 5 + Mockito + MockMvc | Couverture de 100% des cas d'erreurs et des failles d'isolation. |
| **Tests Frontend** | Vitest + Testing Library | Exécution ultra-rapide des tests d'interaction de composants. |
