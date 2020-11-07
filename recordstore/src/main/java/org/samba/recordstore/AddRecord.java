package org.samba.recordstore;

import eu.prismacapacity.spring.cqs.cmd.Command;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZonedDateTime;

// the command (or the query) can contain javax.validation constraints
// which are later checked implicitly at the handler
@Data
@Accessors(chain = true)  // mapstruct would not call the @AfterMapping method with @Builder
public class AddRecord implements Command {
    @NotNull
    private String artist;
    @NotNull
    private String title;
    @NotNull
    private String label;
    @NotNull
    private String format;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    private ZonedDateTime addedToStore;
}
