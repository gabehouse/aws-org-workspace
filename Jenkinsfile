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
                    docker.image('maven:3.8.5-openjdk-8').inside('-v /root/.m2:/root/.m2') {
                        dir('services/wilderchess') {
                            // Build the Uber-JAR specifically
                            sh 'mvn clean package -DskipTests'
                        }
                    }

                    dir('services/wilderchess') {
                        sh 'docker stop wilderchess-app || true'
                        sh 'docker rm wilderchess-app || true'

                        // Build the deployment image using the JAR we just made
                        sh '''
                            echo "FROM openjdk:8-jre-slim
                            COPY target/wilderchess-app.jar app.jar
                            EXPOSE 8080
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
