stage('Build & Deploy Java App') {
    when {
        allOf {
            branch 'dev'
            changeset "services/wilderchess/**"
        }
    }
    steps {
        // This runs the build inside a temporary Maven container
        docker.image('maven:3.9.6-eclipse-temurin-21').inside('-v /root/.m2:/root/.m2') {
            dir('services/wilderchess') {
                sh 'mvn clean package'
            }
        }

        // This part runs on the host (Jenkins node) to handle Docker commands
        script {
            dir('services/wilderchess') {
                sh 'docker stop wilderchess-app || true'
                sh 'docker rm wilderchess-app || true'
                sh '''
                    docker run -d \
                    --name wilderchess-app \
                    -p 8081:8080 \
                    -v /workspaces/aws-org-workspace:/app \
                    --restart unless-stopped \
                    openjdk:17-jdk-slim \
                    java -Dport=8080 -jar /app/services/wilderchess/target/wilderchess-app.jar
                '''
            }
        }
    }
}
