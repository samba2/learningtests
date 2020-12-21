package org.samba;

import lombok.Data;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;


public class StreamParadeLearningTest {

    @Test
    public void minTerminalOperation() {
        var smallestNumber = Stream.of("2", "3", "1", "4")
                .mapToLong(Long::parseLong)
                .min()
                .orElseThrow();

        assertThat(smallestNumber).isEqualTo(1L);
    }

    @Test
    public void sum() {
        var sum = Stream.of("2", "3", "1", "4")
                .mapToLong(Long::parseLong)
                .sum();

        assertThat(sum).isEqualTo(10L);
    }

    // API doc: Returns a stream consisting of the results of replacing each element of this stream with the contents
    // of a mapped stream produced by applying the provided mapping function to each element.
    @Test
    public void flatMap() {
        var result = Stream.of(List.of(1, 2), List.of(3, 4), List.of(5, 6))
                .peek(System.out::println)
                // [1, 2]
                // [3, 4]
                // [5, 6]
                .flatMap(Collection::stream)
                .peek(System.out::println)
                .collect(Collectors.toList());

        assertThat(result).containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    public void flatMapWithSets() {
        var result = List.of(Set.of(1, 2), Set.of(1, 2), Set.of(5, 6)).stream()
                .flatMap(Collection::stream) // could also be Set::stream
                .distinct()  // remove duplicates
                .collect(Collectors.toList());

        assertThat(result).containsExactly(1, 2, 5, 6);
    }

    @Test
    public void sortingViaComparator() {
        var firstStudent = Stream.of(
                new Student("Ronny", "Schmidt"),
                new Student("Valerie", "Mueller"),
                new Student("Aron", "Albrecht"))
                .sorted(Comparator.comparing(Student::getFirstName))
                .findFirst()
                .orElseThrow();

        assertThat(firstStudent.getFirstName()).isEqualTo("Aron");
    }

    @Data
    static class Student {
        private final String firstName;
        private final String lastName;
    }

    @Test
    public void skipAndLimit() {
        var result = IntStream.rangeClosed(1, 10)
                .boxed()  // work on Integer objects
                .skip(3)
                .limit(2)
                .collect(Collectors.toList());

        assertThat(result).containsExactly(4, 5);
    }

    @Test
    public void count() {
        var evenNumbers = IntStream.rangeClosed(1, 5).boxed()
                .filter(integer -> integer % 2 == 0)
                .count();

        assertThat(evenNumbers).isEqualTo(2);
    }


    @Test
    public void anyMatch() {
        var containsRice = List.of("potato", "rice", "carrot").stream()
                .anyMatch(s -> s.equals("rice"));

        assertThat(containsRice).isTrue();
    }

    @Test
    public void noneMatch() {
        var containsNoApples = List.of("potato", "rice", "carrot").stream()
                .noneMatch(s -> s.equals("apple"));

        assertThat(containsNoApples).isTrue();
    }

    @Test
    public void addingValueObjectsViaReduce() {
        var sumOfPositiveTemperatures = Stream.of(
                new Temperature(10),
                new Temperature(-10),
                new Temperature(4))
                .filter(it -> it.getValue() > 0)  // only consider positive temperatures
                .reduce(Temperature::plus)
                .orElseThrow();

        assertThat(sumOfPositiveTemperatures).isEqualTo(new Temperature(14));
    }

    @Value
    static class Temperature {
        private final double value;

        public Temperature(double value) {
            this.value = value;
        }

        public Temperature plus(Temperature summand) {
            return new Temperature(value + summand.getValue());
        }
    }
}
