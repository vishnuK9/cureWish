pipeline {
    agent any
    environment {
        AWS_ACCOUNT_ID="518234350460"
        AWS_DEFAULT_REGION="ap-south-1"
	    CLUSTER_NAME="ecs-demo"
	    SERVICE_NAME="service-2"
	    TASK_DEFINITION_NAME="ecs-demo-task"
	    DESIRED_COUNT="2"
        IMAGE_REPO_NAME="apache"
        IMAGE_TAG="${env.BUILD_ID}"
        REPOSITORY_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}"
	    registryCredential = "CHANGE_ME"
    }
    stages {
        stage('Building image') {
            steps {
                sh 'docker build -t ${REPOSITORY_URI}:latest .'
            }
        }
        stage('Pushing image') {
            steps {
                withAWS(credentials: 'demo-admin-user', region: "${AWS_DEFAULT_REGION}") {
                    script { 
                        sh 'aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin 518234350460.dkr.ecr.ap-south-1.amazonaws.com'
                        sh 'docker push ${REPOSITORY_URI}:latest'
                        sh 'docker system prune -f'
                    }
                }
            }
        }
        stage('Deploy') {
            steps{
                withAWS(credentials: 'demo-admin-user', region: "${AWS_DEFAULT_REGION}") {
                    script { 
                        sh './script.sh'
                    }
                }    
            }
        }  

    }
}
