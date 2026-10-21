pipeline {
    agent any

    environment {
        JAVA_HOME = tool name: 'jdk21', type: 'jdk'
    }

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './gradlew --no-daemon clean build -x test'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew --no-daemon test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh './gradlew --no-daemon bootJar'
                archiveArtifacts artifacts: '*/build/libs/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/build/reports/tests/**/*.html', allowEmptyArchive: true
            archiveArtifacts artifacts: '**/build/reports/problems/*.html', allowEmptyArchive: true
            cleanWs()
        }
        failure {
            emailext(
                subject: "Pipeline failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build ${env.BUILD_URL} failed. Check the test reports and console output.",
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
    }
}
