## Package install
## install.packages("RPostgres")
require(RPostgres)

# Database properties
dsn_database = "postgres"   
dsn_hostname = "**"  
dsn_port = 5432                
dsn_uid = "postgres"         
dsn_pwd = "**"

con = dbConnect(Postgres(), 
                   dbname = dsn_database,
                   host = dsn_hostname, 
                   port = dsn_port,
                   user = dsn_uid, 
                   password = dsn_pwd)

res = dbGetQuery(con, "SELECT * from public.dd_experiment_entity 
	where operation_type = 'READ'
	and status = 'SUCCESS'
	and system_name = 'LAMBDA_DD'
	and resource_tier = 'TIER_2'")

res
