# POC — API-first (OpenAPI → JAX-RS/Jersey) sur Tomcat, avec Swagger UI riche

POC qui montre comment faire de l'**API-first** proprement :

- `src/main/resources/openapi/openapi.yaml` est la **source de vérité**.
- Le build **génère** les interfaces JAX-RS + les DTO (openapi-generator, `jaxrs-spec`).
- On **écrit uniquement l'implémentation** des controllers, en **pur Jakarta EE**
  (`jakarta.ws.rs.*`, Jersey) — **aucun Spring dans les controllers**.
- Swagger UI charge le **fichier openapi.yaml tel quel** → 100 % des métadonnées
  (descriptions, exemples, valeurs par défaut, schémas de réponse) s'affichent,
  **sans une seule annotation Swagger dans le code** (donc zéro template Mustache).

## Le principe (à retenir)

```
                         ┌── (BUILD)   openapi-generator → interfaces JAX-RS + DTO
  openapi.yaml  ─────────┤
  (source unique)        └── (RUNTIME) servi tel quel → Swagger UI

  ⛔ On NE régénère JAMAIS la spec depuis le code (pas de OpenApiResource / springdoc scan).
```

La richesse de Swagger UI dépend **à 100 % du fichier openapi.yaml**, **à 0 %** des
annotations du code. C'est ce qui évite tous les pièges du mode « code-first ».

## Stack

- Java 21, Spring Boot 3.3 packagé en **WAR** (Tomcat 10.1 / Jakarta EE 10 externe)
- **Jersey 3.1** (implémentation JAX-RS) monté sur `/api/*`
- **openapi-generator** `jaxrs-spec` (`interfaceOnly`, `useJakartaEe`, `useBeanValidation`)
- **Bean Validation** fonctionnelle (les `@Size`/`@Pattern` viennent du `pattern`/`minLength` du YAML)
- **Swagger UI** (webjar, hors-ligne, pas de CDN)
- **Spring Security** (démo : swagger en accès libre, API protégée)

## Arborescence utile

| Rôle | Fichier |
|------|---------|
| **Contrat (source de vérité)** | `src/main/resources/openapi/openapi.yaml` |
| Interface générée (à implémenter) | `target/generated-sources/openapi/.../com/poc/api/LivresApi.java` |
| DTO générés (+ Bean Validation) | `target/generated-sources/openapi/.../com/poc/api/model/*.java` |
| **Implémentation (pur JAX-RS)** | `src/main/java/com/poc/api/impl/LivreController.java` |
| Assemblage JAX-RS (providers, pas de scanner) | `.../api/impl/ApiResourceConfig.java` |
| Montage Jersey sur `/api/*` | `src/main/java/com/poc/config/JerseyConfig.java` |
| Service Swagger UI + spec (Spring) | `src/main/java/com/poc/config/SwaggerUiConfig.java` |
| Redirect `/swagger-ui` (context-path safe) | `src/main/java/com/poc/web/SwaggerRedirectController.java` |
| Sécurité (permitAll des URLs Swagger) | `src/main/java/com/poc/config/SecurityConfig.java` |

## Lancer

### Via Docker (Tomcat 10 externe — le vrai cas de déploiement)

```bash
mvn clean package -DskipTests      # génère + compile + WAR (target/app.war)
docker compose up -d               # Tomcat dépose le WAR en ROOT.war, sert sur :8080
```

- Swagger UI : http://localhost:8080/swagger-ui
- Spec brute : http://localhost:8080/openapi/openapi.yaml
- API : `GET /api/livres`, `GET /api/livres/{id}`, `POST /api/livres`, `DELETE /api/livres/{id}`

L'API est protégée par Spring Security (user **admin** / **admin** pour le POC) :

```bash
curl -u admin:admin "http://localhost:8080/api/livres?genre=ROMAN&tri=prix"
```

### En local (Tomcat embarqué, itération rapide)

```bash
mvn spring-boot:run
```

## Points de conception (les pièges rencontrés & résolus)

- **`useSwaggerAnnotations=false`** : inutile en API-first, Swagger UI lit le YAML.
- **`returnResponse=true`** : les méthodes retournent `Response` → contrôle des
  codes (201/204) et de `Location`.
- **`generateSupportingFiles=false`** : sinon le `RestApplication` (`@ApplicationPath`)
  généré déclenche un scan JAX-RS sur Tomcat externe qui prend l'interface pour une
  ressource → **500**.
- **Spec servie en statique** (resource handler `/openapi/**`), jamais par un
  `@Controller` retournant une String → évite le **Circular view path**.
- **Redirect indépendant du nom du WAR** via `request.getContextPath()`.
- **Spring Security** : il faut `permitAll` sur `/swagger-ui`, `/openapi/**` ET
  `/webjars/swagger-ui/**` (pas seulement la spec).
