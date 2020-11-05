package org.samba;


import lombok.*;
import org.factcast.factus.Factus;
import org.factcast.factus.Handler;
import org.factcast.factus.event.EventObject;
import org.factcast.factus.event.Specification;
import org.factcast.factus.projection.SnapshotProjection;
import org.junit.jupiter.api.Test;
import org.samba.helper.AbstractFactCastIntegrationTest;
import org.samba.helper.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FactusLearningTest extends AbstractFactCastIntegrationTest {

    @Autowired
    Factus factus;

    @Test
    public void simplePublishAndFetch() {
        factus.publish(new Address(UUID.randomUUID(),
                "Ronny Schmidt",
                "Some Street 1",
                "End of Nowhere"));

        var addressBookProjection = factus.fetch(AddressBookProjection.class);

        assertEquals(1,addressBookProjection.getAddressBook().size());

        var receivedAddress = addressBookProjection.getAddressBook().get(0);
        assertEquals("Ronny Schmidt", receivedAddress.getName());
        assertEquals("Some Street 1", receivedAddress.getStreet());
        assertEquals("End of Nowhere", receivedAddress.getTown());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)  // required by jackson for deserialization
    @Specification(ns = "test")
    static class Address implements EventObject {
        UUID aggregateId;

        String name;
        String street;
        String town;

        @Override
        public Set<UUID> aggregateIds() {
            return Set.of(aggregateId);
        }
    }

    static class AddressBookProjection implements SnapshotProjection {

        @Getter
        private List<Address> addressBook = new ArrayList<>();

        @Handler
        void apply(Address address) {
            addressBook.add(address);
        }
    }
}
