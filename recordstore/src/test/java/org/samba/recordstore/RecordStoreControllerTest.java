package org.samba.recordstore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.samba.recordstore.AddRecord;
import org.samba.recordstore.AddRecordHandler;
import org.samba.recordstore.RecordStoreController;
import org.samba.recordstore.events.RecordAdded;
import org.samba.recordstore.gen.model.RecordWrite;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordStoreControllerTest {

    @Captor
    ArgumentCaptor<AddRecord> handlerCaptor;

    @Mock
    AddRecordHandler handler;

    @InjectMocks
    RecordStoreController uut;

    @Test
    public void handlerIsInvoked() {
        // arrange
        var receivedJson = new RecordWrite();
        receivedJson.setArtist("The Dancing Monkeys");
        receivedJson.setTitle("Dancing Time");
        receivedJson.setLabel("Ape Records");
        receivedJson.setFormat("12");
        receivedJson.setReleaseDate(LocalDate.of(2020, 1, 1));

        // act
        uut.addRecord(receivedJson);

        // assert
        verify(handler, times(1)).handle(isA(AddRecord.class));

        Mockito.verify(handler).handle(handlerCaptor.capture());
        var receivedCommand = handlerCaptor.getValue();

        assertNotNull(receivedCommand.getAddedToStore());
        assertEquals("Ape Records", receivedCommand.getLabel());
    }
}