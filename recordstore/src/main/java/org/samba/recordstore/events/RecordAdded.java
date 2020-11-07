package org.samba.recordstore.events;

import lombok.*;
import org.factcast.factus.event.EventObject;
import org.factcast.factus.event.Specification;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Specification(ns = "recordstore")
public class RecordAdded implements EventObject {

    private UUID recordId;
    private String artist;
    private String title;
    private String label;  // TODO this could be a separate aggregate
    private String format; // TODO this should be an enum or separate aggregate (12", 7", 10", Picture...) Or maybe tags?!
    private LocalDate releaseDate;
    private ZonedDateTime addedToStore;

    @Override
    public Set<UUID> aggregateIds() {
        return Set.of(recordId);
    }
}
