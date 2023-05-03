pipeline {
    agent any
    environment {
        AWS_ACCOUNT_ID="518234350460"
        AWS_DEFAULT_REGION="ap-south-1"
	    CLUSTER_NAME="ecs-demo"
	    SERVICE_NAME="service-2"
	    TASK_DEFINITION_NAME="ecs-demo-task"
	    DESIRED_COUNT="2"
        IMAGE_REPO_NAME="vishnu99docker/apache_image"
        IMAGE_TAG="${env.BUILD_ID}"
        REPOSITORY_URI = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com/${IMAGE_REPO_NAME}"
	    registryCredential = "CHANGE_ME"
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
                    sh 'docker image prune -a -f'
                }
            }
        }

        stage('Deploy') {
            steps{
                withAWS(credentials: 'demo-admin-user', region: "${AWS_DEFAULT_REGION}") {
                    script { 
                        sh '''
                            #!/bin/bash
                            ROLE_ARN=`aws ecs describe-task-definition --task-definition "${TASK_DEFINITION_NAME}" --region "${AWS_DEFAULT_REGION}" | jq .taskDefinition.executionRoleArn`
                            echo "ROLE_ARN= " $ROLE_ARN

                            FAMILY=`aws ecs describe-task-definition --task-definition "${TASK_DEFINITION_NAME}" --region "${AWS_DEFAULT_REGION}" | jq .taskDefinition.family`
                            echo "FAMILY= " $FAMILY

                            NAME=`aws ecs describe-task-definition --task-definition "${TASK_DEFINITION_NAME}" --region "${AWS_DEFAULT_REGION}" | jq .taskDefinition.containerDefinitions[].name`
                            echo "NAME= " $NAME

                            sed -i "s#BUILD_NUMBER#$IMAGE_TAG#g" task-definition.json
                            sed -i "s#REPOSITORY_URI#$IMAGE_REPO_NAME#g" task-definition.json
                            sed -i "s#ROLE_ARN#$ROLE_ARN#g" task-definition.json
                            sed -i "s#FAMILY#$FAMILY#g" task-definition.json
                            sed -i "s#NAME#$NAME#g" task-definition.json


                            aws ecs register-task-definition --cli-input-json file://task-definition.json --region "${AWS_DEFAULT_REGION}"

                            REVISION=`aws ecs describe-task-definition --task-definition "${TASK_DEFINITION_NAME}" --region "${AWS_DEFAULT_REGION}" | jq .taskDefinition.revision`
                            echo "REVISION= " "${REVISION}"
                            aws ecs update-service --cluster "${CLUSTER_NAME}" --service "${SERVICE_NAME}" --task-definition "${TASK_DEFINITION_NAME}":"${REVISION}" --desired-count "${DESIRED_COUNT}"
                        '''
                    }
                }    
            }
        }  

    }
}
