library(rstudioapi)

curDir = rstudioapi::getActiveDocumentContext()$path 
source(file.path(dirname(curDir), "dissert-queries.R"))

CV <- function(x) { sd(x) / mean(x) }

calculateAllCvs = function(data) {
  lambda_data = data[data$system_name == "LAMBDA_DD",]
  gcf_data = data[data$system_name == "GCF_DD", ]
  lambda_cvs = calculateGroupCvs(lambda_data);
  gcf_cvs = calculateGroupCvs(gcf_data);
  return(list("lambda" = lambda_cvs, "gcf" = gcf_cvs))
}

calculateGroupCvs = function(data) {
  resultDf = data.frame(
    resource_tier = character(),
    file_size_bytes = integer(),
    io_size_bytes = integer(),
    cv = numeric(),
    stringsAsFactors = FALSE
  );
  uniques = unique(data$group_id);
  for (group in 1:length(unique(data$group_id))) {
    group_data = data[data$group_id == uniques[group],]
    cv_result = CV(group_data$latency_seconds);
    newRow = data.frame(
      resource_tier= c(head(group_data, 1)$resource_tier),
      file_size_bytes= c(head(group_data, 1)$file_size_bytes),
      io_size_bytes= c(head(group_data, 1)$io_size_bytes),
      cv = cv_result
    );
    resultDf = rbind(resultDf,newRow);
  }
  return (resultDf);
}

writes = calculateAllCvs(cvWriteQuery())
reads = calculateAllCvs(cvReadQuery())