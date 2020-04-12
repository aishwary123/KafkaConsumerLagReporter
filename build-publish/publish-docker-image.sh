#!/bin/bash
set -e
VERSION=$1
PUSH_IMAGE=$2

# Validate no arguments input
if [ $# -eq 0 ]
  then
    echo "No arguments passed. Image version number is expected"
	exit 1
fi

LABEL="kafka-consumer-lag-reporter"
REPO_PATH="<REPO_PATH>"
REPO_USERNAME="<REPO_USERNAME>"
REPO_PASSWORD="<REPO_PASSWORD>"
TAG="$LABEL:$VERSION"

# Login to Artifactory docker repo
docker login $REPO_PATH --username $REPO_USERNAME  --password $REPO_PASSWORD

# Build the image
docker build --label $LABEL --tag $REPO_PATH/$TAG --file ../Dockerfile ../.

# Push the image by default or specified specifically
if [[ ! $PUSH_IMAGE ]] || [[ $PUSH_IMAGE = true ]]; then
    docker push $REPO_PATH/$TAG
fi

# Remove the local image
docker rmi $REPO_PATH/$TAG
