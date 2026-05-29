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
                script {
                    // Moving the docker logic inside 'script' fixes the 'Expected a symbol' error
                    docker.image('maven:3.9.6-eclipse-temurin-21').inside('-v /root/.m2:/root/.m2') {
                        dir('services/wilderchess') {
                            sh 'mvn clean package'
                        }
                    }

                    dir('services/wilderchess') {
                        // Cleanup and launch sibling container
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        // Note: Using the internal Jenkins workspace path for the mount
                        sh '''
                            docker run -d \
                            --name wilderchess-app \
                            -p 8081:8080 \
                            -v ${WORKSPACE}:/app \
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
