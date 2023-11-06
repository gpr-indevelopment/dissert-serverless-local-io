package io.github.gprindevelopment.dissertexporchestrator;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GcfDdFunctionServiceTest {

    @InjectMocks
    private GcfDdFunctionService gcfDdFunctionService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private GcfDdFunctionProps gcfDdFunctionProps;
    private final EasyRandom generator = new EasyRandom();
    @Nested
    class Rest_template_tests {
        @Test
        public void Should_successfully_call_external_function_api() {
            String expectedFunctionResponse = """
                    1024+0 records in
                    1024+0 records out
                    1073741824 bytes (1.1 GB, 1.0 GiB) copied, 1.09449 s, 981 MB/s""";
            String expectedUrl = generator.nextObject(String.class);
            CommandRequest commandRequest = new CommandRequest(generator.nextObject(String.class));

            when(gcfDdFunctionProps.url()).thenReturn(expectedUrl);
            when(restTemplate.postForObject(expectedUrl, commandRequest, String.class))
                    .thenReturn(expectedFunctionResponse);
            String result = gcfDdFunctionService.callFunction(commandRequest);
            assertEquals(expectedFunctionResponse, result);
        }
    }

}