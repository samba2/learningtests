package org.samba.kotlinkoanscollectionkata;

import lombok.Value;

import java.util.List;

@Value
public class Shop {
    private final String name;
    private final List<Customer> customers;
}
