package org.samba.kotlinkoanscollectionkata;

import lombok.Value;

@Value
public class Product {
    private final String name;
    private final Double price;

    @Override
    public String toString() {
        return String.format("'%s' for %s", name, price);
    }
}
