pipeline {
    // L'agent doit disposer de Java 17, Docker, Docker Compose et d'un shell Linux.
    agent any

    environment {
        BACKEND_IMAGE = 'emp_backend_img'
        FRONTEND_IMAGE = 'emp_frontend_img'
        DOCKERHUB_CREDENTIALS = 'dockerhub-cred'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Récupération du code source'
                git branch: 'master',
                    credentialsId: 'github-cred',
                    url: 'https://github.com/ines-abbes/emp_app.git'
            }
        }

        stage('Tests unitaires - Spring Boot') {
            steps {
                dir('emp_backend') {
                    sh 'chmod +x mvnw'
                    sh './mvnw -B -Dtest=EmployeeControllerUnitTest test'
                }
            }
        }

        stage('Tests d’intégration - Spring Boot + H2') {
            steps {
                dir('emp_backend') {
                    sh './mvnw -B -Dtest=EmployeeControllerIntegrationTest test'
                }
            }
        }

        stage('Package Backend') {
            steps {
                dir('emp_backend') {
                    sh './mvnw -B -DskipTests package'
                }
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('Build Backend') {
                    steps {
                        sh 'docker build -t ${BACKEND_IMAGE}:latest emp_backend'
                    }
                }
                stage('Build Frontend') {
                    steps {
                        sh 'docker build -t ${FRONTEND_IMAGE}:latest emp_frontend'
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKERHUB_CREDENTIALS}",
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_PASS'
                    )]) {
                        sh '''
                            set +x
                            echo "$DOCKERHUB_PASS" | docker login --username "$DOCKERHUB_USER" --password-stdin
                            docker tag "$BACKEND_IMAGE:latest" "$DOCKERHUB_USER/$BACKEND_IMAGE:latest"
                            docker tag "$FRONTEND_IMAGE:latest" "$DOCKERHUB_USER/$FRONTEND_IMAGE:latest"
                            docker push "$DOCKERHUB_USER/$BACKEND_IMAGE:latest"
                            docker push "$DOCKERHUB_USER/$FRONTEND_IMAGE:latest"
                            docker logout
                        '''
                    }
                }
            }
        }

        stage('Déploiement avec Docker Compose') {
            steps {
                sh 'docker compose up -d'
            }
        }
    }

    post {
        always {
            junit testResults: 'emp_backend/target/surefire-reports/*.xml',
                  allowEmptyResults: true
        }
        success {
            echo 'Pipeline terminé : tests validés, images construites et publiées.'
        }
        failure {
            echo 'Pipeline en échec : consulter le stage et les rapports de tests.'
        }
    }
}
