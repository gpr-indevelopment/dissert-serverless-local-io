# Useful scripts

## Export H2 table as CSV:

```sql
call CSVWRITE ( 'D:\Mestrado serverless/MyCSV.txt', 'SELECT * FROM DD_EXP_RECORD_ENTITY' )
```

## How to connect to H2 database through RStudio

I could not make it work by following the instructions [here](https://stackoverflow.com/questions/31275019/connecting-r-to-embedded-h2-database-on-rstudio).
