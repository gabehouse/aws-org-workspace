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
                        // Step 1: Maven Build (Outputs to WSL Host)
                        sh """
                            docker run --rm \
                            -v /root/.m2:/root/.m2 \
                            -v ${WSL_PATH}:/app \
                            -w /app \
                            maven:3.9.6-eclipse-temurin-17 mvn clean package -DskipTests
                        """

                        // Step 2: "The Rescue"
                        // This container maps BOTH the WSL path and the Jenkins path
                        // to move the JAR into Jenkins' view.
                        sh """
                            docker run --rm \
                            -v ${WSL_PATH}/target:/source \
                            -v \$(pwd)/target:/dest \
                            alpine cp /source/wilderchess-app.jar /dest/wilderchess-app.jar
                        """

                        // Step 3: Cleanup and Standard Build
                        sh 'docker stop wilderchess-app || true && docker rm wilderchess-app || true'

                        sh '''
                            echo "FROM eclipse-temurin:17-jre-alpine
                            COPY target/wilderchess-app.jar app.jar
                            ENTRYPOINT [\\"java\\", \\"-Dport=8080\\", \\"-jar\\", \\"app.jar\\"]" > Dockerfile.deploy

                            # Now we don't need WSL_PATH here! Jenkins sees the 'target' folder locally now.
                            docker build -t wilderchess-img -f Dockerfile.deploy .
                            docker run -d --name wilderchess-app -p 8086:8080 wilderchess-img
                        '''
                    }
                }
            }
        }
    }
}
