pipeline {
    agent any

    stages {
        stage('Build & Deploy Java App') {
            when {
                allOf {
                    branch 'dev'
                    changeset "services/wilderchess/**"
                }
            }
            steps {
                // Use a Maven container to avoid "mvn: not found"
                docker.image('maven:3.9.6-eclipse-temurin-21').inside('-v /root/.m2:/root/.m2') {
                    dir('services/wilderchess') {
                        sh 'mvn clean package'
                    }
                }

                script {
                    dir('services/wilderchess') {
                        // Cleanup and launch sibling container
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'
                        sh '''
                            docker run -d \
                            --name wilderchess-app \
                            -p 8081:8080 \
                            -v /var/jenkins_home/workspace/wilderchess-local-build:/app \
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
