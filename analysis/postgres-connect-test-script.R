## Package install
## install.packages("RPostgres")
## install.packages("hrbrthemes")
## install.packages("viridis")
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
  geom_density(adjust=1.5, alpha=.4) +
  xlim(0, 1) + ggtitle("Read latency") + labs(x="Read latency (ms)", y="Frequency") + theme_bw()
