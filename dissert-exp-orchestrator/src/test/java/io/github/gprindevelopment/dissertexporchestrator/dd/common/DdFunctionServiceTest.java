package io.github.gprindevelopment.dissertexporchestrator.dd.common;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.*;
import io.github.gprindevelopment.dissertexporchestrator.domain.ClockService;
import io.github.gprindevelopment.dissertexporchestrator.domain.TimeOfDay;
import io.github.gprindevelopment.dissertexporchestrator.domain.WeekPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DdFunctionServiceTest {

    @InjectMocks
    private DdFunctionStubService ddFunctionStubService;
    @Mock
    private DdExpRecordRepository ddExpRecordRepository;
    @Mock
    private ClockService clockService;

    @Test
    public void Should_consider_business_hours_in_weekend_as_off_hours() throws DdFunctionException {
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(TimeOfDay.OFF_HOUR, savedEntity.getTimeOfDay());
        assertEquals(WeekPeriod.WEEKEND, savedEntity.getWeekPeriod());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_time_of_day_as_off_hours() throws DdFunctionException {
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699493949519L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(TimeOfDay.OFF_HOUR, savedEntity.getTimeOfDay());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_week_period_as_weekday() throws DdFunctionException {
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699493949519L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(WeekPeriod.WEEKDAY, savedEntity.getWeekPeriod());
        assertEquals(DayOfWeek.WEDNESDAY, savedEntity.getDayOfWeek());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_time_of_day_as_business_hours() throws DdFunctionException {
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699455600000L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(TimeOfDay.BUSINESS_HOUR, savedEntity.getTimeOfDay());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_week_period_as_weekend() throws DdFunctionException {
        Long ioSizeBytes = 1_024_000L;
        Long fileSize = 1_000_000_000L;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeBytes, fileSize);
        assertEquals(WeekPeriod.WEEKEND, savedEntity.getWeekPeriod());
        assertEquals(DayOfWeek.SUNDAY, savedEntity.getDayOfWeek());
        verify(ddExpRecordRepository).save(any());
    }

    private static class DdFunctionStubService extends DdFunctionService {

        public DdFunctionStubService(DdExpRecordRepository ddExpRecordRepository, ClockService clockService) {
            super(ddExpRecordRepository, clockService);
        }

        @Override
        protected String callFunction(CommandRequest commandRequest) {
            return """
                976+0 records in
                976+0 records out
                999424000 bytes (999 MB, 953 MiB) copied, 0.830883 s, 1.2 GB/s""";
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
    }

}