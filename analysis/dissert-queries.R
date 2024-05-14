########################## Setup ##############################
library(stringr)
library(rstudioapi)

curDir = rstudioapi::getActiveDocumentContext()$path 
source(file.path(dirname(curDir), "dissert-db-con.R"))

########################## Global query vars ##############################
cutoffDate = "'2024-04-10'";
limitDate = "'2024-05-11'"
repetitions = 150;
writeExperiment = "'URANDOM_WRITE'"

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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 10000
        AND io_size_bytes = 512
        AND experiment_name = ${writeExperiment}
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions, writeExperiment=writeExperiment)))
  
  return(res);
}

minFileMinIoMinMaxResourceWriteQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 10000
        AND io_size_bytes = 512
        AND experiment_name = ${writeExperiment}
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
        AND resource_tier in ('TIER_1', 'TIER_5')
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions, writeExperiment=writeExperiment)))
  
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes = 512
        AND experiment_name = ${writeExperiment}
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions, writeExperiment=writeExperiment)))
  
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes = 128000
        AND experiment_name = ${writeExperiment}
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions, writeExperiment=writeExperiment)))
  
  return(res);
}

maxFileMinMaxIoWriteQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes in (512, 128000)
        AND experiment_name = ${writeExperiment}
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions, writeExperiment=writeExperiment)))
  
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
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
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions)))
  
  return(res);
}

minFileMinIoMinMaxResourceReadQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
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
        AND occurred_at <= ${limitDate}
        AND resource_tier in ('TIER_1', 'TIER_5')
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions)))
  
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
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
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions)))
  
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
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
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions)))
  
  return(res);
}

maxFileMinMaxIoReadQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'READ'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes in (512, 128000)
        AND experiment_name = 'DIRECT_READ'
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions)))
  
  return(res);
}


########################## ANOVA WRITE ########################## 

bigFileWriteAnovaQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, io_size_bytes, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, io_size_bytes, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes in (512, 128000)
        AND experiment_name = ${writeExperiment}
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, repetitions=repetitions, limitDate=limitDate, writeExperiment=writeExperiment)))
  
  return(res);
}

smallFileWriteAnovaQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, io_size_bytes, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, io_size_bytes, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND file_size_bytes = 10000
        AND io_size_bytes = 512
        and resource_tier in ('TIER_1', 'TIER_5')
        AND experiment_name = ${writeExperiment}
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, repetitions=repetitions, limitDate=limitDate, writeExperiment=writeExperiment)))
  
  return(res);
}
########################## ANOVA READ ########################## 

bigFileReadAnovaQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, io_size_bytes, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, io_size_bytes, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'READ'
        AND status = 'SUCCESS'
        AND file_size_bytes = 1024000000
        AND io_size_bytes in (512, 128000)
        AND experiment_name = 'DIRECT_READ'
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions)))
  
  return(res);
}

smallFileReadAnovaQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, io_size_bytes, system_name) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, io_size_bytes, system_name ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'READ'
        AND status = 'SUCCESS'
        AND file_size_bytes = 10000
        AND io_size_bytes = 512
        and resource_tier in ('TIER_1', 'TIER_5')
        AND experiment_name = 'DIRECT_READ'
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions)))
  
  return(res);
}

########################## CV ########################## 

cvWriteQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name, file_size_bytes, io_size_bytes) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name, file_size_bytes, io_size_bytes ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'WRITE'
        AND status = 'SUCCESS'
        AND experiment_name = ${writeExperiment}
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions, writeExperiment=writeExperiment)))
  
  return(res);
}

cvReadQuery = function() {
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
    		DENSE_RANK() OVER (ORDER BY resource_tier, system_name, file_size_bytes, io_size_bytes) AS group_id,
        ROW_NUMBER() OVER (PARTITION BY resource_tier, system_name, file_size_bytes, io_size_bytes ORDER BY occurred_at DESC) AS row_num
    FROM
        public.dd_experiment_entity en
        JOIN dd_experiment_result_entity res ON en.id = res.experiment_id
    WHERE
        operation_type = 'READ'
        AND status = 'SUCCESS'
        AND experiment_name = 'DIRECT_READ'
        AND occurred_at >= ${cutoffDate}
        AND occurred_at <= ${limitDate}
    )
    SELECT
        latency_seconds,
        resource_tier,
        system_name,
        file_size_bytes,
        io_size_bytes,
        experiment_name,
        status,
        occurred_at,
        group_id
    FROM
        ranked_data
    WHERE
        row_num <= ${repetitions}", 
    list(cutoffDate=cutoffDate, limitDate=limitDate, repetitions=repetitions, writeExperiment=writeExperiment)))
  
  return(res);
}