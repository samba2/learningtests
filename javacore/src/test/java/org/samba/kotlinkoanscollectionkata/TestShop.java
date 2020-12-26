package org.samba.kotlinkoanscollectionkata;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class TestShop {

    //products
    Product idea = new Product("IntelliJ IDEA Ultimate", 199.0);
    Product reSharper = new Product("ReSharper", 149.0);
    Product dotTrace = new Product("DotTrace", 159.0);
    Product dotMemory = new Product("DotMemory", 129.0);
    Product dotCover = new Product("DotCover", 99.0);
    Product appCode = new Product("AppCode", 99.0);
    Product phpStorm = new Product("PhpStorm", 99.0);
    Product pyCharm = new Product("PyCharm", 99.0);
    Product rubyMine = new Product("RubyMine", 99.0);
    Product webStorm = new Product("WebStorm", 49.0);
    Product teamCity = new Product("TeamCity", 299.0);
    Product youTrack = new Product("YouTrack", 500.0);

    //customers
    String lucas = "Lucas";
    String cooper = "Cooper";
    String nathan = "Nathan";
    String reka = "Reka";
    String bajram = "Bajram";
    String asuka = "Asuka";

    //cities
    City Canberra = new City("Canberra");
    City Vancouver = new City("Vancouver");
    City Budapest = new City("Budapest");
    City Ankara = new City("Ankara");
    City Tokyo = new City("Tokyo");

    public static Customer customer(String name, City city, Order... orders) {
        return new Customer(name, city, List.of(orders.clone()));
    }

    public static Order order(Boolean isDelivered, Product... products) {
        return new Order(List.of(products.clone()), isDelivered);
    }

    public static Order order(Product... products) {
        return new Order(List.of(products.clone()), true);
    }

    public static Shop shop(String name, Customer... customers) {
        return new Shop(name, List.of(customers.clone()));
    }

    Shop shop = shop("jb test shop",
            customer(lucas, Canberra,
                    order(reSharper),
                    order(reSharper, dotMemory, dotTrace)
            ),
            customer(cooper, Canberra),
            customer(nathan, Vancouver,
                    order(rubyMine, webStorm)
            ),
            customer(reka, Budapest,
                    order(false, idea),
                    order(false, idea),
                    order(idea)
            ),
            customer(bajram, Ankara,
                    order(reSharper)
            ),
            customer(asuka, Tokyo,
                    order(idea)
            )
    );

    public final Map<String, Customer> customers = shop.getCustomers().stream()
            .collect(Collectors.toMap(Customer::getName, Function.identity()));

    public final Set orderedProducts = Set.of(idea, reSharper, dotTrace, dotMemory, rubyMine, webStorm);

    // TODO
    public static void main(String[] args) {
        var a = Map.of(
                "Canberra", List.of(lucas, cooper),
                "Vancouver", List.of(nathan),
                "Budapest", List.of(reka),
                "Ankara", List.of(bajram),
                "Tokyo", List.of(asuka));
    }

    //val groupedByCities = mapOf(
    //        Canberra to listOf(lucas, cooper),
    //        Vancouver to listOf(nathan),
    //        Budapest to listOf(reka),
    //        Ankara to listOf(bajram),
    //        Tokyo to listOf(asuka)
    //).mapValues { it.value.map { name -> customers[name] } }

}
