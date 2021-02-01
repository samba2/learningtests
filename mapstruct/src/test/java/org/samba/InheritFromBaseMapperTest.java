package org.samba;

import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

public class InheritFromBaseMapperTest {

    @Builder
    @Value
    static class OrderAdded {
        UUID orderId;
        String info;
        String details;
        ZonedDateTime addedTimestamp;
    }

    @Value
    static class OrderDto {
        OrderId orderId;
        String info;
        String details;
        long epochSeconds;
    }

    @Value(staticConstructor = "of")
    static class OrderId {
        UUID id;
    }

    @Mapper
    interface BaseMapper {
        default OrderId mapUuidToOrderId(UUID orderId) {
            return OrderId.of(orderId);
        }

        default long mapZonedDateTimeToIsoString(ZonedDateTime zonedDateTime) {
            return zonedDateTime.toEpochSecond();
        }
    }


    @MapperConfig(unmappedTargetPolicy = ReportingPolicy.ERROR)
    interface MyConfig {};

    // ! direct usage of @MapperConfig here does not work (is ignored)
    @Mapper(config = MyConfig.class)
    interface OrderMapper extends BaseMapper {
        OrderMapper MAPPER = Mappers.getMapper(OrderMapper.class);

        @Mapping(target = "epochSeconds", source = "addedTimestamp")
        OrderDto map(OrderAdded orderAdded);
    }

    @Test
    public void baseMapperIsUsedForCommonMappings() {
        var orderAdded = OrderAdded.builder()
                .orderId(UUID.randomUUID())
                .info("My Info")
                .details("Some details")
                .addedTimestamp(ZonedDateTime.now())
                .build();

        var orderDto = OrderMapper.MAPPER.map(orderAdded);

        assertThat(orderDto)
                .hasNoNullFieldsOrProperties()
                .extracting(OrderDto::getEpochSeconds)
                .matches(it -> it > 0, "epoch seconds are set");
    }
}
