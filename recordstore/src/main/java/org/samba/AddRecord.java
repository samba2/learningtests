package org.samba;

import eu.prismacapacity.spring.cqs.cmd.Command;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

// the command (or the query) can contain javax.validation constraints
// which are later checked implicitly at the handler
@Value
@Builder
public class AddRecord implements Command {
    @NotNull private String artist;
    @NotNull private String title;
    @NotNull private String label;
    @NotNull private String format;
    @NotNull private LocalDate releaseDate;
    @NotNull private ZonedDateTime addedToStore;
}
