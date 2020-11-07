package org.samba;

import org.factcast.factus.Factus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.samba.domain.events.RecordAdded;

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
        var cmd = AddRecord.builder()
                .artist("The Singing Monkeys")
                .title("Monkeys out and about")
                .label("Ape Records")
                .format("12")
                .releaseDate(LocalDate.of(2020, 10, 24))
                .addedToStore(ZonedDateTime.now())
                .build();

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
        var cmd = AddRecord.builder()
                .artist("The Singing Monkeys")
                .title("Monkeys out and about")
                .label("Ape Records")
                .format("12")
                .releaseDate(LocalDate.of(2020, 10, 24))
                .addedToStore(ZonedDateTime.now().plusYears(10))  // wrong timestamp, in the future
                .build();

        assertThrows(IllegalArgumentException.class, () -> uut.verify(cmd));
    }

}
