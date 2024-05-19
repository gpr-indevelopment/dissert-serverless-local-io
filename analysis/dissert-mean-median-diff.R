library(readxl)
library(rstudioapi)

curDir = rstudioapi::getActiveDocumentContext()$path 
source(file.path(dirname(curDir), "dissert-queries.R"))

largeFilesMeanMedianDiff = function() {
  mean_median_diff <- read_excel(file.path(dirname(curDir), "mean-median-large-files-diff.xlsx"))
  
  mean_median_diff$Operation <- factor(mean_median_diff$Operation, levels = c("Write", "Read"))
  mean_median_diff$IO_Size <- factor(mean_median_diff$IO_Size, levels = c("512 B", "128 KB"))
  mean_median_diff$Type <- factor(mean_median_diff$Type, levels = c("Median", "Mean", "Difference"))
  
  ggplot(data=mean_median_diff, aes(x=System, y=Value, fill=Type)) +
    geom_bar(stat="identity", position="dodge") +
    geom_text(aes(label=sprintf("%.2f", Value)), vjust=-0.5, position=position_dodge(width=0.9), size=3.5) +
    facet_grid(Operation~IO_Size, scales="free") +
    labs(x = "Provider", y = "Latency (s)", fill = "Platform") + theme_bw() +
    theme(legend.position = "bottom", legend.box = "horizontal")
}

smallFilesMeanMedianDiff = function() {
  mean_median_diff_sf <- read_excel(file.path(dirname(curDir), "mean-median-small-files-diff.xlsx"))
  
  
  mean_median_diff_sf$Operation <- factor(mean_median_diff_sf$Operation, levels = c("Write", "Read"))
  mean_median_diff_sf$Type <- factor(mean_median_diff_sf$Type, levels = c("Median", "Mean", "Difference"))
  
  ggplot(data=mean_median_diff_sf, aes(x=System, y=Value, fill=Type)) +
    geom_bar(stat="identity", position="dodge") +
    geom_text(aes(label=sprintf("%.2f", Value)), vjust=-0.5, position=position_dodge(width=0.9), size=3.5) +
    facet_grid(Operation~Resource_tier, scales="free") +
    labs(x = "Provider", y = "Latency (ms)", fill = "Platform") + theme_bw() +
    theme(legend.position = "bottom", legend.box = "horizontal")
}