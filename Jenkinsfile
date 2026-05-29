pipeline {
    agent any

    environment {
        // Option A in the UI makes this the folder containing the 'docker' binary
        DOCKER_HOME = tool 'docker-latest'
        PATH = "${env.DOCKER_HOME}:${env.PATH}"
    }

    stages {
        stage('Build & Deploy') {
            steps {
                script {
                    dir('services/wilderchess') {
                        sh '''
                            ls -R ${DOCKER_HOME}
                            docker run --rm \
                            -v /root/.m2:/root/.m2 \
                            -v $(pwd):/app \
                            -w /app \
                            maven:3.8.5-openjdk-8 mvn clean package -DskipTests
                        '''

                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        sh '''
                            echo "FROM openjdk:8-jre-slim
                            COPY target/wilderchess-app.jar app.jar
                            ENTRYPOINT [\\"java\\", \\"-Dport=8080\\", \\"-jar\\", \\"app.jar\\"]" > Dockerfile.deploy

                            docker build -t wilderchess-img -f Dockerfile.deploy .
                            docker run -d --name wilderchess-app -p 8086:8080 wilderchess-img
                        '''
                    }
                }
            }
        }
    }
}
