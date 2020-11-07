package org.samba.recordstore;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
public class RecordStoreController {

    private final AddRecordHandler addRecordHandler;

    public ResponseEntity<Void> addRecord(String artist, String title, String label, String format, LocalDate releaseDate) {
        var cmd = AddRecord.builder()
                .artist(artist)
                .title(title)
                .label(label)
                .format(format)
                .releaseDate(releaseDate)
                .addedToStore(ZonedDateTime.now())
                .build();

        addRecordHandler.handle(cmd);

        return ResponseEntity.ok().build();
    }
}
