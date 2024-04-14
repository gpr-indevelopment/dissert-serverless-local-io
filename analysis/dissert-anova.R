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
}

bigFileReadAnova()
smallFileReadAnova()