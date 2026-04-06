pipeline {
  agent any

  tools {
    maven 'Maven'
  }

  stages {
    stage('Clone repo') {
      steps {
        checkout scm
      }
    }

    stage('Build backend') {
      steps {
        dir('backend') {
          bat 'mvn -B clean verify'
        }
      }
    }

    stage('Build frontend') {
      steps {
        dir('frontend') {
          bat 'npm install'
          bat 'npm run build'
        }
      }
    }

    stage('Docker build') {
      steps {
        bat 'docker-compose build'
      }
    }

    stage('Deploy') {
      steps {
        bat 'docker-compose down -v || exit 0'
        bat 'docker-compose up -d --build'
      }
    }
  }
}