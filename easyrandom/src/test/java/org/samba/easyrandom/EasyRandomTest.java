package org.samba.easyrandom;

import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class EasyRandomTest {

    @Test
    public void overwriteValueViaBuilder() {

        // initial fill
        var personBuilder = Person.builder()
                .withFirstName("Ronny")
                .withLastName("Schmidt")
                .withAddress(Address.builder()
                        .withStreetName("Foo street")
                        .withNumber("1a")
                        .withZipCode("42420")
                        .build());

        // overwrite value
        var person = personBuilder.withFirstName("Petra").build();

        assertThat(person)
                .extracting(
                        "firstName", "lastName", "address.streetName",
                        "address.number", "address.zipCode")
                .containsExactly(
                        "Petra", "Schmidt", "Foo street",
                        "1a", "42420");
    }

    @Test
    public void defaultValueIsNotOverwritten() {
        EasyRandomParameters parameters = new EasyRandomParameters()
                .randomize(String.class, () -> "foo");

        EasyRandom easyRandom = new EasyRandom(parameters);
        PersonAdded personAdded = easyRandom.nextObject(PersonAdded.class);

        assertThat(personAdded)
                .extracting("firstName", "lastName", "nickName")
                .containsExactly("foo", "foo", "the boss");
    }

    @Test
    public void overwriteUsingReflection() throws IllegalAccessException {
        var person = Person.builder()
                .withFirstName("Ronny")
                .withLastName("Schmidt")
                .withAddress(Address.builder()
                        .withStreetName("Foo street")
                        .withNumber("1a")
                        .withZipCode("42420")
                        .build())
                .build();

        update(person, "firstName", "Petra");
        update(person.getAddress(), "number", "2b");

        assertThat(person)
                .extracting(
                        "firstName", "lastName", "address.streetName",
                        "address.number", "address.zipCode")
                .containsExactly(
                        "Petra", "Schmidt", "Foo street",
                        "2b", "42420");
    }

    @Test
    public void initializeWithRandomDataAndOverwriteWhereNeeded() {
        var personAdded = aRandomPersonAdded();
        update(personAdded, "firstName", "Peter");
        update(personAdded, "nickName", "Pete");

        assertThat(personAdded)
                .extracting("firstName", "nickName")
                .containsExactly("Peter", "Pete");
    }

    @Test
    public void towardsFluentApi() {
        PersonAdded personAdded = aRandomPersonAdded();
        applyOverwrites(new Object[][]{
                {personAdded, "firstName", "Peter"},
                {personAdded, "nickName", "Pete" }});

        assertThat(personAdded)
                .extracting("firstName", "nickName")
                .containsExactly("Peter", "Pete");
    }

    @SneakyThrows
    private static void update(Object target, String fieldName, Object value) {
        FieldUtils.writeField(target, fieldName, value, true);
    }

    private static PersonAdded aRandomPersonAdded() {
        EasyRandom easyRandom = new EasyRandom();
        return easyRandom.nextObject(PersonAdded.class);
    }

    private static void applyOverwrites(Object[][] overwrites) {
        for (Object[] overwrite : overwrites) {
            // TODO add check: length is 3, 2nd element is string
            update(overwrite[0], (String) overwrite[1], overwrite[2]);
        }
    }

}