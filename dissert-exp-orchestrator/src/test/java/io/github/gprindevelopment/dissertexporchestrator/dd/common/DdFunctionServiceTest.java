package io.github.gprindevelopment.dissertexporchestrator.dd.common;

import io.github.gprindevelopment.dissertexporchestrator.dd.domain.*;
import io.github.gprindevelopment.dissertexporchestrator.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DdFunctionServiceTest {

    @InjectMocks
    @Spy
    private DdFunctionStubService ddFunctionStubService;
    @Mock
    private DdExpRecordRepository ddExpRecordRepository;
    @Mock
    private DdOperationErrorRepository ddOperationErrorRepository;

    @Mock
    private ClockService clockService;

    @Test
    public void Should_consider_business_hours_in_weekend_as_off_hours() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(TimeOfDay.OFF_HOUR, savedEntity.getTimeOfDay());
        assertEquals(WeekPeriod.WEEKEND, savedEntity.getWeekPeriod());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_time_of_day_as_off_hours() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699493949519L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(TimeOfDay.OFF_HOUR, savedEntity.getTimeOfDay());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_week_period_as_weekday() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699493949519L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(WeekPeriod.WEEKDAY, savedEntity.getWeekPeriod());
        assertEquals(DayOfWeek.WEDNESDAY, savedEntity.getDayOfWeek());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_time_of_day_as_business_hours() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699455600000L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(TimeOfDay.BUSINESS_HOUR, savedEntity.getTimeOfDay());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_week_period_as_weekend() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(WeekPeriod.WEEKEND, savedEntity.getWeekPeriod());
        assertEquals(DayOfWeek.SUNDAY, savedEntity.getDayOfWeek());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_set_current_resource_tier_as_one_when_null_during_collect_write_record() {
        ddFunctionStubService.currentResourceTier = null;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699455600000L));
        ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        verify(ddExpRecordRepository).save(any());
        assertEquals(ResourceTier.TIER_1, ddFunctionStubService.currentResourceTier);
    }

    @Test
    public void Should_set_current_resource_tier_as_one_when_null_during_collect_read_record() {
        ddFunctionStubService.currentResourceTier = null;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699455600000L));
        ddFunctionStubService.collectReadExpRecord(ioSizeTier);
        verify(ddExpRecordRepository).save(any());
        assertEquals(ResourceTier.TIER_1, ddFunctionStubService.currentResourceTier);
    }

    @Test
    public void Should_set_current_resource_tier_when_setting_resource_tier() {
        ddFunctionStubService.currentResourceTier = null;
        ResourceTier resourceTier = ResourceTier.TIER_1;

        assertNull(ddFunctionStubService.currentResourceTier);
        ddFunctionStubService.setFunctionResources(resourceTier);
        assertEquals(resourceTier, ddFunctionStubService.currentResourceTier);
    }

    @Test
    public void Should_successfully_populate_resource_tier_when_saving_write_record() {
        ddFunctionStubService.currentResourceTier = ResourceTier.TIER_1;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier);
        assertEquals(ResourceTier.TIER_1, savedEntity.getResourceTier());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_successfully_populate_resource_tier_when_saving_read_record() {
        ddFunctionStubService.currentResourceTier = ResourceTier.TIER_1;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        when(ddExpRecordRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(1699801200000L));
        DdExpRecordEntity savedEntity = ddFunctionStubService.collectReadExpRecord(ioSizeTier);
        assertEquals(ResourceTier.TIER_1, savedEntity.getResourceTier());
        verify(ddExpRecordRepository).save(any());
    }

    @Test
    public void Should_throw_exception_when_function_call_fails_on_write() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_1;

        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(System.currentTimeMillis()));
        doThrow(RuntimeException.class).when(ddFunctionStubService).callFunction(any());

        assertThrows(DdFunctionException.class, () -> ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier));
    }

    @Test
    public void Should_throw_exception_when_function_call_fails_on_read() {
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;

        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(System.currentTimeMillis()));
        doThrow(RuntimeException.class).when(ddFunctionStubService).callFunction(any());

        assertThrows(DdFunctionException.class, () -> ddFunctionStubService.collectReadExpRecord(ioSizeTier));
    }

    @Test
    public void Should_persist_error_record_when_read_operation_fails() {
        ddFunctionStubService.currentResourceTier = ResourceTier.TIER_2;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        ArgumentCaptor<DdOperationErrorEntity> entityCaptor = ArgumentCaptor.forClass(DdOperationErrorEntity.class);

        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(ddOperationErrorRepository.save(entityCaptor.capture())).thenReturn(null);
        doThrow(RuntimeException.class).when(ddFunctionStubService).callFunction(any());

        assertThrows(DdFunctionException.class, () -> ddFunctionStubService.collectReadExpRecord(ioSizeTier));
        DdOperationErrorEntity savedEntity = entityCaptor.getValue();
        assertNotNull(savedEntity.getCommand());
        assertNotNull(savedEntity.getIoSizeBytes());
        assertNotNull(savedEntity.getSystemName());
        assertNotNull(savedEntity.getOccurredAt());
        assertNotNull(savedEntity.getWeekPeriod());
        assertNotNull(savedEntity.getDayOfWeek());
        assertNotNull(savedEntity.getTimeOfDay());
        assertEquals(ResourceTier.TIER_2, savedEntity.getResourceTier());
        assertEquals(OperationType.READ, savedEntity.getOperationType());
        assertNull(savedEntity.getFileSizeBytes());
    }

    @Test
    public void Should_persist_error_record_when_write_operation_fails() {
        ddFunctionStubService.currentResourceTier = ResourceTier.TIER_2;
        IoSizeTier ioSizeTier = IoSizeTier.TIER_1;
        FileSizeTier fileSizeTier = FileSizeTier.TIER_2;
        ArgumentCaptor<DdOperationErrorEntity> entityCaptor = ArgumentCaptor.forClass(DdOperationErrorEntity.class);

        when(clockService.getCurrentTimestamp()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(ddOperationErrorRepository.save(entityCaptor.capture())).thenReturn(null);
        doThrow(RuntimeException.class).when(ddFunctionStubService).callFunction(any());

        assertThrows(DdFunctionException.class, () -> ddFunctionStubService.collectWriteExpRecord(ioSizeTier, fileSizeTier));
        DdOperationErrorEntity savedEntity = entityCaptor.getValue();
        assertNotNull(savedEntity.getCommand());
        assertNotNull(savedEntity.getFileSizeBytes());
        assertNotNull(savedEntity.getIoSizeBytes());
        assertNotNull(savedEntity.getSystemName());
        assertNotNull(savedEntity.getOccurredAt());
        assertNotNull(savedEntity.getWeekPeriod());
        assertNotNull(savedEntity.getDayOfWeek());
        assertNotNull(savedEntity.getTimeOfDay());
        assertEquals(ResourceTier.TIER_2, savedEntity.getResourceTier());
        assertEquals(OperationType.WRITE, savedEntity.getOperationType());
    }

    private static class DdFunctionStubService extends DdFunctionService {

        public DdFunctionStubService(
                DdExpRecordRepository ddExpRecordRepository,
                DdOperationErrorRepository ddOperationErrorRepository,
                ClockService clockService) {
            super(ddExpRecordRepository, ddOperationErrorRepository, clockService);
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

        @Override
        public void callSetFunctionResources(ResourceTier resourceTier) {}
    }

}