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

# Database properties
dsn_database = "postgres"   
dsn_hostname = "localhost"  
dsn_port = 5432                
dsn_uid = "postgres"         
dsn_pwd = "local-db-pw"

getCon = function() {
  return(dbConnect(Postgres(), 
                   dbname = dsn_database,
                   host = dsn_hostname, 
                   port = dsn_port,
                   user = dsn_uid, 
                   password = dsn_pwd))
}  