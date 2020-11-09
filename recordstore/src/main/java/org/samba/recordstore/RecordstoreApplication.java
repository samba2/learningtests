package org.samba.recordstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// TODO come up with local setup
@SpringBootApplication
public class RecordstoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecordstoreApplication.class, args);
    }

    // TODO fixes: ...fails with: org.springframework.web.util.NestedServletException: Request processing failed; nested exception is org.springframework.http.converter.HttpMessageConversionException: Type definition error: [simple type, class java.time.LocalDate]; nested exception is com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `java.time.LocalDate` (no Creators, like default constructor, exist): no String-argument constructor/factory method to deserialize from String value ('2020-01-30')
    // Is there a better way?
    @Bean
    public ObjectMapper serializingObjectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
