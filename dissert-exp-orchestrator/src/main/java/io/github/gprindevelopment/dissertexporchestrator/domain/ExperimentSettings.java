package io.github.gprindevelopment.dissertexporchestrator.domain;

import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;

public record ExperimentSettings(DdFunctionService functionService, IoSizeTier ioSizeTier, FileSizeTier fileSizeTier) {
}
