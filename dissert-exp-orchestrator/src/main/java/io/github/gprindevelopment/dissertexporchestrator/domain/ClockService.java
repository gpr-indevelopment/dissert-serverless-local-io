package io.github.gprindevelopment.dissertexporchestrator.domain;

import org.springframework.stereotype.Service;

@Service
public class ClockService {

    public Long getSystemTimeMillis() {
        return System.currentTimeMillis();
    }
}
