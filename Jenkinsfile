pipeline {
    agent any
    environment {
        AWS_ACCOUNT_ID="CHANGE_ME"
        AWS_DEFAULT_REGION="CHANGE_ME" 
	     CLUSTER_NAME="CHANGE_ME"
	    SERVICE_NAME="CHANGE_ME"
	    TASK_DEFINITION_NAME="CHANGE_ME"
	    DESIRED_COUNT="CHANGE_ME"
        IMAGE_REPO_NAME="CHANGE_ME"
        IMAGE_TAG="${env.BUILD_ID}"
        REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}"
	    registryCredential = "CHANGE_ME"
    }
    stages {
        stage('Building image') {
            steps {
		        sh 'ls'
                sh 'docker build -t apache_image:${IMAGE_TAG} .'
                sh 'docker image tag apache_image:${IMAGE_TAG} vishnu99docker/apache_image:latest'
            }
        }

        stage('Pushing image') {
            steps {
		        withCredentials([string(credentialsId: 'dockerhub', variable: 'dockerhub')]) {
                    // some block
                    sh 'docker login -u vishnu99docker -p ${dockerhub}'
                    sh 'docker image push vishnu99docker/apache_image:latest'
                    sh 'docker image prune -a'
                }
            }
        }
    }
}
