# Useful scripts

## Database dump

This script was used to dump a PostgreSQL database in the cloud to a file as SQL inserts.

```
pg_dump -h db_hostname -d db_database --table=public.final_experiment_data --data-only --column-inserts -U db_user -p 5432 -v -f ~/Downloads/insrt_stmts_file_name.sql
```
