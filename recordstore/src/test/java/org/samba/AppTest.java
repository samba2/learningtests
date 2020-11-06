package org.samba;

import org.junit.jupiter.api.Test;
import org.samba.domain.RecordAdded;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;


public class AppTest {

    @Test
    public void publishFactToFactCast() {
        var recordAddedEvent= RecordAdded.builder()
                .recordId(UUID.randomUUID())
                .artist("The Sinning Monkeys")
                .title("Monkeys out and about")
                .label("Ape Records")
                .format("12")
                .releaseDate(LocalDate.of(2020, 10, 24))
                .addedToStore(ZonedDateTime.now())
                .build();


    }
}
