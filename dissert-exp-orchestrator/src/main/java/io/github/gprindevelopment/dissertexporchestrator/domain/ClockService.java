package io.github.gprindevelopment.dissertexporchestrator.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ClockService {

    public Timestamp getCurrentTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now());
    }

    public void wait(TimeUnit timeUnit, long duration) {
        try {
            timeUnit.sleep(duration);
        } catch (InterruptedException ex) {
            log.error("Interrupted exception while waiting for " + timeUnit, ex);
        }
    }
}
