library(rstudioapi)

curDir = rstudioapi::getActiveDocumentContext()$path 
source(file.path(dirname(curDir), "dissert-queries.R"))

CV <- function(x) { sd(x) / mean(x) }

calculateGroupCvs = function(data) {
  resultDf = data.frame(
    system_name = character(),
    resource_tier = character(),
    file_size_bytes = numeric(),
    io_size_bytes = numeric(),
    mean = numeric(),
    cv = numeric(),
    observations = numeric(),
    is_normal = logical(),
    stringsAsFactors = FALSE
  );
  uniques = unique(data$group_id);
  for (group in 1:length(unique(data$group_id))) {
    group_data = data[data$group_id == uniques[group],]
    newRow = data.frame(
      system_name = c(head(group_data, 1)$system_name),
      resource_tier= c(head(group_data, 1)$resource_tier),
      file_size_bytes= c(head(group_data, 1)$file_size_bytes),
      io_size_bytes= c(head(group_data, 1)$io_size_bytes),
      cv =  CV(group_data$latency_seconds),
      mean = mean(group_data$latency_seconds),
      observations = nrow(group_data),
      is_normal = ifelse(shapiro.test(group_data$latency_seconds)$p.value > 0.05, TRUE, FALSE)
    );
    resultDf = rbind(resultDf,newRow);
  }
  return (resultDf);
}

generateWriteCvBar = function() {
  print("CV for writes")
  writes = calculateGroupCvs(cvWriteQuery())
  filtered_write = writes[
    writes$resource_tier %in% c("TIER_1", "TIER_5") & 
      as.integer(writes$io_size_bytes) %in% c(512, 128000) &
      as.integer(writes$file_size_bytes) %in% c(10000, 1024000000),]
  
  ggplot(data=filtered_write, aes(x=resource_tier, y=cv*100, fill=system_name)) +
    geom_bar(stat="identity", position="dodge") +
    geom_text(aes(label=sprintf("%.2f", cv*100)), vjust=-0.5, position=position_dodge(width=0.9), size=3.5) +
    facet_wrap(io_size_bytes~file_size_bytes, scales="free", labeller = labeller(
      io_size_bytes = c("512" = "I/O = 512 B", "128000" = "I/O = 128 KB"),
      file_size_bytes = c("10000" = "File = 10 KB", "1024000000" = "File = 1 GB"))) +
    labs(x = "Resource tier", y = "CV (%)", fill = "Platform") + theme_bw() +
    theme(legend.position = "bottom", legend.box = "horizontal") +
    scale_x_discrete(labels=c("TIER_1"="Tier 1", "TIER_5"="Tier 5")) + 
    scale_fill_discrete(labels=c("GCF_DD"="GCF", "LAMBDA_DD"="AWS Lambda"))
}

generateReadCvBar = function() {
  print("CV for reads")
  reads = calculateGroupCvs(cvReadQuery())
  filtered_read = reads[
    reads$resource_tier %in% c("TIER_1", "TIER_5") & 
      as.integer(reads$io_size_bytes) %in% c(512, 128000) &
      as.integer(reads$file_size_bytes) %in% c(10000, 1024000000),]
  
  ggplot(data=filtered_read, aes(x=resource_tier, y=cv*100, fill=system_name)) +
    geom_bar(stat="identity", position="dodge") +
    geom_text(aes(label=sprintf("%.2f", cv*100)), vjust=-0.5, position=position_dodge(width=0.9), size=3.5) +
    facet_wrap(io_size_bytes~file_size_bytes, scales="free", labeller = labeller(
      io_size_bytes = c("512" = "I/O = 512 B", "128000" = "I/O = 128 KB"),
      file_size_bytes = c("10000" = "File = 10 KB", "1024000000" = "File = 1 GB"))) +
    labs(x = "Resource tier", y = "CV (%)", fill = "Platform") + theme_bw() +
    theme(legend.position = "bottom", legend.box = "horizontal") +
    scale_x_discrete(labels=c("TIER_1"="Tier 1", "TIER_5"="Tier 5")) + 
    scale_fill_discrete(labels=c("GCF_DD"="GCF", "LAMBDA_DD"="AWS Lambda"))
}

