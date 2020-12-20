package org.samba;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class JacksonAnnotationsLearningTest {

    // also read here: https://www.baeldung.com/jackson-annotations

    @Test
    public void hashtableToJson() throws JsonProcessingException {
        var person = new Person();

        person.addProperty("eyes", "green");
        person.addProperty("size", "tall");

        String result = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(person);

        System.out.println(result);

        // output:
        // {
        //  "size" : "tall",
        //  "eyes" : "green"
        //}
    }

    class Person {
        private Map<String, String> properties = new HashMap<>();

        public void addProperty(String key, String value) {
            properties.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, String> getProperties() {
            return this.properties;
        }
    }

    @Test
    public void pojoToSingleJsonValue() throws JsonProcessingException {
        Book book = new Book();
        book.setTitle("Good book");
        book.setPrice(1000L);

        String result = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(book);

        System.out.println(result);

        // returns:
        // "Good book"
        //
        // instead of
        //
        // {
        //  "title" : "Good book",
        //  "price" : 1000
        //}
    }

    @Data
    class Book {
        @JsonValue
        private String title;
        private Long price;
    }


    @Test
    // Jackson docs: Marker annotation that can be used to define constructors and factory methods as one to use for
    // instantiating new instances of the associated class.
    public void jsonCreatorWithDelegate() throws JsonProcessingException {
        val mapper = new ObjectMapper();
        val studentJson = "{\"customerNumber\": \"110841e3-e6fb-4191-8fd8-5674a5107c33\" }";

        mapper.readValue(studentJson, CustomerNumber.class);

        // works, can't reconstruct the issue

        // when leaving out the NoArgsConstructor this appears:
        // com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance of `org.samba.JacksonAnnotationsLearningTest$CustomerNumber` (although at least one Creator exists): cannot deserialize from Object value (no delegate- or property-based Creator)
        // at [Source: (String)"{"customerNumber": 42 }"; line: 1, column: 2]
    }


    @Value
    @NoArgsConstructor(force = true)
    static class CustomerNumber {
        //        @JsonValue
        UUID customerNumber;

        //        @JsonCreator
        public CustomerNumber(UUID customerNumber) {
            this.customerNumber = customerNumber;
        }

        //        @JsonCreator
        public CustomerNumber(String customerNumber) {
            // error handling left out intentionally
            this(UUID.fromString(customerNumber));
        }
    }

    @Test
    public void readJacksonizedValueObjectWithBuilder() throws JsonProcessingException {
        var json = "{ \"title\" : \"good title\", \"author\" :\"great author\" }";
        var result = new ObjectMapper().readValue(json, OtherBook.class);

        assertThat(result).isEqualTo(OtherBook.builder()
                .title("good title")
                .author("great author").build());
    }

    @Test
    public void createJsonViaJacksonizedValueObjectWithBuilder() throws JsonProcessingException {
        var book = OtherBook.builder()
                .title("good title")
                .author("great author")
                .price(12).build();

        var result = new ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(book);

        assertThat(result).contains("title");
        assertThat(result).contains("author");
        assertThat(result).doesNotContain("price");
    }


    @Value
    // since lombok 1.18.14 (Oct. 2020)
    @Jacksonized
    @Builder
    static class OtherBook {
        String title;
        String author;

        @JsonIgnore // is recognized
        int price;
    }


    @Test
    public void valueObjectWithBuilder() throws JsonProcessingException {
        var json = "{ \"title\" : \"good title\", \"author\" :\"great author\" }";
        var result = new ObjectMapper().readValue(json, OtherBook2.class);

        assertThat(result).isEqualTo(OtherBook2.builder()
                .title("good title")
                .author("great author").build());
    }

    @Value
    @Builder
    @JsonDeserialize(builder = OtherBook2.OtherBook2Builder.class)
    static class OtherBook2 {
        String title;
        String author;

        @JsonPOJOBuilder(withPrefix = "")
        public static class OtherBook2Builder {
        }
    }

}

