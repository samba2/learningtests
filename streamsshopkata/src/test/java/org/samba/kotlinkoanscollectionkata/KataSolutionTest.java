package org.samba.kotlinkoanscollectionkata;

import com.google.common.collect.Sets;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.samba.kotlinkoanscollectionkata.TestShop.*;

class KataSolutionTest {

    @Test
    public void introduction() {
        // Get a set of all shop customers
        Assertions.assertThat(getSetOfCustomers(shop))
                .isNotEmpty()
                .hasSize(6);
    }

    private static Set<Customer> getSetOfCustomers(Shop shop) {
        return Set.copyOf(shop.getCustomers());
    }

    @Test
    public void filterAndMap1() {
        // Return the set of cities the customers are from
        Assertions.assertThat(getCitiesCustomersAreFrom(shop))
                .hasSize(5)
                .extracting("name")
                .contains("Tokyo", "Ankara", "Vancouver", "Budapest", "Canberra");
    }

    private static Set<City> getCitiesCustomersAreFrom(Shop shop) {
        return shop.getCustomers().stream()
                .map(it -> it.getCity())
                .collect(Collectors.toSet());
    }

    @Test
    public void filterAndMap2() {
        // Return a list of the customers who live in the given city
        Assertions.assertThat(getCustomersFrom(shop, new City("Budapest")))
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
        Assertions.assertThat(findAnyCustomerFrom(shop, new City("Tokyo"))).isPresent();
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
        Assertions.assertThat(orderedProducts(shop.getCustomers().get(0)))
                .hasSize(4)
                .extracting("name")
                .containsExactly("ReSharper", "ReSharper", "DotMemory", "DotTrace");

        // Return all products that were ordered by at least one customer
        Assertions.assertThat(allOrderedProducts(shop))
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

    @Test
    public void groupCustomersByCity() {
         // Return a map of the customers living in each city
        var result = groupCustomersByCity(shop);
        Assertions.assertThat(result).hasSize(5);

        Assertions.assertThat(result.get(TestShop.Tokyo))
                .extracting("name")
                .containsExactly("Asuka");

        Assertions.assertThat(result.get(TestShop.Canberra))
                .extracting("name")
                .containsExactly("Lucas", "Cooper");
    }

    private static Map<City, List<Customer>> groupCustomersByCity(Shop shop) {
        return shop.getCustomers().stream()
                .collect(Collectors.groupingBy(Customer::getCity));
    }

    @Test
    public void partitionBy() {
        // Return customers who have more undelivered orders than delivered
        Assertions.assertThat(getCustomersWithMoreUndeliveredOrdersThanDelivered2(shop))
                .hasSize(1)
                .extracting("name")
                .containsExactly("Reka");
    }

    // follows the proposed implementation
    private static Set<Customer> getCustomersWithMoreUndeliveredOrdersThanDelivered2(Shop shop) {
        return shop.getCustomers().stream()
                .filter(customer -> {
                    var partitionedOrders = customer.getOrders().stream()
                            .collect(Collectors.partitioningBy(Order::isDelivered));
                    return partitionedOrders.get(false).size() > partitionedOrders.get(true).size();
                })
                .collect(Collectors.toSet());
    }

    // this was way to complicated. left this because of the nicely extracted functions
    private static Set<Customer> getCustomersWithMoreUndeliveredOrdersThanDelivered(Shop shop) {
        Function<Customer, Long> notDeliveredCount = customer -> customer.getOrders().stream()
                .filter(order -> order.isDelivered() == false)
                .count();

        Function<Customer, Long> deliveredCount = customer -> customer.getOrders().stream()
                .filter(order -> order.isDelivered())
                .count();

        Predicate<Customer> customerPredicate = customer -> notDeliveredCount.apply(customer) > deliveredCount.apply(customer);

        return shop.getCustomers().stream()
                .collect(Collectors.partitioningBy(customerPredicate, Collectors.toSet())).get(true);
    }

    @Test
    public void reduce() {
         // Return the set of products that were ordered by every customer
        Assertions.assertThat(getSetOfProductsOrderedByEveryCustomer(shop)).isEmpty();
        Assertions.assertThat(getSetOfProductsOrderedByEveryCustomer2(shop)).isEmpty();
    }


    // follows the suggested implementation
    // algorithm:
    // - start with a set of all products ever ordered by any customer
    // - at each reduction step, calculate the common products of the previous step and the current. Take the outcome
    //   as the input (= new "commonProducts") for the next reduction step. For the first step this is the full "allProducts" set.
    // - with each reduction step the set of common products is either untouched or decreased
    // - the third combiner argument is needed since "commonProducts" and "customer" are of different type.
    //   See here for details (2nd answer): https://stackoverflow.com/questions/24308146/why-is-a-combiner-needed-for-reduce-method-that-converts-type-in-java-8
    private static Set<Product> getSetOfProductsOrderedByEveryCustomer(Shop shop) {
        return shop.getCustomers().stream()
                .reduce(getAllProducts(shop),
                        (commonProducts, customer) -> Sets.intersection(commonProducts, getCustomerProducts(customer)),
                        Sets::union);
    }

    // less funky implementation using local state
    private static Set<Product> getSetOfProductsOrderedByEveryCustomer2(Shop shop) {
        var allProducts = getAllProducts(shop);
        shop.getCustomers().stream()
                .forEach(customer -> allProducts.retainAll(getCustomerProducts(customer)));
        return allProducts;
    }

    private static Set<Product> getAllProducts(Shop shop) {
        return shop.getCustomers().stream()
                .flatMap(customer -> customer.getOrders().stream())
                .flatMap(order -> order.getProducts().stream())
                .collect(Collectors.toSet());
    }

    private static Set<Product> getCustomerProducts(Customer customer) {
        return customer.getOrders().stream()
                .flatMap(order -> order.getProducts().stream())
                .collect(Collectors.toSet());
    }

    @Test
    public void compoundTask1() {
         // Return the most expensive product among all delivered products
        // (use the Order.isDelivered flag)
        var testCustomer = TestShop.customer(TestShop.reka, TestShop.Budapest,
                TestShop.order(false, TestShop.idea),
                TestShop.order(false, TestShop.idea),
                TestShop.order(TestShop.reSharper),
                TestShop.order(TestShop.reSharper, TestShop.dotMemory, TestShop.dotTrace, TestShop.teamCity));

        Assertions.assertThat(getMostExpensiveDeliveredProduct(testCustomer))
                .isPresent()
                .contains(TestShop.teamCity);
    }

    private static Optional<Product> getMostExpensiveDeliveredProduct(Customer customer) {
        return customer.getOrders().stream()
                .filter(Order::isDelivered)
                .flatMap(order -> order.getProducts().stream())
                .max(Comparator.comparing(Product::getPrice));
    }

    @Test
    public void compoundTask2() {
        // Return how many times the given product was ordered.
        // Note: a customer may order the same product for several times.
        assertThat(getNumberOfTimesProductWasOrdered(shop, TestShop.rubyMine)).isEqualTo(1);
        assertThat(getNumberOfTimesProductWasOrdered(shop, TestShop.reSharper)).isEqualTo(3);
    }

    private static long getNumberOfTimesProductWasOrdered(Shop shop, Product product) {
        return shop.getCustomers().stream()
                .flatMap(customer -> customer.getOrders().stream())
                .flatMap(order -> order.getProducts().stream())
                .filter(product1 -> product1.equals(product))
                .count();
    }

}