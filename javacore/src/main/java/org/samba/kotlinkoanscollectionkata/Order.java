package org.samba.kotlinkoanscollectionkata;

import lombok.Value;

import java.util.List;

@Value
public class Order {
    private final List<Product> products;
    private final boolean isDelivered;
}
