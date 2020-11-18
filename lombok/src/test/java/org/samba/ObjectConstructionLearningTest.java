package org.samba;

import lombok.*;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ObjectConstructionLearningTest {

    ///////////////////////////////////////////////////////////////////

    // with this config, fields can't be set to "final"
    @Getter
    @With
    @AllArgsConstructor
    @NoArgsConstructor
    static class Person {
        private String firstName;
        private String lastName;
    }

    @Test
    public void classBasedWither() {
        var p = new Person()
                .withFirstName("Ronny")
                .withLastName("Schmidt");

        assertEquals("Ronny", p.getFirstName());
        assertEquals("Schmidt", p.getLastName());
    }

    ///////////////////////////////////////////////////////////////////

    @Value
    @Builder
    static class Person2 {
        String firstName;
        String lastName;
    }

    @Test
    public void valueAndBuilder() {
        var p = Person2.builder()
                .firstName("Ronny")
                .lastName("Schmidt")
                .build();
    }

    ///////////////////////////////////////////////////////////////////

    @Data
    @Accessors(fluent = true)
    static class Person3 {
        private String firstName;
        private String lastName;
    }

    @Test
    public void usingAccessors() {
        var p = new Person3()
                .firstName("Ronny")
                .lastName("Schmidt");
    }
}
