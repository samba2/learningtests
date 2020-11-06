package org.samba;

import eu.prismacapacity.spring.cqs.cmd.CommandHandler;
import eu.prismacapacity.spring.cqs.cmd.CommandHandlingException;
import eu.prismacapacity.spring.cqs.cmd.CommandVerificationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.factcast.factus.Factus;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.samba.domain.RecordAdded;

import java.util.UUID;

@RequiredArgsConstructor
public class AddRecordHandler implements CommandHandler<AddRecord> {

    private final Factus factus;

    // TODO add some logic later
    @Override
    public void verify(@NonNull AddRecord addRecord) throws CommandVerificationException {
    }

    // spring-cqs ensures that "verify" is called before "handle". Done using Aspects
    // TODO register DI in spring: @Mapper(componentModel = "spring")
    @Override
    public void handle(@NonNull AddRecord addRecord) throws CommandHandlingException {
        RecordCommandToEventMapper mapper = Mappers.getMapper(RecordCommandToEventMapper.class);
        RecordAdded event = mapper.commandToEvent(addRecord);
        event.setRecordId(UUID.randomUUID());
        factus.publish(event);
    }

    @Mapper
    public interface RecordCommandToEventMapper {
        RecordAdded commandToEvent(AddRecord cmd);
    }
}
