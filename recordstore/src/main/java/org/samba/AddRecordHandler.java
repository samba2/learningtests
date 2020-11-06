package org.samba;

import eu.prismacapacity.spring.cqs.cmd.CommandHandler;
import eu.prismacapacity.spring.cqs.cmd.CommandHandlingException;
import eu.prismacapacity.spring.cqs.cmd.CommandVerificationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.factcast.factus.Factus;

@RequiredArgsConstructor
public class AddRecordHandler implements CommandHandler<AddRecord> {

    private final Factus factus;

    // TODO add some logic later
    @Override
    public void verify(@NonNull AddRecord addRecord) throws CommandVerificationException {
    }

    // spring-cqs ensures that "verify" is called before "handle". Done using Aspects
    @Override
    public void handle(@NonNull AddRecord addRecord) throws CommandHandlingException {


//        factus.publish();
    }
}
