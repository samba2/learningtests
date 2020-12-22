package org.samba;

import lombok.Data;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
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
    public void allMatch() {
        var allEvent = List.of(2, 4, 6).stream()
                .allMatch(it -> it % 2 == 0);

        assertThat(allEvent).isTrue();
    }


    // pick just one of the stream elements without a guarantee which one exactly
    @Test
    public void findAny() {
        Optional<String> result = Stream.of("house", "chair", "table")
                .findAny();

        assertThat(result).isPresent();
        System.out.println(result.get());
    }


    @Test
    public void summingValueObjectsViaReduce() {
        var sumOfPositiveTemperatures = Stream.of(
                new Temperature(10),
                new Temperature(-10),
                new Temperature(4))
                .filter(it -> it.getValue() > 0)  // only consider positive temperatures
                .reduce(Temperature::plus)
                .orElseThrow();

        assertThat(sumOfPositiveTemperatures).isEqualTo(new Temperature(14));
    }

    @Test
    public void summingValueObjectsViaReduceWithInitialValue() {
        var baseTemperature = new Temperature(42);
        var sum = Stream.of(
                new Temperature(10),
                new Temperature(4))
                .reduce(baseTemperature, Temperature::plus);

        assertThat(sum).isEqualTo(new Temperature(56));
    }


    // throws IllegalStateException on duplicate keys
    @Test
    public void mapOfStudentsByLastName() {
        Map<String, Student> studentByName = Stream.of(
                new Student("Ronny", "Schmidt"),
                new Student("Petra", "Müller"))
                .collect(Collectors.toMap(Student::getLastName, Function.identity()));

        assertThat(studentByName).containsExactly(
                entry("Schmidt", new Student("Ronny", "Schmidt")),
                entry("Müller", new Student("Petra", "Müller")));
    }


    // allow multiple keys
    // also see: https://stackoverflow.com/questions/45231351/differences-between-collectors-tomap-and-collectors-groupingby-to-collect-in/45231743
    @Test
    public void groupStudentsByLastName() {
        Map<String, List<Student>> studentByLastname = Stream.of(
                new Student("Ronny", "Schmidt"),
                new Student("Klaus", "Schmidt"),
                new Student("Petra", "Müller"))
                .collect(Collectors.groupingBy(Student::getLastName));

        assertThat(studentByLastname.get("Müller")).containsExactly(
                new Student("Petra", "Müller"));

        assertThat(studentByLastname.get("Schmidt")).containsExactly(
                new Student("Ronny", "Schmidt"), new Student("Klaus", "Schmidt"));
    }

    @Test
    public void joinStringViaStream() {
        String result = Stream.of("A", "B", "C").collect(Collectors.joining(","));
        assertThat(result).isEqualTo("A,B,C");
    }

    @Test
    public void reverseList() {
        var result = Stream.of(1, 2, 3)
                // initial implementation, IntelliJ suggested "reverseOrder" replacement
                //.sorted((i1, i2) -> i2.compareTo(i1))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toUnmodifiableList());

        assertThat(result).containsExactly(3,2,1);
    }

    @Test
    public void minViaComparableInterface() {
        var coldestTemperature = Stream.of(
                new Temperature(10),
                new Temperature(-10),
                new Temperature(4))
                .min(Temperature::compareTo)
                .orElseThrow();

        assertThat(coldestTemperature).isEqualTo(new Temperature(-10));
    }


    @Test
    public void maxViaComparingLambda() {
        var studentLastInAlphabet = Stream.of(
                new Student("Anton", "Auer"),
                new Student("Zoe", "Zappa"))
                .max(Comparator.comparing(Student::getLastName))
                .orElseThrow();

        assertThat(studentLastInAlphabet).isEqualTo(new Student("Zoe", "Zappa"));
    }


    @Value
    static class Temperature implements Comparable<Temperature> {
        private final double value;

        public Temperature(double value) {
            this.value = value;
        }

        public Temperature plus(Temperature summand) {
            return new Temperature(value + summand.getValue());
        }

        @Override
        public int compareTo(Temperature other) {
            return Double.compare(this.value, other.value);
        }
    }
}
