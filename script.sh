# ./bin/deploy
# …
# fetch current task definition
current_task_definition=$(
  aws ecs describe-task-definition \
    --task-definition "$TASK_DEFINITION_NAME" \
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
  aws ecs describe-task-definition --task-definition "$TASK_DEFINITION_NAME" \
                                   --query 'taskDefinition.revision'
)

# compare current and updated image tags
# current_container_image="$(echo "$current_task_definition" | jq .containerDefinitions[0].image)"
# updated_container_image="$ecr_repo:$unique_image_tag"
 
# if [[ $current_container_image = "\"$updated_container_image\"" ]]; then
#   echo "Container image '$unique_image_tag' already defined in the latest task definition revision: $task_definition_family:$current_task_definition_revision"
#   read -p "Are you sure you want to deploy?" -n 1 -r
#   if [[ ! $REPLY =~ ^[Yy]$ ]]
#   then
#     exit 1
#   fi
# fi

# inject new image tag into task definition and update
updated_task_definition=$(
  echo "$current_task_definition" | jq --arg CONTAINER_IMAGE "$REPOSITORY_URI" '.containerDefinitions[0].image = $CONTAINER_IMAGE'
)
updated_task_definition_info=$(aws ecs register-task-definition --cli-input-json "$updated_task_definition")
 
# update service with new task definition revision
updated_task_definition_revision=$(echo "$updated_task_definition_info" | jq '.taskDefinition.revision')
aws ecs update-service --cluster "$CLUSTER_NAME" \
                       --service "$SERVICE_NAME" \
                       --task-definition "$TASK_DEFINITION_NAME:$updated_task_definition_revision" \
                       --deployment-configuration "deploymentCircuitBreaker={enable=true,rollback=true}" \
                       --maximumPercent 200 \
                       --minimumHealthyPercent 100\
                       >/dev/null