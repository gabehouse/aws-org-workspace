pipeline {
    agent any

    stages {
        stage('Build & Deploy Java App') {
            // Only runs if files inside wilderchess-app/ changed
            when {
                changeset "services/wilderchess/**"
            }
            steps {
                dir('services/wilderchess') {
                    sh 'mvn clean package'
                    script {
                        // 1. Cleanup old instances
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        // 2. Launch the Jetty WebSocket Server
                        // Note: We use --network bridge to ensure it's accessible locally
                        sh '''
                            docker run -d \
                            --name wilderchess-app \
                            -p 8081:8080 \
                            -v /workspaces/aws-org-workspace:/app \
                            --restart unless-stopped \
                            openjdk:17-jdk-slim \
                            java -Dport=8080 -jar /app/services/wilderchess/target/wilderchess-app.jar
                        '''
                    }
                }
            }
        }
    }
}
