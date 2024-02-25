## Requirements

Dockerfile needs a GCF credentials file to authenticate the SDK. 
It expects a file named `gcf-creds.json` placed on the project root.
Refer to the [GCF docs](https://cloud.google.com/docs/authentication/provide-credentials-adc?hl=pt-br#wlif-key) on how to create this credentials file.

## Running the orchestrator in the cloud

This orchestrator ran in a free tier EC2 instance using Amazon Linux 2023.
Documentation:

* [Installing docker in the instance](https://docs.aws.amazon.com/pt_br/serverless-application-model/latest/developerguide/install-docker.html#install-docker-instructions).
* [Install Amazon ECR Credential Helper](https://github.com/awslabs/amazon-ecr-credential-helper?tab=readme-ov-file#configuration) to Amazon Linux 2023 and add a role to EC2 that allows downloading from ECR. Follow [this SO thread](https://stackoverflow.com/a/71194886).
* Docker needs environment variables to run. Refer to the [scp docs](https://unix.stackexchange.com/a/416490) on how to transfer files through SSH.
* Docker allows for passing an env variable file when running the container. Refer to the [Docker docs](https://docs.docker.com/compose/environment-variables/set-environment-variables/#substitute-with---env-file) on how to do it.

## Database

The orchestrator running in the cloud uses a Postgres RDS instance in the free tier.
Make it publicly accessible when creating the instance, and attach a security group to it that allows all traffic. 
This is not a best practice but was done for this experiment for the sake of simplicity.
This application uses H2 for testing and Postgres for production.

## Steps to running the Orchestrator in the cloud

1. Build a new image with `mvn clean install`.
2. Deploy the new image to ECR. Refer to the AWS console ECR page on how to do it.
3. Download the new image in EC2 instance from ECR using `docker image pull ${ECR_IMAGE_NAME}`.
4. Copy the environment variables file from local machine to the EC2 instance using SCP.
5. Run the application with the environment variables file. Eg. `docker run --env-file ./docker.env -d --name orch ${ECR_IMAGE_NAME}`.

