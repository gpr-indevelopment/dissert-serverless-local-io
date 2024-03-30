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

# Database properties
dsn_database = "postgres"   
dsn_hostname = "*"  
dsn_port = 5432                
dsn_uid = "postgres"         
dsn_pwd = "*"
# Other props

con = dbConnect(Postgres(), 
                   dbname = dsn_database,
                   host = dsn_hostname, 
                   port = dsn_port,
                   user = dsn_uid, 
                   password = dsn_pwd)

densityMinFileAndMinIoWrite = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'WRITE'
	and status = 'SUCCESS'
  and file_size_bytes = 10000
  and io_size_bytes = 500
  and command like '%urandom%'")
  
  ggplot(data=res, aes(x=latency_seconds*1000, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    xlim(0, 1) + ggtitle("Write latency for a 10 KB file and 500 B I/O size") + labs(x="Latency (ms)", y="Density", fill="Provider") + theme_bw()
}

densityMaxFileAndMinIoWrite = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'WRITE'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes = 500
  and command like '%urandom%'")
  
  ggplot(data=res, aes(x=latency_seconds, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    xlim(5, 14) + ggtitle("Write latency for a 1 GB file and 500 B I/O size") + labs(x="Latency (s)", y="Density", fill="Provider") + theme_bw()
}

densityMaxFileAndMaxIoWrite = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'WRITE'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes = 128000
  and command like '%urandom%'")
  
  ggplot(data=res, aes(x=latency_seconds, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    ggtitle("Write latency for a 1 GB file and 128 KB I/O size") + labs(x="Latency (s)", y="Density", fill="Provider") + theme_bw()
}

densityMinFileAndMinIoWrite()
densityMaxFileAndMinIoWrite()
densityMaxFileAndMaxIoWrite()

######################## READ #############################################

densityMinFileAndMinIoRead = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'READ'
	and status = 'SUCCESS'
  and file_size_bytes = 10000
  and io_size_bytes = 500")
  
  ggplot(data=res, aes(x=latency_seconds*1000, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    xlim(0, 0.5) + ggtitle("Read latency for a 10 KB file and 500 B I/O size") + labs(x="Latency (ms)", y="Density", fill="Provider") + theme_bw()
}

densityMaxFileAndMinIoRead = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'READ'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes = 500")
  
  ggplot(data=res, aes(x=latency_seconds, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    xlim(1, 6) + ggtitle("Read latency for a 1 GB file and 500 B I/O size") + labs(x="Latency (s)", y="Density", fill="Provider") + theme_bw()
}

densityMaxFileAndMaxIoRead = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'READ'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes = 128000")
  
  ggplot(data=res, aes(x=latency_seconds, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    xlim(0, 0.2) + ggtitle("Read latency for a 1 GB file and 128 KB I/O size") + labs(x="Read latency (s)", y="Density", fill="Provider") + theme_bw()
}

densityMinFileAndMinIoRead()
densityMaxFileAndMinIoRead()
densityMaxFileAndMaxIoRead()

## Box plots

boxplotMinFileAndMinIoWrite = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'WRITE'
	and status = 'SUCCESS'
  and file_size_bytes = 10000
  and io_size_bytes = 500
  and command like '%urandom%'")
  
  ggplot(data=res, aes(y=latency_seconds*1000, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    ylim(0,1) + #limits it between 0 and 1, and hides some of the values
    ggtitle("Write latency for a 10 KB file and 500 B I/O size") + labs(x="Resource tier", y="Latency (ms)", fill="Provider") + theme_bw()
}

boxplotMaxFileAndMinIoWrite = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'WRITE'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes = 500
  and command like '%urandom%'")
  
  ggplot(data=res, aes(y=latency_seconds, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    ggtitle("Write latency for a 1 GB file and 500 B I/O size") + labs(x="Resource tier", y="Latency (s)", fill="Provider") + theme_bw()
  
}

boxplotMaxFileAndMaxIoWrite = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'WRITE'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes = 128000
  and command like '%urandom%'")
  
  ggplot(data=res, aes(y=latency_seconds, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    ggtitle("Write latency for a 1 GB file and 128 KB I/O size") + labs(x="Resource tier", y="Latency (s)", fill="Provider") + theme_bw()
}

boxplotMinFileAndMinIoWrite()
boxplotMaxFileAndMinIoWrite()
boxplotMaxFileAndMaxIoWrite()

######################## READ #############################################

boxplotMinFileAndMinIoRead = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'READ'
	and status = 'SUCCESS'
  and file_size_bytes = 10000
  and io_size_bytes = 500")
  
  ggplot(data=res, aes(y=latency_seconds*1000, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    coord_cartesian(ylim = c(0, 0.3)) + 
    ggtitle("Read latency for a 10 KB file and 500 B I/O size") + labs(x="Resource tier", y="Latency (ms)", fill="Provider") + theme_bw()
}

boxplotMaxFileAndMinIoRead = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'READ'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes = 500")
  
  ggplot(data=res, aes(y=latency_seconds, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    coord_cartesian(ylim = c(0, 6)) +
    ggtitle("Read latency for a 1 GB file and 500 B I/O size") + labs(x="Resource tier", y="Latency (s)", fill="Provider") + theme_bw()
}

boxplotMaxFileAndMaxIoRead = function() {
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'READ'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes = 128000")
  
  ggplot(data=res, aes(y=latency_seconds, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    coord_cartesian(ylim = c(0, 0.15)) +
    ggtitle("Read latency for a 1 GB file and 128 KB I/O size") + labs(x="Resource tier", y="Latency (s)", fill="Provider") + theme_bw()
}

boxplotMinFileAndMinIoRead()
boxplotMaxFileAndMinIoRead()
boxplotMaxFileAndMaxIoRead()