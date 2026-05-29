pipeline {
    agent any
    stages {
        stage('Build & Deploy') {
            steps {
                script {
                    // 1. Build the JAR inside a Maven container
                    docker.image('maven:3.8.5-openjdk-8').inside('-v /root/.m2:/root/.m2') {
                        dir('services/wilderchess') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }

                    // 2. Wrap the JAR into a Docker Image (This is the "Update")
                    dir('services/wilderchess') {
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        // We create a tiny Dockerfile to "Bake" the JAR into the image
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
