package org.samba;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.factcast.factus.Factus;
import org.factcast.factus.Handler;
import org.factcast.factus.event.EventObject;
import org.factcast.factus.event.Specification;
import org.factcast.factus.projection.Aggregate;
import org.factcast.factus.projection.LocalManagedProjection;
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

import static java.util.UUID.*;
import static org.junit.jupiter.api.Assertions.*;

// TODO use factcast-test package. Why is a DB needed, I thought we are on gprc!? DB for storing snapshots?

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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)  // required by jackson for deserialization
    @Specification(ns = "test")
    static class StreetChanged implements EventObject {

        private UUID aggregateId;
        private String updatedStreet;

        @Override
        public Set<UUID> aggregateIds() {
            return Set.of(aggregateId);
        }
    }

    // TODO snapshots are stored by default in FactCast.
    // How to configure an alternative SnapshotCache like Redis? Properties?
    // Clarify:
    //  - a "fetch" always causes a FactCast communication for
    //           - getting the most recent snapshot ?
    //           - collecting new events
    // - a snapshot is e.g. an old instance of AddressBookProjection
    @Test
    public void simplePublishAndFetchViaSnapshotProjection() {
        factus.publish(new AddressAdded(randomUUID(),
                "Ronny Schmidt",
                "Some Street 1",
                "End of Nowhere"));

        // the returned object is controlled (= stored/ refreshed) by Factus.
        // it is guaranteed that the returned projection includes the most recent event updates
        // the application code has no way to interfere here => "unmanaged"
        // "fetch" is local to this method. there is no global singleton to ask. For this ManagedProjects are used
        AddressBookProjection addressBookProjection = factus.fetch(AddressBookProjection.class);

        assertEquals(1, addressBookProjection.getAddressBook().size());

        var receivedAddress = addressBookProjection.getAddressBook().get(0);
        assertEquals("Ronny Schmidt", receivedAddress.getName());
        assertEquals("Some Street 1", receivedAddress.getStreet());
        assertEquals("End of Nowhere", receivedAddress.getTown());
    }

    // this object is automatically snapshoted (serialized/ deserialized + stored e.g. in FactCast)
    @Data
    static class AddressBookProjection implements SnapshotProjection {
        private List<AddressAdded> addressBook = new ArrayList<>();

        @Handler
        void apply(AddressAdded receivedAddressAddedEvent) {
            addressBook.add(receivedAddressAddedEvent);
        }
    }

    @Test
    public void findSingleEventViaAggregateId() {
        // arrange
        var firstEvent = new AddressAdded(randomUUID(),
                "Petra Müller",
                "Other Street 2",
                "Beginning of Nowhere");

        var secondEvent = new AddressAdded(randomUUID(),
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

    @Test
    public void findAggregateWithMultipleEvents() {
        var louReedAggregateId = randomUUID();

        // publish 3 events, the first 2 share the same aggregateId
        factus.publish(List.of(
                new AddressAdded(
                        louReedAggregateId,
                        "Lou Reed",
                        "Dark Street 1",
                        "Dark town"),
                new StreetChanged(
                        louReedAggregateId,
                        "Bright Street 42"),
                new AddressAdded(
                        randomUUID(),
                        "Iggy Pop",
                        "Skinny Road 21",
                        "LA")
        ));

        // act
        Optional<AddressAggregate2> foundAddress = factus.find(AddressAggregate2.class, louReedAggregateId);

        // assert
        assertTrue(foundAddress.isPresent());
        assertEquals("Lou Reed", foundAddress.get().getName());
        assertEquals("Bright Street 42", foundAddress.get().getStreet());
        assertEquals("Dark town", foundAddress.get().getTown());

        assertEquals(2, foundAddress.get().getInvocationCounter());
    }


    @Data
    static class AddressAggregate2 extends Aggregate {
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

        @Handler
        void apply(StreetChanged receivedEvent) {
            this.street = receivedEvent.getUpdatedStreet();
            this.invocationCounter++;
        }

    }

    @Test
    public void manualUpdatesWithLocallyManagedProjection() {
        var addressBook = new AddressBookLocalManagedProjection();
        // empty address book
        assertEquals(0, addressBook.getAddressBook().size());

        factus.publish(
                new AddressAdded(
                        randomUUID(),
                        "Lou Reed",
                        "Dark Street 1",
                        "Dark town"));

        // still empty address book, no update yet
        assertEquals(0, addressBook.getAddressBook().size());

        factus.update(addressBook);
        // "AddressAdded" event was picked up from FactCast and applied
        assertEquals(1, addressBook.getAddressBook().size());

        factus.publish(
                new AddressAdded(
                        randomUUID(),
                        "Iggy Pop",
                        "Skinny Road 21",
                        "LA"));

        // ! addressBook did not increase, "update" was missing
        assertEquals(1, addressBook.getAddressBook().size());

        factus.update(addressBook);
        // second AddressAdded event received and applied
        assertEquals(2, addressBook.getAddressBook().size());
    }

    // This projection is only in memory. No fancy snapshotting business.
    // It is updated (=asking FactCast for new events) when factus.update() is executed.
    // it can be used as a @Component to be application wide available.
    @Data
    static class AddressBookLocalManagedProjection extends LocalManagedProjection {
        private List<AddressAdded> addressBook = new ArrayList<>();

        @Handler
        void apply(AddressAdded receivedAddressAddedEvent) {
            addressBook.add(receivedAddressAddedEvent);
        }
    }

}
