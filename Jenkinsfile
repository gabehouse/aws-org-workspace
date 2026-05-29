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
                    def WSL_PATH = "/home/g/workspace/aws-org-workspace/services/wilderchess"

                    dir('services/wilderchess') {
                        // 1. Run Maven Build
                        // We removed the named volume. We just use the WSL_PATH mount.
                        sh """
                            docker run --name maven-build-container \
                            -v /root/.m2:/root/.m2 \
                            -v ${WSL_PATH}:/app \
                            -w /app \
                            maven:3.9.6-eclipse-temurin-17 mvn clean package -DskipTests
                        """

                        // 2. The "Direct Extraction"
                        // We use docker cp to grab the file directly from the container we just ran
                        sh """
                            mkdir -p target
                            docker cp maven-build-container:/app/target/wilderchess-app.jar ./target/wilderchess-app.jar
                            docker rm maven-build-container
                        """

                        sh 'docker stop wilderchess-app || true && docker rm wilderchess-app || true'

                        // 3. Standard Docker Build
                        sh '''
                            echo "FROM eclipse-temurin:17-jre-alpine
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
