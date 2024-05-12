library(rstudioapi)
library(scales)
curDir = rstudioapi::getActiveDocumentContext()$path 
source(file.path(dirname(curDir), "dissert-queries.R"))

conf_level = 0.95;

maxFileConfInt = function(data) {
  lambda_data = data[data$system_name == "LAMBDA_DD", ]
  gcf_data = data[data$system_name == "GCF_DD", ]
  
  lambda_data_min_io = lambda_data[lambda_data$io_size_bytes == 512, ]
  lambda_data_max_io = lambda_data[lambda_data$io_size_bytes == 128000, ]
  
  gcf_data_min_io = gcf_data[gcf_data$io_size_bytes == 512, ]
  gcf_data_max_io = gcf_data[gcf_data$io_size_bytes == 128000, ]
  
  result_lambda_min_io = t.test(lambda_data_min_io$latency_seconds, conf_level = conf_level)$conf.int
  result_lambda_max_io = t.test(lambda_data_max_io$latency_seconds, conf_level = conf_level)$conf.int
  
  result_gcf_min_io = t.test(gcf_data_min_io$latency_seconds, conf_level = conf_level)$conf.int
  result_gcf_max_io = t.test(gcf_data_max_io$latency_seconds, conf_level = conf_level)$conf.int
  
  results = data.frame(
    provider = c("LAMBDA_DD", "LAMBDA_DD", "GCF_DD", "GCF_DD"),
    io_size = c(512, 128000, 512, 128000),
    lower = c(result_lambda_min_io[1], result_lambda_max_io[1], result_gcf_min_io[1], result_gcf_max_io[1]),
    upper = c(result_lambda_min_io[2], result_lambda_max_io[2], result_gcf_min_io[2], result_gcf_max_io[2])
  )
  
  print(results)
  
  ggplot(results, aes(x=provider, y = (lower + upper) / 2)) +
    geom_point() +
    facet_wrap(~io_size, labeller = labeller(io_size = c("512" = "512 B", "128000" = "128 KB")), scales="free_y") + 
    geom_errorbar(aes(ymin = lower, ymax = upper, colour = provider)) +
    theme_bw() + 
    theme(legend.position = "none") +
    labs(x = "Provider", y = "Latency (s)", colour = "Platform") +
    scale_x_discrete(labels=c("GCF_DD"="GCF", "LAMBDA_DD"="AWS Lambda"))
  
}

minFileConfInt = function(data) {
  # Convert to ms
  data$latency_seconds = data$latency_seconds*1000
  lambda_data = data[data$system_name == "LAMBDA_DD", ]
  gcf_data = data[data$system_name == "GCF_DD", ]
  
  lambda_data_min_res = lambda_data[lambda_data$resource_tier == "TIER_1", ]
  lambda_data_max_res = lambda_data[lambda_data$resource_tier == "TIER_5", ]
  
  gcf_data_min_res = gcf_data[gcf_data$resource_tier == "TIER_1", ]
  gcf_data_max_res = gcf_data[gcf_data$resource_tier == "TIER_5", ]
  
  result_lambda_min_res = t.test(lambda_data_min_res$latency_seconds, conf_level = conf_level)$conf.int
  result_lambda_max_res = t.test(lambda_data_max_res$latency_seconds, conf_level = conf_level)$conf.int
  
  result_gcf_min_res = t.test(gcf_data_min_res$latency_seconds, conf_level = conf_level)$conf.int
  result_gcf_max_res = t.test(gcf_data_max_res$latency_seconds, conf_level = conf_level)$conf.int
  
  results = data.frame(
    provider = c("LAMBDA_DD", "LAMBDA_DD", "GCF_DD", "GCF_DD"),
    resource_tier = c("TIER_1", "TIER_5", "TIER_1", "TIER_5"),
    lower = c(result_lambda_min_res[1], result_lambda_max_res[1], result_gcf_min_res[1], result_gcf_max_res[1]),
    upper = c(result_lambda_min_res[2], result_lambda_max_res[2], result_gcf_min_res[2], result_gcf_max_res[2])
  )
  
  print(results)
  
  ggplot(results, aes(x=provider, y = (lower + upper) / 2)) +
    geom_point() +
    facet_wrap(~resource_tier, labeller = labeller(resource_tier = c("TIER_1" = "Tier 1", "TIER_5" = "Tier 5")), scales="free_y") + 
    geom_errorbar(aes(ymin = lower, ymax = upper, colour = provider)) +
    theme_bw() + 
    theme(legend.position = "none") +
    labs(x = "Provider", y = "Latency (ms)", colour = "Platform") +
    scale_x_discrete(labels=c("GCF_DD"="GCF", "LAMBDA_DD"="AWS Lambda"))
}