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
	    // registryCredential = "CHANGE_ME"
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
                        sh '''
                            current_task_definition=$(
                                aws ecs describe-task-definition \
                                --task-definition "${TASK_DEFINITION_NAME}" \
                                --query '{  containerDefinitions: taskDefinition.containerDefinitions,
                                family: taskDefinition.family,
                                executionRoleArn: taskDefinition.executionRoleArn,
                                networkMode: taskDefinition.networkMode,
                                volumes: taskDefinition.volumes,
                                placementConstraints: taskDefinition.placementConstraints,
                                requiresCompatibilities: taskDefinition.requiresCompatibilities,
                                cpu: taskDefinition.cpu,
                                memory: taskDefinition.memory }'
                            )
                            current_task_definition_revision=$(
                                aws ecs describe-task-definition --task-definition "${TASK_DEFINITION_NAME}" \
                               --query 'taskDefinition.revision'
                            )

                            updated_task_definition=$(echo "${current_task_definition}" | jq --arg CONTAINER_IMAGE "${REPOSITORY_URI}" '.containerDefinitions[0].image = $CONTAINER_IMAGE')
                            updated_task_definition_info=$(aws ecs register-task-definition --cli-input-json "${updated_task_definition}")

                            updated_task_definition_revision=$(echo "${updated_task_definition_info}" | jq '.taskDefinition.revision')
                            aws ecs update-service --cluster "${CLUSTER_NAME}" --service "${SERVICE_NAME}" --task-definition "${TASK_DEFINITION_NAME}:${updated_task_definition_revision}" --desired-count "${DESIRED_COUNT}"
                            
                            mssg=$(aws ecs wait services-stable --cluster ecs-demo --service service-2)
                            if [ mssg == "" ]
                            then
                                echo $a
                                aws ecs update-service --cluster "${CLUSTER_NAME}" --service "${SERVICE_NAME}" --task-definition "${TASK_DEFINITION_NAME}:${current_task_definition_revision}" --desired-count "${DESIRED_COUNT}"
                            fi
                        '''
                    }
                }    
            }
        }  
    }
    post {
            failure {
                // Rollback to the previous task definition in case of deployment failure
                sh 'aws ecs update-service --cluster "${CLUSTER_NAME}" --service "${SERVICE_NAME}" --task-definition "${TASK_DEFINITION_NAME}:${current_task_definition_revision}" --desired-count "${DESIRED_COUNT}"'
            } 
    }
}