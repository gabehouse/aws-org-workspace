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
                    dir('services/wilderchess') {
                        // 1. Remove old instances
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        // 2. Build a small image with the JAR baked in
                        // This solves the 'No such container' and 'File not found' issues
                        sh '''
                            echo "FROM openjdk:17-jdk-slim
                            COPY target/wilderchess-app.jar /app.jar
                            EXPOSE 8080
                            ENTRYPOINT [\\"java\\", \\"-Dport=8080\\", \\"-jar\\", \\"/app.jar\\"]" > Dockerfile.deploy

                            docker build -t wilderchess-img -f Dockerfile.deploy .
                            docker run -d --name wilderchess-app -p 8081:8080 wilderchess-img
                        '''
                    }
                }
            }
        }
    }
}
