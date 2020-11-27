package org.samba;


import lombok.*;
import org.factcast.core.spec.FactSpec;
import org.factcast.core.subscription.Subscription;
import org.factcast.factus.Factus;
import org.factcast.factus.Handler;
import org.factcast.factus.event.EventObject;
import org.factcast.factus.event.Specification;
import org.factcast.factus.lock.LockedOperationAbortedException;
import org.factcast.factus.projection.Aggregate;
import org.factcast.factus.projection.LocalManagedProjection;
import org.factcast.factus.projection.LocalSubscribedProjection;
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
import java.util.concurrent.CopyOnWriteArrayList;

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

    /////////////////////// Define Test Events //////////////////////
    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)  // required by jackson for deserialization
    @Specification(ns = "test")
    static class AddressAdded implements EventObject {

        private UUID addressId;  // this is the aggregate ID
        private String name;
        private String street;
        private String town;

        @Override
        public Set<UUID> aggregateIds() {
            return Set.of(addressId);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)  // required by jackson for deserialization
    @Specification(ns = "test")
    static class StreetChanged implements EventObject {

        private UUID addressId;   // this is the aggregate ID
        private String updatedStreet;

        @Override
        public Set<UUID> aggregateIds() {
            return Set.of(addressId);
        }
    }

    /////////////////////// Snapshot Projection //////////////////////

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

    @Test
    public void conditionalPublishUsingOptimisticLocking() {
        var addressAddedEvent = new AddressAdded(randomUUID(),
                "Ronny Schmidt",
                "Some Street 1",
                "End of Nowhere");

        // publish event first time succeeds
        factus.withLockOn(AddressBookProjection.class).attempt((freshAddressBookProjection, tx) -> {
            if (freshAddressBookProjection.getAddressBook().contains(addressAddedEvent)) {
                tx.abort("duplicate address detected");
            } else {
                tx.publish(addressAddedEvent);
            }
        });

        AddressBookProjection addressBookProjection = factus.fetch(AddressBookProjection.class);
        assertEquals(1, addressBookProjection.getAddressBook().size());

        // publishing 2nd time fails since address is already existing
        assertThrows(LockedOperationAbortedException.class, () -> {
            factus.withLockOn(AddressBookProjection.class).attempt((freshAddressBookProjection, tx) -> {
                if (freshAddressBookProjection.getAddressBook().contains(addressAddedEvent)) {
                    tx.abort("duplicate address detected"); // exception message
                } else {
                    tx.publish(addressAddedEvent);
                }
            });
        });
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


    /////////////////////// Aggregate Projection //////////////////////

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
        var secondEventAggregateId = secondEvent.getAddressId();
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

    /////////////////////// Locally Managed Projection //////////////////////

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
        private List<AddressAdded> addressBook = Collections.synchronizedList(new ArrayList<>());

        @Handler
        void apply(AddressAdded receivedAddressAddedEvent) {
            addressBook.add(receivedAddressAddedEvent);
        }
    }


    @Test
    public void filteringOfAggregateIdsViaPostProcess() {
        // arrange
        var address1 = new AddressAdded(
                randomUUID(),
                "Lou Reed",
                "Dark Street 1",
                "Dark town");

        var address2 = new AddressAdded(
                randomUUID(),
                "Bat Man",
                "Very Dark Street 42",
                "Gotham City");

        factus.publish(List.of(
                address1,
                address1,   // uups, duplicate. So we expect 2 events to be returned
                address2
        ));

        // act
        List<AddressAdded> result = getAddressAddedFor(address1.getAddressId());

        // assert
        assertEquals(2, result.size());
        assertEquals(address1, result.get(0));
        assertEquals(address1, result.get(1));
    }

    // create an instant local projection with just the events having addressId as aggregate ID
    private List<AddressAdded> getAddressAddedFor(UUID addressId) {
        var filteredManagedProjection = new FilteredAddressBookLocalManagedProjection(addressId);
        factus.update(filteredManagedProjection);
        return filteredManagedProjection.getAddressBook();
    }

    // only subscribe to "AddressAdded" events with the provided addressId (aggregate ID)
    @RequiredArgsConstructor
    static class FilteredAddressBookLocalManagedProjection extends LocalManagedProjection {
        private final UUID addressId;
        private List<AddressAdded> addressBook = Collections.synchronizedList(new ArrayList<>());

        // make FactSpec more specific
        @Override
        public @NonNull List<FactSpec> postprocess(@NonNull List<FactSpec> specsAsDiscovered) {
            // before: FactSpec(ns=test, type=AddressAdded, version=0, aggId=null, meta={}, jsFilterScript=null, filterScript=null)
            specsAsDiscovered.forEach(factSpec -> factSpec.aggId(addressId));
            // after: FactSpec(ns=test, type=AddressAdded, version=0, aggId=e08f3369-c134-4d1d-ad23-b1849360009c, meta={}, jsFilterScript=null, filterScript=null)
            return specsAsDiscovered;
        }

        @Handler
        void apply(AddressAdded receivedAddressAddedEvent) {
            addressBook.add(receivedAddressAddedEvent);
        }

        public List<AddressAdded> getAddressBook() {
            return Collections.unmodifiableList(addressBook);
        }
    }

    /////////////////////// Locally Subscribed Projection //////////////////////

    @Test
    public void automaticUpdatedWithLocalSubscribedProjection() throws InterruptedException {
        var autoUpdateAddressBook = new AddressBookLocalSubscribedProjection();
        factus.publish(
                new AddressAdded(
                        randomUUID(),
                        "Lou Reed",
                        "Dark Street 1",
                        "Dark town"));

        Subscription subscription = factus.subscribeAndBlock(autoUpdateAddressBook);
        // sync receive (block)
        subscription.awaitCatchup();

        assertEquals(1, autoUpdateAddressBook.getAddressBook().size());

        factus.publish(
                new AddressAdded(
                        randomUUID(),
                        "Iggy Pop",
                        "Skinny Road 21",
                        "LA"));
        // wait for aync projection update
        Thread.sleep(1000);

        assertEquals(2, autoUpdateAddressBook.getAddressBook().size());
    }

    @Data
    static class AddressBookLocalSubscribedProjection extends LocalSubscribedProjection {
        private List<AddressAdded> addressBook = new CopyOnWriteArrayList<>(); // ensure concurrent behaviour

        @Handler
        void apply(AddressAdded receivedAddressAddedEvent) {
            addressBook.add(receivedAddressAddedEvent);
        }
    }
}
