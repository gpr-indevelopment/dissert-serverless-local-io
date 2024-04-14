########################## Setup ##############################

## Package install
## install.packages("RPostgres")
## install.packages("hrbrthemes")
## install.packages("viridis")
## install.packages("dplyr")
## install.packages("tidyr")
## install.packages("cli")
require(RPostgres)
library(ggplot2)
library(gridExtra)
library(hrbrthemes)
library(dplyr)
library(tidyr)
library(viridis)
library(stringr)

# Database properties
dsn_database = "postgres"   
dsn_hostname = "*"  
dsn_port = 5432                
dsn_uid = "postgres"         
dsn_pwd = "*"
# Other props

getCon = function() {
  return(dbConnect(Postgres(), 
                   dbname = dsn_database,
                   host = dsn_hostname, 
                   port = dsn_port,
                   user = dsn_uid, 
                   password = dsn_pwd))
}  
  

########################## Global query vars ##############################
cutoffDate = "'2024-04-10'";
repetitions = 50;

########################## Common table expression (CTE) explanation #####################

# This query first uses a common table expression (CTE) ranked_data to assign a row number to each row within each 
# combination of resource_tier and system_name. The ORDER BY RANDOM() ensures that the row numbers are assigned 
# randomly within each group. Then, the main query selects rows where the row number is less than or equal to 50, 
# ensuring that each combination repeats exactly 50 times.

########################## WRITE #############################

minFileMinIoWriteQuery = function() {
  res = dbGetQuery(getCon(), stringr::str_interp("WITH ranked_data AS (
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 10000
        AND io_size_bytes = 512
        AND experiment_name = 'DIRECT_URANDOM_WRITE'
        AND occurred_at >= ${cutoffDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, repetitions=repetitions)))
  
  return(res);
}

maxFileMinIoWriteQuery = function() {
  res = dbGetQuery(getCon(), stringr::str_interp("WITH ranked_data AS (
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes = 512
        AND experiment_name = 'DIRECT_URANDOM_WRITE'
        AND occurred_at >= ${cutoffDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, repetitions=repetitions)))
  
  return(res);
}

maxFileMaxIoWriteQuery = function() {
  res = dbGetQuery(getCon(), stringr::str_interp("WITH ranked_data AS (
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes = 128000
        AND experiment_name = 'DIRECT_URANDOM_WRITE'
        AND occurred_at >= ${cutoffDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, repetitions=repetitions)))
  
  return(res);
}

########################## READ #############################

minFileMinIoReadQuery = function() {
  res = dbGetQuery(getCon(), stringr::str_interp("WITH ranked_data AS (
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'READ'
        AND status = 'SUCCESS'
        AND file_size_bytes = 10000
        AND io_size_bytes = 512
        AND experiment_name = 'DIRECT_READ'
        AND occurred_at >= ${cutoffDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, repetitions=repetitions)))
  
  return(res);
}

maxFileMinIoReadQuery = function() {
  res = dbGetQuery(getCon(), stringr::str_interp("WITH ranked_data AS (
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'READ'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes = 512
        AND experiment_name = 'DIRECT_READ'
        AND occurred_at >= ${cutoffDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, repetitions=repetitions)))
  
  return(res);
}

maxFileMaxIoReadQuery = function() {
  res = dbGetQuery(getCon(), stringr::str_interp("WITH ranked_data AS (
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'READ'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes = 128000
        AND experiment_name = 'DIRECT_READ'
        AND occurred_at >= ${cutoffDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, repetitions=repetitions)))
  
  return(res);
}
