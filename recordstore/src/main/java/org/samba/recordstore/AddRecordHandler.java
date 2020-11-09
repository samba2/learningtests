package org.samba.recordstore;

import eu.prismacapacity.spring.cqs.cmd.CommandHandler;
import eu.prismacapacity.spring.cqs.cmd.CommandHandlingException;
import eu.prismacapacity.spring.cqs.cmd.CommandVerificationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.factcast.factus.Factus;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.samba.recordstore.events.RecordAdded;

import java.util.UUID;

import static java.time.ZonedDateTime.*;

@RequiredArgsConstructor
public class AddRecordHandler implements CommandHandler<AddRecord> {

    private final Factus factus;

    @Override
    public void verify(@NonNull AddRecord addRecord) throws CommandVerificationException {
        if (addRecord.getAddedToStore().isAfter(now())) {
            throw new IllegalArgumentException("Record can't ne added in the future");
        }
    }

    // spring-cqs ensures that "verify" is called before "handle". Done using Aspects
    @Override
    public void handle(@NonNull AddRecord addRecord) throws CommandHandlingException {
        RecordCommandToEventMapper mapper = Mappers.getMapper(RecordCommandToEventMapper.class);
        RecordAdded event = mapper.commandToEvent(addRecord);
        event.setRecordId(UUID.randomUUID());
        factus.publish(event);
    }

    // TODO register DI in spring: @Mapper(componentModel = "spring")
    @Mapper
    public interface RecordCommandToEventMapper {
        RecordAdded commandToEvent(AddRecord cmd);
    }

}
