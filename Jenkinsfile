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
                    // This is the absolute path inside your Ubuntu WSL instance
                    def WSL_PATH = "/home/g/workspace/aws-org-workspace/services/wilderchess"

                    dir('services/wilderchess') {
                        sh """
                            docker run --rm \
                            -v /root/.m2:/root/.m2 \
                            -v ${WSL_PATH}:/app \
                            -w /app \
                            maven:3.9.6-eclipse-temurin-17 mvn clean package -DskipTests
                        """

                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        sh """
                            ls -lh target/  # This debug line will prove the JAR exists to Jenkins

                            echo "FROM eclipse-temurin:17-jre-alpine
                            COPY target/wilderchess-app.jar app.jar
                            ENTRYPOINT [\\"java\\", \\"-Dport=8080\\", \\"-jar\\", \\"app.jar\\"]" > Dockerfile.deploy

                            # We use '.' to specify that THIS directory is the context
                            docker build -t wilderchess-img -f Dockerfile.deploy .

                            docker run -d --name wilderchess-app -p 8086:8080 wilderchess-img
                        """
                    }
                }
            }
        }
    }
}
