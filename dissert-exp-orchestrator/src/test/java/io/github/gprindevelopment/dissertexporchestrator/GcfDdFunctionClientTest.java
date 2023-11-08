package io.github.gprindevelopment.dissertexporchestrator;

import io.github.gprindevelopment.dissertexporchestrator.common.CommandRequest;
import io.github.gprindevelopment.dissertexporchestrator.common.DdFunctionClient;
import io.github.gprindevelopment.dissertexporchestrator.gcf.GcfDdFunctionClient;
import io.github.gprindevelopment.dissertexporchestrator.gcf.GcfDdFunctionProps;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GcfDdFunctionClientTest {

    @InjectMocks
    private GcfDdFunctionClient gcfDdFunctionClient;
    @Mock
    private DdFunctionClient ddFunctionClient;
    @Mock
    private GcfDdFunctionProps gcfDdFunctionProps;
    private final EasyRandom generator = new EasyRandom();
    @Test
    public void Should_successfully_call_dd_function_client() {
        String expectedFunctionResponse = """
                    1024+0 records in
                    1024+0 records out
                    1073741824 bytes (1.1 GB, 1.0 GiB) copied, 1.09449 s, 981 MB/s""";
        String expectedUrl = generator.nextObject(String.class);
        CommandRequest commandRequest = new CommandRequest(generator.nextObject(String.class));

        when(gcfDdFunctionProps.url()).thenReturn(expectedUrl);
        when(ddFunctionClient.callFunction(commandRequest, expectedUrl)).thenReturn(expectedFunctionResponse);
        String result = gcfDdFunctionClient.callFunction(commandRequest);
        assertEquals(expectedFunctionResponse, result);
    }
}