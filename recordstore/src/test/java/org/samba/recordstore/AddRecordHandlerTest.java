package org.samba.recordstore;

import org.factcast.factus.Factus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.samba.recordstore.events.RecordAdded;
import org.samba.recordstore.AddRecord;
import org.samba.recordstore.AddRecordHandler;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddRecordHandlerTest {

    @Captor
    ArgumentCaptor<RecordAdded> factusCaptor;

    @Mock
    Factus mockedFactus;

    @InjectMocks
    AddRecordHandler uut;

    @Test
    void handlerPublishesEvent() {
        // arrange
        var cmd = new AddRecord()
                .setArtist("The Singing Monkeys")
                .setTitle("Monkeys out and about")
                .setLabel("Ape Records")
                .setFormat("12")
                .setReleaseDate(LocalDate.of(2020, 10, 24))
                .setAddedToStore(ZonedDateTime.now());

        // act
        uut.handle(cmd);

        // assert
        verify(mockedFactus, times(1)).publish(isA(RecordAdded.class));

        Mockito.verify(mockedFactus).publish(factusCaptor.capture());
        var publishedEvent = factusCaptor.getValue();

        assertNotNull(publishedEvent.getRecordId());
        assertEquals("The Singing Monkeys", publishedEvent.getArtist());
        assertEquals("Monkeys out and about", publishedEvent.getTitle());
    }

    @Test
    public void verifyRecordCantBeAddedInTheFuture() {
        // arrange
        var cmd = new AddRecord()
                .setArtist("The Singing Monkeys")
                .setTitle("Monkeys out and about")
                .setLabel("Ape Records")
                .setFormat("12")
                .setReleaseDate(LocalDate.of(2020, 10, 24))
                .setAddedToStore(ZonedDateTime.now().plusYears(10));  // wrong timestamp, in the future

        assertThrows(IllegalArgumentException.class, () -> uut.verify(cmd));
    }

}
