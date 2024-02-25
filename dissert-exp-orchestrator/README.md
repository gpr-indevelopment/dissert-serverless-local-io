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



