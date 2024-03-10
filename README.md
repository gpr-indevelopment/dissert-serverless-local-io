# dissert-serverless-local-io
Source code for experiments for evaluating performance of local file system I/O workloads in serverless cloud environments.

This repository includes the source code for functions used in the I/O experiments and for the data collection orchestrator. It also includes the R source code for data analysis alongside raw collected data.

## Functions

* [docker-fio-lambda](./docker-fio-lambda): Lambda function based a custom image that comes with fio benchmark installed.
* [lambda-dd](./lambda-dd): Lambda function for running dd in the Lambda environment.
* [gcf-dd](./gcf-dd): Google Cloud Function for running dd in the GCF environment.
* [lambda-list-all-commands](./lambda-list-all-commands): Lambda function that outputs all executables on the underlying operating system of functions. Thats how we discovered dd was available.
* [gcf-list-all-commands](./gcf-list-all-commands): Google Cloud Function that outputs all executables on the underlying operating system of functions. Thats how we discovered dd was available.

## Other source code

* [dissert-exp-orchestrator](./dissert-exp-orchestrator): Java Spring Boot application that periodically calls functions for collecting data. The data gets saved to a PostgreSQL database in the cloud.
* [analysis](./analysis): R source code and raw data used for analysis.
