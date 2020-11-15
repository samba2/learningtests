package org.samba.recordstore;

import lombok.RequiredArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.samba.recordstore.gen.model.RecordRead;
import org.samba.recordstore.gen.model.RecordWrite;
import org.samba.recordstore.gen.rest.RecordstoreApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;

@RestController
@RequiredArgsConstructor
public class RecordStoreController implements RecordstoreApi {

    private final AddRecordHandler addRecordHandler;

    @Override
    public ResponseEntity<RecordRead> addRecord(@Valid RecordWrite recordWrite) {
        var mapper = Mappers.getMapper(ReceivedJsonToCommandMapper.class);
        var cmd = mapper.addRecordJsonToCommand(recordWrite);

        addRecordHandler.handle(cmd);

        return ResponseEntity
                .ok()
                .body(mapper.addRecordJsonToResponse(recordWrite));
    }

    @Mapper
    public interface ReceivedJsonToCommandMapper {
        AddRecord addRecordJsonToCommand(RecordWrite receivedJson);
        RecordRead addRecordJsonToResponse(RecordWrite receivedJson);

        @AfterMapping
        default void fillField(@MappingTarget AddRecord addRecord, RecordWrite receivedJson) {
            addRecord.setAddedToStore(ZonedDateTime.now());
        }
    }
}
