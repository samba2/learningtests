package org.samba;


import lombok.*;
import org.factcast.factus.Factus;
import org.factcast.factus.Handler;
import org.factcast.factus.event.EventObject;
import org.factcast.factus.event.Specification;
import org.factcast.factus.projection.Aggregate;
import org.factcast.factus.projection.SnapshotProjection;
import org.junit.jupiter.api.Test;
import org.samba.helper.AbstractFactCastIntegrationTest;
import org.samba.helper.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FactusLearningTest extends AbstractFactCastIntegrationTest {

    @Autowired
    Factus factus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)  // required by jackson for deserialization
    @Specification(ns = "test")
    static class AddressAdded implements EventObject {

        private UUID aggregateId;
        private String name;
        private String street;
        private String town;

        @Override
        public Set<UUID> aggregateIds() {
            return Set.of(aggregateId);
        }
    }

    @Test
    public void simplePublishAndFetchViaSnapshotProjection() {
        factus.publish(new AddressAdded(UUID.randomUUID(),
                "Ronny Schmidt",
                "Some Street 1",
                "End of Nowhere"));

        var addressBookProjection = factus.fetch(AddressBookProjection.class);

        assertEquals(1, addressBookProjection.getAddressBook().size());

        var receivedAddress = addressBookProjection.getAddressBook().get(0);
        assertEquals("Ronny Schmidt", receivedAddress.getName());
        assertEquals("Some Street 1", receivedAddress.getStreet());
        assertEquals("End of Nowhere", receivedAddress.getTown());
    }

    static class AddressBookProjection implements SnapshotProjection {
        @Getter
        private List<AddressAdded> addressBook = new ArrayList<>();

        @Handler
        void apply(AddressAdded receivedAddressAddedEvent) {
            addressBook.add(receivedAddressAddedEvent);
        }
    }

    @Test
    public void findSingleEventViaAggregateId() {
        // arrange
        var firstEvent = new AddressAdded(UUID.randomUUID(),
                "Petra Müller",
                "Other Street 2",
                "Beginning of Nowhere");

        var secondEvent = new AddressAdded(UUID.randomUUID(),
                "Max Musterman",
                "Different Street 3",
                "Some town");

        factus.publish(List.of(firstEvent, secondEvent));

        // act
        var secondEventAggregateId = secondEvent.getAggregateId();
        Optional<AddressAggregate> foundAddress = factus.find(AddressAggregate.class, secondEventAggregateId);

        // assert
        assertTrue(foundAddress.isPresent());
        assertEquals("Max Musterman", foundAddress.get().getName());
        assertEquals("Different Street 3", foundAddress.get().getStreet());
        assertEquals("Some town", foundAddress.get().getTown());

        assertEquals(1, foundAddress.get().getInvocationCounter());
    }

    @EqualsAndHashCode(callSuper = true) // TODO why?
    @Data
    static class AddressAggregate extends Aggregate {
        private String name;
        private String street;
        private String town;
        private int invocationCounter = 0;

        @Handler
        void apply(AddressAdded receivedEvent) {
            this.name = receivedEvent.getName();
            this.street = receivedEvent.getStreet();
            this.town = receivedEvent.getTown();
            this.invocationCounter++;
        }
    }
}
