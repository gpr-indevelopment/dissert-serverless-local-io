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

bigFileWriteAnova = function() {
  print("1 GB file for WRITE operations")
  res = dbGetQuery(con, "SELECT io_size_bytes, latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'WRITE'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes in (500, 128000)
  and command like '%urandom%'")
  
  res$io_size_bytes_fac = factor(res$io_size_bytes)
  
  res.aov = aov(latency_seconds ~ system_name * io_size_bytes_fac, data=res)
  print(summary(res.aov))
  
  SS = anova(res.aov)["Sum Sq"]
  SST = sum(SS)
  round(100*SS/sum(SS), 2)
  
  tukey = TukeyHSD(res.aov, conf.level = 0.95)
  print(tukey)
  plot(tukey,las=1,tcl = -.6)
}

smallFileWriteAnova = function() {
  print("10 KB file for WRITE operations")
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'WRITE'
	and status = 'SUCCESS'
  and file_size_bytes = 10000
  and io_size_bytes = 500
  and resource_tier in ('TIER_1', 'TIER_5')
  and command like '%urandom%'")
  
  res.aov = aov(latency_seconds ~ system_name * resource_tier, data=res)
  print(summary(res.aov))
  
  SS = anova(res.aov)["Sum Sq"]
  SST = sum(SS)
  round(100*SS/sum(SS), 2)
  
  tukey = TukeyHSD(res.aov, conf.level = 0.95)
  print(tukey)
  plot(tukey,las=1,tcl = -.6)
}

bigFileWriteAnova()
smallFileWriteAnova()

############################### READ ###############################
bigFileReadAnova = function() {
  print("1 GB file for READ operations")
  res = dbGetQuery(con, "SELECT io_size_bytes, latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'READ'
	and status = 'SUCCESS'
  and file_size_bytes = 1024000000
  and io_size_bytes in (500, 128000)")
  
  res$io_size_bytes_fac = factor(res$io_size_bytes)
  
  res.aov = aov(latency_seconds ~ system_name * io_size_bytes_fac, data=res)
  print(summary(res.aov))
  
  SS = anova(res.aov)["Sum Sq"]
  SST = sum(SS)
  round(100*SS/sum(SS), 2)
  
  tukey = TukeyHSD(res.aov, conf.level = 0.95)
  print(tukey)
  plot(tukey,las=1,tcl = -.6)
}

smallFileReadAnova = function() {
  print("10 KB file for READ operations")
  res = dbGetQuery(con, "SELECT latency_seconds, resource_tier, system_name from public.dd_experiment_entity en
	join dd_experiment_result_entity res
		on en.id = res.experiment_id
	where operation_type = 'READ'
	and status = 'SUCCESS'
  and file_size_bytes = 10000
  and io_size_bytes = 500
  and resource_tier in ('TIER_1', 'TIER_5')")
  
  res.aov = aov(latency_seconds ~ system_name * resource_tier, data=res)
  print(summary(res.aov))
  
  SS = anova(res.aov)["Sum Sq"]
  SST = sum(SS)
  round(100*SS/sum(SS), 2)
  
  tukey = TukeyHSD(res.aov, conf.level = 0.95)
  print(tukey)
  plot(tukey,las=1,tcl = -.6)
}

bigFileReadAnova()
smallFileReadAnova()