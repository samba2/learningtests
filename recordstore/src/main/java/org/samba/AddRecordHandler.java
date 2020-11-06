package org.samba;

import eu.prismacapacity.spring.cqs.cmd.Command;
import eu.prismacapacity.spring.cqs.cmd.CommandHandler;
import eu.prismacapacity.spring.cqs.cmd.CommandHandlingException;
import eu.prismacapacity.spring.cqs.cmd.CommandVerificationException;
import lombok.NonNull;

public class AddRecordHandler implements CommandHandler<AddRecord> {
    @Override
    public void handle(@NonNull AddRecord addRecord) throws CommandHandlingException {

    }

    @Override
    public void verify(@NonNull AddRecord addRecord) throws CommandVerificationException {

    }
}
