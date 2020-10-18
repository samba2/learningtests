package org.samba;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.factcast.factus.Factus;
import org.factcast.factus.event.EventObject;
import org.factcast.factus.event.Specification;
import org.factcast.spring.boot.autoconfigure.store.pgsql.PgFactStoreAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
@ContextConfiguration(classes = {Application.class})
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AppTest extends AbstractFactCastIntegrationTest {

    @Autowired
    Factus ec;

    @Test
    public void shouldAnswerWithTrue() {
        ec.publish(new HelloWorld(UUID.randomUUID(), "hello world"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Specification(ns = "test")
    class HelloWorld implements EventObject {
        UUID aggregateId;

        String dummy;

        @Override
        public Set<UUID> aggregateIds() {
            return Set.of(aggregateId);
        }
    }
}
