library(rstudioapi)
library(scales)
curDir = rstudioapi::getActiveDocumentContext()$path 
source(file.path(dirname(curDir), "dissert-queries.R"))

######################## DENSITY WRITE #############################################

densityMinFileAndMinIoWrite = function() {
  res = minFileMinIoWriteQuery();
  
  ggplot(data=res, aes(x=latency_seconds*1000, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    scale_x_log10() +
    ggtitle("Write latency for a 10 KB file and 512 B I/O size") + labs(x="Latency (ms)", y="Density", fill="Provider") + theme_bw()
}

densityMaxFileAndMinIoWrite = function() {
  res = maxFileMinIoWriteQuery();
  
  ggplot(data=res, aes(x=latency_seconds, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    ggtitle("Write latency for a 1 GB file and 512 B I/O size") + labs(x="Latency (s)", y="Density", fill="Provider") + theme_bw()
}

densityMaxFileAndMaxIoWrite = function() {
  res = maxFileMaxIoWriteQuery();
  
  ggplot(data=res, aes(x=latency_seconds, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    ggtitle("Write latency for a 1 GB file and 128 KB I/O size") + labs(x="Latency (s)", y="Density", fill="Provider") + theme_bw()
}

densityMinFileAndMinIoWrite()
densityMaxFileAndMinIoWrite()
densityMaxFileAndMaxIoWrite()

######################## DENSITY READ #############################################

densityMinFileAndMinIoRead = function() {
  res = minFileMinIoReadQuery()
  
  ggplot(data=res, aes(x=latency_seconds*1000, group=system_name, fill=system_name)) +
    facet_wrap(~resource_tier) +
    geom_density(alpha=.4) +
    scale_x_log10() +
    ggtitle("Read latency for a 10 KB file and 512 B I/O size") + labs(x="Latency (ms)", y="Density", fill="Provider") + theme_bw()
}

densityMaxFileAndMinIoRead = function() {
  res = maxFileMinIoReadQuery();
  
  ggplot(data=res, aes(x=latency_seconds, group=system_name, fill=system_name)) +
    geom_density(alpha=.4) +
    scale_x_log10(limits = c(0.1, 1000)) +
    ggtitle("Read latency for a 1 GB file and 512 B I/O size") + labs(x="Latency (s)", y="Density", fill="Provider") + theme_bw()
}

densityMaxFileAndMaxIoRead = function() {
  res = maxFileMaxIoReadQuery()
  
  ggplot(data=res, aes(x=latency_seconds, group=system_name, fill=system_name)) +
    geom_density(alpha=.4) +
    scale_x_log10(limits = c(0.1, 1000)) +
    ggtitle("Read latency for a 1 GB file and 128 KB I/O size") + labs(x="Read latency (s)", y="Density", fill="Provider") + theme_bw()
}

densityMinFileAndMinIoRead()
densityMaxFileAndMinIoRead()
densityMaxFileAndMaxIoRead()

## Box plots

######################## BOXPLOT WRITE #############################################

boxplotMinFileAndMinIoWrite = function() {
  res = minFileMinIoWriteQuery();
  
  ggplot(data=res, aes(y=latency_seconds*1000, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    scale_y_log10() +
    #coord_cartesian(ylim = c(0, 25)) + 
    #ylim(0,1) + limits it between 0 and 1, and hides some of the values
    ggtitle("Write latency for a 10 KB file and 512 B I/O size") + labs(x="Resource tier", y="Latency (ms)", fill="Provider") + theme_bw()
}

boxplotMaxFileAndMinIoWrite = function() {
  res = maxFileMinIoWriteQuery();
  
  ggplot(data=res, aes(y=latency_seconds, fill=system_name, x=resource_tier)) +
    scale_y_log10(breaks = trans_breaks("log10", function(x) 10^x),
                  labels = trans_format("log10", math_format(10^.x))) +  
    geom_boxplot(outlier.shape = NA) + 
    ggtitle("Write latency for a 1 GB file and 512 B I/O size") + labs(x="Resource tier", y="Latency (s)", fill="Provider") + theme_bw()
  
}

boxplotMaxFileAndMaxIoWrite = function() {
  res = maxFileMaxIoWriteQuery();
  
  ggplot(data=res, aes(y=latency_seconds, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    ggtitle("Write latency for a 1 GB file and 128 KB I/O size") + labs(x="Resource tier", y="Latency (s)", fill="Provider") + theme_bw()
}

boxplotMinFileAndMinIoWrite()
boxplotMaxFileAndMinIoWrite()
boxplotMaxFileAndMaxIoWrite()

######################## BOXPLOT READ #############################################

boxplotMinFileAndMinIoRead = function() {
  res = minFileMinIoReadQuery();
  
  ggplot(data=res, aes(y=latency_seconds*1000, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    scale_y_log10() +
    #coord_cartesian(ylim = c(0, 0.3)) + 
    ggtitle("Read latency for a 10 KB file and 512 B I/O size") + labs(x="Resource tier", y="Latency (ms)", fill="Provider") + theme_bw()
}

boxplotMaxFileAndMinIoRead = function() {
  res = maxFileMinIoReadQuery();
  
  ggplot(data=res, aes(y=latency_seconds, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    #coord_cartesian(ylim = c(0, 6)) +
    ggtitle("Read latency for a 1 GB file and 512 B I/O size") + labs(x="Resource tier", y="Latency (s)", fill="Provider") + theme_bw()
}

boxplotMaxFileAndMaxIoRead = function() {
  res = maxFileMaxIoReadQuery();
  
  ggplot(data=res, aes(y=latency_seconds, fill=system_name, x=resource_tier)) +
    geom_boxplot(outlier.shape = NA) + 
    #coord_cartesian(ylim = c(0, 0.15)) +
    ggtitle("Read latency for a 1 GB file and 128 KB I/O size") + labs(x="Resource tier", y="Latency (s)", fill="Provider") + theme_bw()
}

boxplotMinFileAndMinIoRead()
boxplotMaxFileAndMinIoRead()
boxplotMaxFileAndMaxIoRead()

######################## ECDF WRITE #############################################

ecdfMinFileAndMinIoWrite = function() {
  res = minFileMinIoWriteQuery();

  ggplot(data=res, aes(x=latency_seconds*1000, colour = resource_tier)) +
    stat_ecdf() + 
    facet_wrap(~system_name) + 
    ggtitle("Write latency for a 10 KB file and 512 B I/O size") + labs(x="Latency (ms)") + theme_bw()
}

ecdfMinFileMinIoMinMaxResourceWrite = function() {
  res = minFileMinIoMinMaxResourceWriteQuery();
  print("ECDF of write latency for a 10 KB file and 512 B I/O size")
  medians <- res %>% group_by(system_name, resource_tier) %>% summarize(median_latency = quantile(latency_seconds, 0.5) *1000)
  print(medians)
  ggplot(data=res, aes(x=latency_seconds*1000, colour = system_name)) +
    stat_ecdf() + 
    facet_grid(~resource_tier, labeller = labeller(resource_tier = c("TIER_1" = "Tier 1", "TIER_5" = "Tier 5"))) + 
    scale_color_discrete(labels=c("GCF_DD"="GCF", "LAMBDA_DD"="AWS Lambda")) +
    #ggtitle("Write latency for a 10 KB file and 512 B I/O size") + 
    labs(x="Latency (ms)",y="Percentile", colour = "Platform") + theme_bw() + 
    theme(legend.position = "bottom", legend.box = "horizontal")
}

ecdfMaxFileAndMinIoWrite = function() {
  res = maxFileMinIoWriteQuery();
  
  ggplot(data=res, aes(x=latency_seconds, colour = resource_tier)) +
    stat_ecdf() + 
    facet_wrap(~system_name) + 
    ggtitle("Write latency for a 1 GB file and 512 B I/O size") + labs(x="Latency (s)") + theme_bw()
}

ecdfMaxFileAndMaxIoWrite = function() {
  res = maxFileMaxIoWriteQuery();
  
  ggplot(data=res, aes(x=latency_seconds, colour = resource_tier)) +
    stat_ecdf() + 
    facet_wrap(~system_name) + 
    ggtitle("Write latency for a 1 GB file and 128 KB I/O size") + labs(x="Latency (s)") + theme_bw()
}

ecdfMaxFileAndMinMaxIoWrite = function() {
  res = maxFileMinMaxIoWriteQuery();
  print("ECDF of write latency for a 1 GB file")
  
  medians <- res %>% group_by(system_name, io_size_bytes) %>% summarize(median_latency = quantile(latency_seconds, 0.5))
  print(medians)
  
  ggplot(data=res, aes(x=latency_seconds, colour = system_name)) +
    stat_ecdf() + 
    facet_grid(~io_size_bytes, labeller = labeller(io_size_bytes = c("512" = "512 B", "128000" = "128 KB"))) + 
    scale_color_discrete(labels=c("GCF_DD"="GCF", "LAMBDA_DD"="AWS Lambda")) +
    #ggtitle("ECDF of write latency for a 1 GB file") + 
    labs(x="Latency (s)",y="Percentile", colour = "Platform") + theme_bw() + 
    theme(legend.position = "bottom", legend.box = "horizontal")
}

ecdfMinFileAndMinIoWrite()
ecdfMaxFileAndMinIoWrite()
ecdfMaxFileAndMaxIoWrite()
ecdfMaxFileAndMinMaxIoWrite()

######################## ECDF READ #############################################

ecdfMinFileAndMinIoRead = function() {
  res = minFileMinIoReadQuery();
  
  ggplot(data=res, aes(x=latency_seconds * 1000, colour = resource_tier)) +
    stat_ecdf() + 
    facet_wrap(~system_name) + 
    ggtitle("Read latency for a 10 KB file and 512 B I/O size") + labs(x="Latency (ms)") + theme_bw()
}

ecdfMinFileMinIoMinMaxResourceRead = function() {
  res = minFileMinIoMinMaxResourceReadQuery();
  print("ECDF of read latency for a 10 KB file and 512 B I/O size")
  medians <- res %>% group_by(system_name, resource_tier) %>% summarize(median_latency = quantile(latency_seconds, 0.5)*1000)
  print(medians)
  ggplot(data=res, aes(x=latency_seconds*1000, colour = system_name)) +
    stat_ecdf() + 
    facet_grid(~resource_tier, labeller = labeller(resource_tier = c("TIER_1" = "Tier 1", "TIER_5" = "Tier 5"))) + 
    scale_color_discrete(labels=c("GCF_DD"="GCF", "LAMBDA_DD"="AWS Lambda")) +
    #ggtitle("Read latency for a 10 KB file and 512 B I/O size") + 
    labs(x="Latency (ms)",y="Percentile", colour = "Platform") + theme_bw() + 
    theme(legend.position = "bottom", legend.box = "horizontal")
}

ecdfMaxFileAndMinIoRead = function() {
  res = maxFileMinIoReadQuery();
  
  ggplot(data=res, aes(x=latency_seconds, colour = resource_tier)) +
    stat_ecdf() + 
    facet_wrap(~system_name) + 
    ggtitle("Read latency for a 1 GB file and 512 B I/O size") + labs(x="Latency (s)") + theme_bw()
}

ecdfMaxFileAndMaxIoRead = function() {
  res = maxFileMaxIoReadQuery();
  
  ggplot(data=res, aes(x=latency_seconds, colour = resource_tier)) +
    stat_ecdf() + 
    facet_wrap(~system_name) + 
    ggtitle("Read latency for a 1 GB file and 128 KB I/O size") + labs(x="Latency (s)") + theme_bw()
}

ecdfMaxFileAndMinMaxIoRead = function() {
  res = maxFileMinMaxIoReadQuery();
  print("ECDF of read latency for a 1 GB file")
  medians <- res %>% group_by(system_name, io_size_bytes) %>% summarize(median_latency = quantile(latency_seconds, 0.5))
  print(medians)
  ggplot(data=res, aes(x=latency_seconds, colour = system_name)) +
    stat_ecdf() + 
    facet_grid(~io_size_bytes, labeller = labeller(io_size_bytes = c("512" = "512 B", "128000" = "128 KB"))) + 
    scale_color_discrete(labels=c("GCF_DD"="GCF", "LAMBDA_DD"="AWS Lambda")) +
    #ggtitle("ECDF of read latency for a 1 GB file") + 
    labs(x="Latency (s)", y="Percentile", colour = "Platform") + theme_bw() +
    theme(legend.position = "bottom", legend.box = "horizontal")
}

ecdfMinFileAndMinIoRead()
ecdfMaxFileAndMinIoRead()
ecdfMaxFileAndMaxIoRead()
ecdfMaxFileAndMinMaxIoRead()
