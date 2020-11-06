package org.samba;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

// note: had to update to latest maven compiler plugin
public class MapstructLearningTest {

    // POJOs had to be static
    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
    public static class SimpleSource {
        private String name;
        private String description;
    }

    @Data
    public static class SimpleDestination {
        private String name;
        private String description;
    }

    @Mapper
    public interface SimpleSourceDestinationMapper {
        SimpleDestination sourceToDestination(SimpleSource source);
        SimpleSource destinationToSource(SimpleDestination destination);
    }

    @Test
    public void simpleMap() {
        SimpleSourceDestinationMapper mapper =
                Mappers.getMapper(SimpleSourceDestinationMapper.class);

        SimpleSource source = new SimpleSource();
        source.setName("Ronny");
        source.setDescription("mate");
        SimpleDestination destination = mapper.sourceToDestination(source);

        assertEquals(source.getName(), destination.getName());
        assertEquals(source.getDescription(), destination.getDescription());
    }

}
