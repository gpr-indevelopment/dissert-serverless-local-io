library(rstudioapi)

curDir = rstudioapi::getActiveDocumentContext()$path 
source(file.path(dirname(curDir), "dissert-queries.R"))

CV <- function(x) { sd(x) / mean(x) }

calculateGroupCvs = function(data) {
  resultDf = data.frame(
    system_name = character(),
    resource_tier = character(),
    file_size_bytes = integer(),
    io_size_bytes = integer(),
    cv = numeric(),
    observations = integer(),
    stringsAsFactors = FALSE
  );
  uniques = unique(data$group_id);
  for (group in 1:length(unique(data$group_id))) {
    group_data = data[data$group_id == uniques[group],]
    cv_result = CV(group_data$latency_seconds);
    newRow = data.frame(
      system_name = c(head(group_data, 1)$system_name),
      resource_tier= c(head(group_data, 1)$resource_tier),
      file_size_bytes= c(head(group_data, 1)$file_size_bytes),
      io_size_bytes= c(head(group_data, 1)$io_size_bytes),
      cv = cv_result,
      observations = nrow(group_data)
    );
    resultDf = rbind(resultDf,newRow);
  }
  return (resultDf);
}

writes = calculateGroupCvs(cvWriteQuery())
reads = calculateGroupCvs(cvReadQuery())