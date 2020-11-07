package org.samba;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordStoreControllerTest {

    @Mock
    AddRecordHandler handler;

    @InjectMocks
    RecordStoreController uut;

    @Test
    public void handlerIsInvoked() {
        uut.addRecord(
                "The Dancing Monkeys",
                "Dancing Time",
                "Ape Records",
                "12",
                LocalDate.of(2020, 1, 1));

        verify(handler, times(1)).handle(isA(AddRecord.class));
    }
}