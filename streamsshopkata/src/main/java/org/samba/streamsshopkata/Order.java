package org.samba.streamsshopkata;

import lombok.Value;

import java.util.List;

@Value
public class Order {
    private final List<Product> products;
    private final boolean isDelivered;
}
