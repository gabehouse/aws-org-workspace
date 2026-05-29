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
                    // 1. The WSL path for the SOURCE code
                    def WSL_PATH = "/home/g/workspace/aws-org-workspace/services/wilderchess"

                    dir('services/wilderchess') {
                        // 2. Build the JAR, but save it to a NAMED DOCKER VOLUME called 'build-artifacts'
                        sh """
                            docker run --rm \
                            -v /root/.m2:/root/.m2 \
                            -v ${WSL_PATH}:/app \
                            -v build-artifacts:/app/target \
                            -w /app \
                            maven:3.9.6-eclipse-temurin-17 mvn clean package -DskipTests
                        """

                        sh 'docker stop wilderchess-app || true && docker rm wilderchess-app || true'

                        // 3. The Build: We tell Docker to build an image, but we pull the
                        // JAR out of that named volume.
                        sh '''
                            # Create a temporary container to extract the JAR from the volume
                            docker create --name artifact-helper -v build-artifacts:/data alpine
                            mkdir -p target
                            docker cp artifact-helper:/data/wilderchess-app.jar ./target/wilderchess-app.jar
                            docker rm artifact-helper

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
