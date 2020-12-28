package org.samba.kotlinkoanscollectionkata;

import lombok.Value;

import java.util.List;

@Value
public class Customer {
    private final String name;
    private final City city;
    private final List<Order> orders;

    @Override
    public String toString() {
        return String.format("%s from %s", name, city.getName());
    }

}
