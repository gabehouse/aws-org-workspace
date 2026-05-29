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
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        // 1. Create a tiny Dockerfile dynamically
                        // 2. Build the image (this puts the JAR inside the image)
                        // 3. Run it without needing a volume mount
                        sh '''
                            echo "FROM openjdk:17-jdk-slim
                            COPY target/wilderchess-app.jar app.jar
                            ENTRYPOINT [\\"java\\", \\"-Dport=8080\\", \\"-jar\\", \\"app.jar\\"]" > Dockerfile.deploy

                            docker build -t wilderchess-img -f Dockerfile.deploy .
                            docker run -d --name wilderchess-app -p 8081:8080 wilderchess-img
                        '''
                    }
                }
            }
        }
    }
}
