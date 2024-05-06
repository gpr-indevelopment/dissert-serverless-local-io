calculateAndPrintSumSq = function(aov_data) {
  ss = anova(aov_data)["Sum Sq"]
  sst = sum(ss)
  print("Sum of squares")
  print(round(100*ss/sst, 2))
}

calculateAndPrintFCrit = function(aov_data, conf_level = 0.95) {
  df = anova(aov_data)["Df"]
  residuals_df = df$Df[length(df$Df)]
  f_crit = qf(conf_level, 1, residuals_df);
  print("F_crit calculation")
  print(data.frame("factor_df" = c(1), "residual_df" = c(residuals_df),"F_crit" = c(f_crit)))
}

bigFileWriteAnova = function() {
  print("1 GB file for WRITE operations")
  res = bigFileWriteAnovaQuery();
  
  res$io_size_bytes_fac = factor(res$io_size_bytes)
  
  res.aov = aov(latency_seconds ~ system_name * io_size_bytes_fac, data=res)
  print(summary(res.aov))
  
  SS = anova(res.aov)["Sum Sq"]
  SST = sum(SS)
  round(100*SS/sum(SS), 2)
  
  tukey = TukeyHSD(res.aov, conf.level = 0.95)
  print(tukey)
  plot(tukey,las=1,tcl = -.6)
  calculateAndPrintSumSq(res.aov)
  calculateAndPrintFCrit(res.aov)
}

smallFileWriteAnova = function() {
  print("10 KB file for WRITE operations")
  res = smallFileWriteAnovaQuery()
  
  res.aov = aov(latency_seconds ~ system_name * resource_tier, data=res)
  print(summary(res.aov))
  
  SS = anova(res.aov)["Sum Sq"]
  SST = sum(SS)
  round(100*SS/sum(SS), 2)
  
  tukey = TukeyHSD(res.aov, conf.level = 0.95)
  print(tukey)
  plot(tukey,las=1,tcl = -.6)
  calculateAndPrintSumSq(res.aov)
  calculateAndPrintFCrit(res.aov)
}

bigFileWriteAnova()
smallFileWriteAnova()

############################### READ ###############################
bigFileReadAnova = function() {
  print("1 GB file for READ operations")
  res = bigFileReadAnovaQuery()
  
  res$io_size_bytes_fac = factor(res$io_size_bytes)
  
  res.aov = aov(latency_seconds ~ system_name * io_size_bytes_fac, data=res)
  print(summary(res.aov))
  
  SS = anova(res.aov)["Sum Sq"]
  SST = sum(SS)
  round(100*SS/sum(SS), 2)
  
  tukey = TukeyHSD(res.aov, conf.level = 0.95)
  print(tukey)
  plot(tukey,las=1,tcl = -.6)
  calculateAndPrintSumSq(res.aov)
  calculateAndPrintFCrit(res.aov)
}

smallFileReadAnova = function() {
  print("10 KB file for READ operations")
  res = smallFileReadAnovaQuery()
  
  res.aov = aov(latency_seconds ~ system_name * resource_tier, data=res)
  print(summary(res.aov))
  
  SS = anova(res.aov)["Sum Sq"]
  SST = sum(SS)
  round(100*SS/sum(SS), 2)
  
  tukey = TukeyHSD(res.aov, conf.level = 0.95)
  print(tukey)
  plot(tukey,las=1,tcl = -.6)
  calculateAndPrintSumSq(res.aov)
  calculateAndPrintFCrit(res.aov)
}

bigFileReadAnova()
smallFileReadAnova()