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
                    // STAGE 1: Build the JAR using a Maven container
                    docker.image('maven:3.9.6-eclipse-temurin-21').inside('-v /root/.m2:/root/.m2') {
                        dir('services/wilderchess') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }

                    // STAGE 2: Deploy the JAR as a sibling container
                    dir('services/wilderchess') {
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        // We use a relative path for the JAR since we are on the same host socket
                        sh '''
                            echo "FROM openjdk:17-jdk-slim
                            COPY target/wilderchess-app.jar app.jar
                            ENTRYPOINT [\\"java\\", \\"-jar\\", \\"app.jar\\"]" > Dockerfile.deploy

                            docker build -t wilderchess-img -f Dockerfile.deploy .
                            docker run -d --name wilderchess-app -p 8081:8080 wilderchess-img
                        '''
                    }
                }
            }
        }
    }
}
