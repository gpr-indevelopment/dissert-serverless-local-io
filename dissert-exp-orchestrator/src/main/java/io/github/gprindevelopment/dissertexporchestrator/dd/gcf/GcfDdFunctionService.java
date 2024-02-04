package io.github.gprindevelopment.dissertexporchestrator.dd.gcf;

import io.github.gprindevelopment.dissertexporchestrator.dd.common.DdFunctionService;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdExpRecordRepository;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.DdFunctionException;
import io.github.gprindevelopment.dissertexporchestrator.dd.domain.SystemName;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.ResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfNotFoundException;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfResourceTier;
import io.github.gprindevelopment.dissertexporchestrator.gcp.GcfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GcfDdFunctionService extends DdFunctionService {

    private final GcfDdFunctionClient gcfDdFunctionClient;
    private final GcfDdFunctionProps gcfDdFunctionProps;
    private final GcfService gcfService;

    public GcfDdFunctionService(GcfDdFunctionClient gcfDdFunctionClient,
                                GcfDdFunctionProps gcfDdFunctionProps,
                                GcfService gcfService,
                                DdExpRecordRepository ddExpRecordRepository,
                                ClockService clockService) {
        super(ddExpRecordRepository, clockService);
        this.gcfDdFunctionClient = gcfDdFunctionClient;
        this.gcfDdFunctionProps = gcfDdFunctionProps;
        this.gcfService = gcfService;
    }

    @Override
    protected String callFunction(CommandRequest commandRequest) {
        log.info("Collecting gcf-dd exp record for command: {}", commandRequest);
        return gcfDdFunctionClient.callFunction(commandRequest);
    }

    @Override
    protected String extractRawLatency(String rawResponse) {
        return rawResponse.split(",")[2].trim();
    }

    @Override
    protected String extractRawThroughput(String rawResponse) {
        return rawResponse.split(",")[3].trim();
    }

    @Override
    protected SystemName getSystemName() {
        return SystemName.GCF_DD;
    }

    @Override
    protected void callSetFunctionResources(ResourceTier resourceTier) {
        String functionName = gcfDdFunctionProps.name();
        GcfResourceTier gcfResourceTier = GcfResourceTier.from(resourceTier);
        try {
            gcfService.setFunctionResources(functionName, gcfResourceTier.getMemory(), gcfResourceTier.getCpu());
        } catch (GcfNotFoundException e) {
            throw new DdFunctionException("Unable to find GCF DD function with name: " + functionName, e);
        }
    }
}
