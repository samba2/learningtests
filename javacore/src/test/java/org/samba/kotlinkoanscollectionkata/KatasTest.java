package org.samba.kotlinkoanscollectionkata;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.samba.kotlinkoanscollectionkata.TestShop.*;

class KatasTest {

    @Test
    public void introduction() {
        // Get a set of all shop customers
        var result = Set.copyOf(shop.getCustomers());
        System.out.println(result);
    }

    @Test
    public void filterAndMap() {
        // Return the set of cities the customers are from
        Set<City> cities = shop.getCustomers().stream()
                .map(it -> it.getCity())
                .collect(Collectors.toSet());

        assertThat(cities)
                .hasSize(5)
                .extracting("name")
                .contains("Tokyo", "Ankara", "Vancouver", "Budapest", "Canberra");

        // Return a list of the customers who live in the given city
        assertThat(getCustomersFrom(shop, new City("Budapest")))
                .hasSize(1)
                .extracting("name")
                .containsExactly("Reka");
    }

    private static List<Customer> getCustomersFrom(Shop shop, City city) {
        return shop.getCustomers().stream()
                .filter(it -> it.getCity().equals(city))
                .collect(Collectors.toList());
    }


    @Test
    public void allAnyAndOrderPredicates() {
        // Return true if all customers are from the given city
        assertThat(checkAllCustomersAreFrom(shop, new City("Tokyo"))).isFalse();

        // Return true if there is at least one customer from the given city
        assertThat(hasCustomerFrom(shop, new City("Tokyo"))).isTrue();

        // Return the number of customers from the given city
        assertThat(countCustomersFrom(shop, new City("Tokyo"))).isEqualTo(1L);

        // Return a customer who lives in the given city, or Optional.empty if there is none
        assertThat(findAnyCustomerFrom(shop, new City("Tokyo"))).isPresent();
        assertThat(findAnyCustomerFrom(shop, new City("Tokyo")).get().getName()).isEqualTo("Asuka");
    }

    private static boolean checkAllCustomersAreFrom(Shop shop, City city) {
        return shop.getCustomers().stream()
                .allMatch(customer -> customer.getCity().equals(city));
    }

    private static boolean hasCustomerFrom(Shop shop, City city) {
        return shop.getCustomers().stream()
                .anyMatch(customer -> customer.getCity().equals(city));
    }

    private static long countCustomersFrom(Shop shop, City city) {
        return shop.getCustomers().stream()
                .filter(customer -> customer.getCity().equals(city))
                .count();
    }

    private static Optional<Customer> findAnyCustomerFrom(Shop shop, City city) {
        return shop.getCustomers().stream()
                .filter(customer -> customer.getCity().equals(city))
                .findAny();
    }

    @Test
    public void flatMap() {
        // Return all products this customer has ordered
        assertThat(orderedProducts(shop.getCustomers().get(0)))
                .hasSize(4)
                .extracting("name")
                .containsExactly("ReSharper", "ReSharper", "DotMemory", "DotTrace");

        // Return all products that were ordered by at least one customer
        assertThat(allOrderedProducts(shop))
                .hasSize(6)
                .extracting("name")
                .containsExactly("RubyMine", "IntelliJ IDEA Ultimate", "DotMemory", "DotTrace", "WebStorm", "ReSharper");
    }

    private static List<Product> orderedProducts(Customer customer) {
        return customer.getOrders().stream()
                .flatMap(order -> order.getProducts().stream())
                .collect(Collectors.toList());
    }

    private static Set<Product> allOrderedProducts(Shop shop) {
        return shop.getCustomers().stream()
                .flatMap(customer -> customer.getOrders().stream())
                .flatMap(order -> order.getProducts().stream())
                .collect(Collectors.toSet());
    }

    @Test
    public void maxMin() {
        // Return a customer whose order count is the highest among all customers
        assertThat(getCustomerWithMaximumNumberOfOrders(shop).get().getName()).isEqualTo("Reka");

        // Return the most expensive product which has been ordered
        assertThat(getMostExpensiveOrderedProduct(shop.getCustomers().get(0)).get().getName())
                .isEqualTo("DotTrace");

    }

    private static Optional<Customer> getCustomerWithMaximumNumberOfOrders(Shop shop) {
        return shop.getCustomers().stream()
                .max(Comparator.comparing(customer -> customer.getOrders().size()));
    }

    private static Optional<Product> getMostExpensiveOrderedProduct(Customer customer) {
        return customer.getOrders().stream()
                .flatMap(order -> order.getProducts().stream())
                .max(Comparator.comparing(Product::getPrice));
    }

    @Test
    public void sort() {
        // Return a list of customers, sorted by the ascending number of orders they made
        var result = getCustomersSortedByNumberOfOrders(shop);
        assertThat(result.get(0).getName()).isEqualTo("Cooper");
        assertThat(result.get(5).getName()).isEqualTo("Reka");

    }

    private static List<Customer> getCustomersSortedByNumberOfOrders(Shop shop) {
        return shop.getCustomers().stream()
                .sorted(Comparator.comparing(customer -> customer.getOrders().size()))
                .collect(Collectors.toList());
    }

    @Test
    public void sum() {
         // Return the sum of prices of all products that a customer has ordered.
        // Note: the customer may order the same product for several times.
        assertThat(getTotalOrderPrice(shop.getCustomers().get(0)))
                .isEqualTo(586);
    }

    private static Double getTotalOrderPrice(Customer customer) {
        return customer.getOrders().stream()
                .flatMap(order -> order.getProducts().stream())
                .mapToDouble(Product::getPrice)
                .sum();
    }

    // TODO continue here: https://play.kotlinlang.org/koans/Collections/GroupBy/Task.kt

}