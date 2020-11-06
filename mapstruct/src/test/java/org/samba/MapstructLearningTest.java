package org.samba;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

// see also https://www.baeldung.com/mapstruct
// note: had to update to latest maven compiler plugin
public class MapstructLearningTest {

    // POJOs had to be static
    @Data
    @AllArgsConstructor
    static class SimpleSource {
        private String name;
        private String description;
    }

    @Data
    @AllArgsConstructor
    static class SimpleDestination {
        private String name;
        private String description;
    }

    // to register DI in spring: @Mapper(componentModel = "spring")
    @Mapper
    public interface SimpleSourceDestinationMapper {
        SimpleDestination sourceToDestination(SimpleSource source);
        SimpleSource destinationToSource(SimpleDestination destination);
    }

    @Test
    public void simpleMapSourceToDestination() {
        SimpleSourceDestinationMapper mapper =
                Mappers.getMapper(SimpleSourceDestinationMapper.class);

        SimpleSource source = new SimpleSource("Ronny", "mate");
        SimpleDestination destination = mapper.sourceToDestination(source);

        assertEquals(source.getName(), destination.getName());
        assertEquals(source.getDescription(), destination.getDescription());
    }

    @Test
    public void destinationToSource() {
        SimpleSourceDestinationMapper mapper =
                Mappers.getMapper(SimpleSourceDestinationMapper.class);

        SimpleDestination destination = new SimpleDestination("Ronny", "mate");
        SimpleSource source = mapper.destinationToSource(destination);

        assertEquals(destination.getName(), source.getName());
        assertEquals(destination.getDescription(), source.getDescription());
    }

    @Data
    @AllArgsConstructor
    static class VinylRecord {
        private Date releaseYear;
        private String artist;
        private String title;
    }

    @Data
    static class ShopProduct {
        private ZonedDateTime manufactured;
        private String mainTitle;
        private String description;
    }


    @Mapper
    public interface VinylRecordMapper {
        @Mappings({
                @Mapping(target = "mainTitle", source = "vinylRecord.artist"),
                @Mapping(target = "description", source = "vinylRecord.title"),
                @Mapping(target = "manufactured", source = "vinylRecord.releaseYear")
        })
        ShopProduct vinylRecordToShopProduct(VinylRecord vinylRecord);
        // other conversion direction missing due to laziness
    }

    @Test
    public void vinylRecordToShopProduct() {
        var mapper = Mappers.getMapper(VinylRecordMapper.class);

        VinylRecord vinylRecord = new VinylRecord(new Date(2017 - 1900, 8, 1), "Great Artist", "Best Of");
        ShopProduct shopProduct = mapper.vinylRecordToShopProduct(vinylRecord);

        assertEquals(vinylRecord.getArtist(), shopProduct.getMainTitle());
        assertEquals(vinylRecord.getTitle(), shopProduct.getDescription());
        assertEquals(2017, shopProduct.getManufactured().getYear());
        assertEquals(Month.SEPTEMBER, shopProduct.getManufactured().getMonth());
        assertEquals(1, shopProduct.getManufactured().getDayOfMonth());
    }

    @Mapper
    public interface VinylRecordMapper2 {
        // we only map the date automatically
        @Mappings({
                @Mapping(target = "manufactured", source = "vinylRecord.releaseYear")
        })
        ShopProduct vinylRecordToShopProduct(VinylRecord vinylRecord);

        @AfterMapping
        default void customTitleAndDescription(@MappingTarget ShopProduct shopProduct, VinylRecord vinylRecord) {
            var title = String.format("%s - %s", vinylRecord.getArtist(), vinylRecord.getTitle());
            shopProduct.setMainTitle(title);

            var description = String.format("Music by %s, album is called %s", vinylRecord.getArtist(), vinylRecord.getTitle());
            shopProduct.setDescription(description);
        }
    }

    @Test
    public void customMappings() {
        var mapper = Mappers.getMapper(VinylRecordMapper2.class);

        VinylRecord vinylRecord = new VinylRecord(new Date(2017 - 1900, 8, 1), "Great Artist", "Best Of");
        ShopProduct shopProduct = mapper.vinylRecordToShopProduct(vinylRecord);

        assertEquals("Great Artist - Best Of", shopProduct.getMainTitle());
        assertEquals("Music by Great Artist, album is called Best Of", shopProduct.getDescription());
    }
}
