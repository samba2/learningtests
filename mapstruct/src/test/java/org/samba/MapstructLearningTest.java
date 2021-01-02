package org.samba;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

// see also https://www.baeldung.com/mapstruct
// note: had to update to latest maven compiler plugin
// mapstruct generates mapper at "compile" lifecycle
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

        assertThat(destination).usingRecursiveComparison().isEqualTo(source);
    }

    @Test
    public void destinationToSource() {
        SimpleSourceDestinationMapper mapper =
                Mappers.getMapper(SimpleSourceDestinationMapper.class);

        SimpleDestination destination = new SimpleDestination("Ronny", "mate");
        SimpleSource source = mapper.destinationToSource(destination);

        assertThat(destination).usingRecursiveComparison().isEqualTo(source);
    }

    ////////////////////////////////////////////////////////////////////////

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
                @Mapping(target = "mainTitle", source = "artist"),
                @Mapping(target = "description", source = "title"),
                @Mapping(target = "manufactured", source = "releaseYear")
        })
        ShopProduct vinylRecordToShopProduct(VinylRecord vinylRecord);
        // other conversion direction missing due to laziness
    }

    @Test
    public void vinylRecordToShopProduct() {
        var mapper = Mappers.getMapper(VinylRecordMapper.class);

        VinylRecord vinylRecord = new VinylRecord(new Date(2017 - 1900, 8, 1), "Great Artist", "Best Of");
        ShopProduct shopProduct = mapper.vinylRecordToShopProduct(vinylRecord);

        assertThat(shopProduct).extracting(
                "mainTitle",
                "description",
                "manufactured.year",
                "manufactured.month",
                "manufactured.dayOfMonth")
                .containsExactly(
                        vinylRecord.getArtist(),
                        vinylRecord.getTitle(),
                        2017,
                        Month.SEPTEMBER,
                        1);
    }

    ////////////////////////////////////////////////////////////////////////

    @Mapper
    public interface VinylRecordMapper2 {
        // we only map the date automatically
        @Mappings({
                @Mapping(target = "manufactured", source = "releaseYear")
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

        assertThat(shopProduct)
                .extracting(
                        ShopProduct::getMainTitle,
                        ShopProduct::getDescription)
                .containsExactly(
                        "Great Artist - Best Of",
                        "Music by Great Artist, album is called Best Of");
    }

    ////////////////////////////////////////////////////////////////////////

    @Mapper
    public interface VinylRecordMapper3 {
        @Mappings({
                @Mapping(target = "manufactured", source = "releaseYear"),
                @Mapping(target = "mainTitle", expression = "java(MapstructLearningTest.mapMainTitle(vinylRecord))"),
                @Mapping(target = "description", expression = "java(MapstructLearningTest.mapDescription(vinylRecord))")
        })
        ShopProduct vinylRecordToShopProduct(VinylRecord vinylRecord);
    }

    static String mapMainTitle(VinylRecord vinylRecord) {
        return String.format("%s - %s", vinylRecord.getArtist(), vinylRecord.getTitle());
    }

    static String mapDescription(VinylRecord vinylRecord) {
        return String.format("Music by %s, album is called %s", vinylRecord.getArtist(), vinylRecord.getTitle());
    }

    @Test
    public void customMappingViaExpression() {
        var mapper = Mappers.getMapper(VinylRecordMapper3.class);

        VinylRecord vinylRecord = new VinylRecord(new Date(2017 - 1900, 8, 1), "Great Artist", "Best Of");
        ShopProduct shopProduct = mapper.vinylRecordToShopProduct(vinylRecord);

        assertThat(shopProduct)
                .extracting(
                        ShopProduct::getMainTitle,
                        ShopProduct::getDescription)
                .containsExactly(
                        "Great Artist - Best Of",
                        "Music by Great Artist, album is called Best Of"
                );
    }

    //////////////////////////////////////////////////////////////////


    static enum MyColor {
        BLACK,
        WHITE,
        GREEN
    }

    @Data
    @AllArgsConstructor
    static class MyProduct {
        int id;
        MyColor color;
    }


    static enum TheirColor {
        BLACK,
        WHITE,
        GREEN
    }

    @Data
    static class TheirProduct {
        Integer id;
        TheirColor color;
    }

    @Mapper
    interface ColorMapper {
        ColorMapper INSTANCE = Mappers.getMapper(ColorMapper.class);

        TheirProduct mapProduct(MyProduct myProduct);

        TheirColor mapColor(MyColor color);
    }

    @Test
    public void mapObjectWithEnum() {
        var myProduct = new MyProduct(1, MyColor.GREEN);
        var theirProduct = ColorMapper.INSTANCE.mapProduct(myProduct);

        assertThat(theirProduct)
                .usingRecursiveComparison()
                .isEqualTo(myProduct);
    }


    ////////////////////////////////////////////////////////////////////


    @Value
    static class FirstName {
        String firstName;  // the inner field name have to match the field name of the flat target
    }

    @Value
    static class LastName {
        String lastName;
    }

    @Value
    static class Person {
        FirstName firstName;
        LastName lastName;
    }

    @Value
    static class FlatPerson {
        String firstName;
        String lastName;
    }

    @Mapper
    interface PersonMapper {
        PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);

        @Mapping(source = "firstName", target = ".")
        @Mapping(source = "lastName", target = ".")
        FlatPerson mapPerson(Person person);
    }

    @Test
    public void mappingNestedSources() {
        var person = new Person(new FirstName("Ronny"), new LastName("Schmidt"));

        var flatPerson = PersonMapper.INSTANCE.mapPerson(person);

        assertThat(flatPerson)
                .extracting("firstName", "lastName")
                .containsExactly("Ronny", "Schmidt");

    }


    ////////////////// Mapping Composition //////////////////////////////////////////////////

    @Builder
    @Value
    static class OrderAdded {
        UUID orderId;
        String info;
        String details;
        ZonedDateTime addedTimestamp;
    }

    @Builder
    @Value
    static class OrderChanged {
        UUID orderId;
        String info;
        String details;
        ZonedDateTime changedTimestamp;
    }

    @Value
    static class OrderAggregate {
        UUID id;
        String name;
        String description;
        ZonedDateTime lastModified;
    }

    @Retention(RetentionPolicy.CLASS)
    @Mapping(source = "orderId", target = "id")
    @Mapping(source = "info", target = "name")
    @Mapping(source = "details", target = "description")
    public @interface ToOrderAggregate {
    }

    @Mapper
    interface OrderEventsMapper {
        OrderEventsMapper INSTANCE = Mappers.getMapper(OrderEventsMapper.class);

        @ToOrderAggregate
        @Mapping(source = "addedTimestamp", target = "lastModified")
        OrderAggregate mapOrderAdded(OrderAdded orderAdded);

        @ToOrderAggregate
        @Mapping(source = "changedTimestamp", target = "lastModified")
        OrderAggregate mapOrderChanged(OrderChanged orderChanged);
    }

    @Test
    public void orderAddedIsMapped() {
        var orderId = UUID.randomUUID();
        var addedTimestamp = ZonedDateTime.now();

        var orderAdded = OrderAdded.builder()
                .orderId(orderId)
                .info("SAULT - UNTITLED")
                .details("Great 2nd album")
                .addedTimestamp(addedTimestamp).build();

        var orderAggregate = OrderEventsMapper.INSTANCE.mapOrderAdded(orderAdded);

        assertThat(orderAggregate)
                .extracting("id", "name", "description", "lastModified")
                .containsExactly(orderId, "SAULT - UNTITLED", "Great 2nd album", addedTimestamp);
    }

    @Test
    public void orderChangedIsMapped() {
        var orderId = UUID.randomUUID();
        var changedTimeStamp = ZonedDateTime.now();

        var orderChanged = OrderChanged.builder()
                .orderId(orderId)
                .info("SAULT - UNTITLED")
                .details("Great 2nd album")
                .changedTimestamp(changedTimeStamp).build();

        var orderAggregate = OrderEventsMapper.INSTANCE.mapOrderChanged(orderChanged);

        assertThat(orderAggregate)
                .extracting("id", "name", "description", "lastModified")
                .containsExactly(orderId, "SAULT - UNTITLED", "Great 2nd album", changedTimeStamp);
    }


    /////////////// Error on unmapped //////////////////

    @Value
    @Builder
    static class VinylRecord2 {
        private Date releaseYear;
        private String artist;
        private String title;
    }

    @Value
    @Builder
    static class ShopProduct2 {
        private ZonedDateTime manufactured;
        private String mainTitle;
        private String description;
    }

    @MapperConfig(
            // this gives an error in the IDE AND the maven build
//            unmappedTargetPolicy = ReportingPolicy.ERROR
    )
    public interface CentralConfig {
    }

    // alternatively one can configure this in the configuration of the maven compiler plugin.
    // however, this only fails at maven run but NOT in the IDE:
    //             <compilerArg>
    //              -Amapstruct.unmappedTargetPolicy=ERROR
    //            </compilerArg>
    // See pom.xml for details

    @Mapper(config = CentralConfig.class)
    interface NullMapper {
        NullMapper INSTANCE = Mappers.getMapper(NullMapper.class);

        ShopProduct2 mapVinylRecord(VinylRecord2 vinylRecord);
    }

    @Test
    public void forgotMapperConfigLeadsToNoMaping() {
        var vinylRecord = VinylRecord2.builder()
                .artist("Sinning Apes")
                .title("Crazy Apes")
                .releaseYear(new Date(2017 - 1900, 8, 1)).build();

        var shopProduct = NullMapper.INSTANCE.mapVinylRecord(vinylRecord);

        assertThat(shopProduct)
                .extracting("mainTitle", "description", "manufactured")
                .containsExactly(null, null, null);
    }
}
