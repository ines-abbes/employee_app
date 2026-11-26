# Tests automatisés du backend Spring Boot

## Prérequis
- Java 17
- Maven (ou le Maven Wrapper `./mvnw`)
- Jenkins avec un agent Linux disposant de Java 17, Docker et Docker Compose
- Credentials Jenkins `github-cred` et `dockerhub-cred`

## Types de tests

### Tests unitaires
`EmployeeControllerUnitTest` utilise JUnit 5 et Mockito. Le `EmployeeRepository` est simulé (`@Mock`) : les tests vérifient le comportement du contrôleur sans démarrer Spring ni accéder à une base de données.

### Tests d'intégration
`EmployeeControllerIntegrationTest` utilise `@SpringBootTest`, `@AutoConfigureMockMvc` et le vrai repository JPA. Le profil `test` configure H2 en mémoire via `src/test/resources/application-test.properties`. MySQL et Docker ne sont donc pas nécessaires pour ces tests.

## Exécuter localement

Depuis `emp_backend/` :

```bash
# Tests unitaires uniquement
./mvnw -B -Dtest=EmployeeControllerUnitTest test

# Tests d'intégration uniquement
./mvnw -B -Dtest=EmployeeControllerIntegrationTest test

# Tous les tests
./mvnw -B test
```

Sous Windows PowerShell, utiliser `mvnw.cmd` à la place de `./mvnw`.

Les rapports XML Surefire sont générés dans `emp_backend/target/surefire-reports/`.

## Ordre du pipeline Jenkins

1. Checkout
2. Tests unitaires
3. Tests d'intégration
4. Packaging du backend
5. Construction des images Docker (backend et frontend en parallèle)
6. Publication sur Docker Hub
7. Déploiement avec Docker Compose

Si un test échoue, Jenkins interrompt les étapes suivantes : aucune image n'est construite ni publiée.

## Remarques sur l'environnement Jenkins
Le Jenkinsfile fourni utilise les étapes `sh` : il suppose donc un agent Linux. Celui-ci doit avoir accès au démon Docker. Adapter le pipeline (`bat` au lieu de `sh`) si l'agent est exclusivement Windows.

Le pipeline conserve le dépôt GitHub et les identifiants présents dans le Jenkinsfile initial. Vérifier que la branche distante est bien `master` et que les credentials Jenkins portent les IDs `github-cred` et `dockerhub-cred`.
