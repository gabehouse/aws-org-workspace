pipeline {
    agent any
    stages {
        stage('Build & Deploy') {
            steps {
                script {
                    dir('services/wilderchess') {
                        // Run Maven via a one-off docker run command instead of the 'inside' DSL
                        sh '''
                            docker run --rm \
                            -v /root/.m2:/root/.m2 \
                            -v $(pwd):/app \
                            -w /app \
                            maven:3.8.5-openjdk-8 mvn clean package -DskipTests
                        '''

                        // Now proceed with building the deployment image
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
