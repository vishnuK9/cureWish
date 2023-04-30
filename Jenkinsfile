pipeline {
    agent any
    environment {
        AWS_ACCOUNT_ID="518234350460"
        AWS_DEFAULT_REGION="ap-south-1" 
	    CLUSTER_NAME="ecs-demo"
	    SERVICE_NAME="ecs-demo-service"
	    TASK_DEFINITION_NAME="ecs-demo-task"
	    DESIRED_COUNT="1"
        IMAGE_REPO_NAME="vishnu99docker/apache_image"
        IMAGE_TAG="${env.BUILD_ID}"
        // REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}"
	    // registryCredential = "CHANGE_ME"
    }
    stages {
        stage('Building image') {
            steps {
		        sh 'ls'
                sh 'docker build -t apache_image:${IMAGE_TAG} .'
                sh 'docker image tag apache_image:${IMAGE_TAG} vishnu99docker/apache_image:${IMAGE_TAG}'
            }
        }

        stage('Pushing image') {
            steps {
		        withCredentials([string(credentialsId: 'dockerhub', variable: 'dockerhub')]) {
                    // some block
                    sh 'docker login -u vishnu99docker -p ${dockerhub}'
                    sh 'docker image push vishnu99docker/apache_image:${IMAGE_TAG}'
                    sh 'docker image prune -a -y'
                }
            }
        }

        stage('Deploy') {
            steps{
                withAWS(credentials: 'ecs-demo-jenkins-user', region: "${AWS_DEFAULT_REGION}") {
                    script {
			            sh './script.sh'
                    }
                }    
            }
        }  

    }
}
