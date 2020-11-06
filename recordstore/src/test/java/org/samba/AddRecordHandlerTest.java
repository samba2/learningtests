package org.samba;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

public class AddRecordHandlerTest {

    @Mock
    private AddRecordHandler uut;

    @Test
    void usesValidator() {
        var addRecord = mock(AddRecord.class);

        uut.validate(addRecord);
// TODO continue here
//        verify(validator).validate(mockedPlan);
    }
}
