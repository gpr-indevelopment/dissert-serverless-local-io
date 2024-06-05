# dissert-serverless-local-io

Source code for experiments for evaluating performance of local file system I/O workloads in serverless cloud environments of AWS Lambda and Google Cloud Functions.

This repository includes the source code for functions used in the I/O experiments and for the data collection orchestrator. It also includes the R source code for data analysis alongside raw collected data.

## Reproducibility package

This reproducibility package has the following pre-requisites:

1. [Docker](https://docs.docker.com/engine/install/)
2. [RStudio](https://posit.co/download/rstudio-desktop/)

### Preliminary experiments

The preliminary experiment's goal is to verify if time of day and day of week factors are statistically significant for local file system I/O workloads in AWS Lambda and Google Cloud Functions. The experiment data was saved as a CSV file. The R scripts can connect to this CSV file on local environment to produce visualization (histograms, ECDFs, etc) based on its data:

1. Open [any of the R Markdown files ](/analysis/preliminary-exp/) (`*.Rmd`) for the preliminary experiment in RStudio.
2. Click the knit button. This will read the CSV file and generate the data visualization to a PDF file.

### Main experiment

This experiment includes data from running local file system I/O workloads in AWS Lambda and Google Cloud Functions using files from 10 KB to 1 GB, with I/O sizes ranging from 512 B to 128 KB. This data was collected with minimum and maximum compatible resource allocation between these platforms.

This reproducibility package includes a custom Docker image that contains a PostgreSQL database pre-loaded with all experiment data. In addition, the R scripts can connect to this database on local environment to produce visualization (histograms, ECDFs, etc) based on its data:

1. Run the custom PostgreSQL on local using the shell command below. This will run a database named `postgres` on `localhost:5432`. Its username and password are `postgres` and `local-db-pw`, respectively.

```
docker run --rm -e POSTGRES_PASSWORD=local-db-pw -p 5432:5432 pimentgabriel/serverless-local-io-db
```

2. Then, open the [R Markdown](/analysis/main-exp/Dissert-final-exp.Rmd) from the main experiment and knit using RStudio. This will connect to the database on local and generate the data visualization to a PDF file.

## Functions

- [docker-fio-lambda](./docker-fio-lambda): Lambda function based a custom image that comes with fio benchmark installed.
- [lambda-dd](./lambda-dd): Lambda function for running dd in the Lambda environment.
- [gcf-dd](./gcf-dd): Google Cloud Function for running dd in the GCF environment.
- [lambda-list-all-commands](./lambda-list-all-commands): Lambda function that outputs all executables on the underlying operating system of functions. Thats how we discovered dd was available.
- [gcf-list-all-commands](./gcf-list-all-commands): Google Cloud Function that outputs all executables on the underlying operating system of functions. Thats how we discovered dd was available.

## Other source code

- [dissert-exp-orchestrator](./dissert-exp-orchestrator): Java Spring Boot application that periodically calls functions for collecting data. The data gets saved to a PostgreSQL database in the cloud.
- [analysis](./analysis): R source code and raw data used for analysis.
