library(rstudioapi)

curDir = rstudioapi::getActiveDocumentContext()$path 
source(file.path(dirname(curDir), "dissert-queries.R"))

CV <- function(x) { sd(x) / mean(x) }

calculateAllCvs = function(data) {
  lambda_data = data[data$system_name == "LAMBDA_DD",]
  gcf_data = data[data$system_name == "GCF_DD", ]
  lambda_cvs = calculateCv(lambda_data);
  gcf_cvs = calculateCv(gcf_data);
  return(list("lambda" = lambda_cvs, "gcf" = gcf_cvs))
}

calculateCv = function(data) {
  result = c();
  uniques = unique(data$group_id);
  for (group in 1:length(unique(data$group_id))) {
    group_data = data[data$group_id == uniques[group],]
    result = append(result, CV(group_data$latency_seconds))
  }
  return (result);
}

a = calculateAllCvs(minFileMinIoWriteQuery())