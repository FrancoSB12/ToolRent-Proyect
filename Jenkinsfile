pipeline {
    agent any

    environment {
        DOCKER_USER = 'francosb'
        IMAGE_BACKEND = 'toolrent-backend'
        IMAGE_FRONTEND = 'toolrent-frontend'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
    }

    stages {
        stage('Test Backend') {
            steps {
                echo '🧪 Ejecutando tests unitarios y cobertura...'
                dir('Tingeso_proyect_2-2025_backend') {
                    sh 'chmod +x mvnw'
                    sh './mvnw clean test'
                }
            }
        }

        stage('Build & Push Backend') {
            steps {
                cleanWs()
                checkout scm
                echo '🐳 Construyendo imagen de Backend...'
                dir('Tingeso_proyect_2-2025_backend') {
                    sh './mvnw package -DskipTests'
                    
                    script {
                        docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                            def app = docker.build("${DOCKER_USER}/${IMAGE_BACKEND}:latest", '--no-cache .')
                            app.push()
                        }
                    }
                }
            }
        }

        stage('Build & Push Frontend') {
            steps {
                cleanWs()
                checkout scm
                echo '⚛️ Construyendo imagen de Frontend...'
                dir('Tingeso_proyect_2-2025_frontend') {
                    script {
                        docker.withRegistry('', DOCKER_CREDENTIALS_ID) {
                            def app = docker.build("${DOCKER_USER}/${IMAGE_FRONTEND}:latest", '--no-cache .')
                            app.push()
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo '🧹 Limpiando espacio de trabajo...'
            cleanWs()
        }
        success {
            echo '✅ ¡Pipeline finalizado con éxito! Las imágenes están en Docker Hub.'
        }
        failure {
            echo '❌ Algo falló. Revisa los logs de Jenkins.'
        }
    }
}