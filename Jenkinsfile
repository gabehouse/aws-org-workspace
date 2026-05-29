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
                    // 1. Define the WSL path only for the Docker command to use
                    def WSL_PATH = "/home/g/workspace/aws-org-workspace/services/wilderchess"

                    dir('services/wilderchess') {
                        // Build the JAR (This works because we map WSL_PATH to /app)
                        sh """
                            docker run --rm \
                            -v /root/.m2:/root/.m2 \
                            -v ${WSL_PATH}:/app \
                            -w /app \
                            maven:3.9.6-eclipse-temurin-17 mvn clean package -DskipTests
                        """

                        // Cleanup
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        // 2. Write the Dockerfile to the CURRENT directory (Jenkins Workspace)
                        // Jenkins CAN see this path!
                        sh '''
                            echo "FROM eclipse-temurin:17-jre-alpine
                            COPY target/wilderchess-app.jar app.jar
                            ENTRYPOINT [\\"java\\", \\"-Dport=8080\\", \\"-jar\\", \\"app.jar\\"]" > Dockerfile.deploy
                        '''

                        // 3. The "Magic Trick": Use -f to point to the local Dockerfile,
                        // but use WSL_PATH as the build context for the JAR.
                        sh "docker build -t wilderchess-img -f Dockerfile.deploy ${WSL_PATH}"

                        sh "docker run -d --name wilderchess-app -p 8086:8080 wilderchess-img"
                    }
                }
            }
        }
    }
}
