package org.samba;

import com.google.common.collect.Streams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;


// more examples for groupingBy: https://www.baeldung.com/java-groupingby-collector

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

        assertThat(result).contains(1, 2, 5, 6);
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
    public void findLast() {
        Optional<Integer> lastNumber = Streams.findLast(
                Stream.of(1, 2, 3));

        assertThat(lastNumber.get()).isEqualTo(3);
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

    @Test
    public void summingUp() {
        Double sum = Stream.of(
                new Temperature(10),
                new Temperature(5),
                new Temperature(4))
                .collect(Collectors.summingDouble(Temperature::getValue));

        assertThat(sum).isEqualTo(19);
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

    @Test
    public void mapWithSimpleLambdaAsValue() {
        Map<String, String> studentByName = Stream.of(
                new Student("Ronny", "Schmidt"),
                new Student("Petra", "Müller"))
                .collect(Collectors.toMap(Student::getLastName, it -> it.getFirstName().toUpperCase()));

        assertThat(studentByName).containsExactly(
                entry("Schmidt", "RONNY"),
                entry("Müller", "PETRA"));
    }

    @Test
    public void obscureCollectionToHashmap() {
        HashMap<Object, Object> result = Stream.of(
                new Student("Ronny", "Schmidt"),
                new Student("Petra", "Müller"))
                .collect(HashMap::new,
                        (hashMap, student) -> hashMap.put(
                                student.getLastName(),
                                student.getFirstName()),
                        HashMap::putAll);

        assertThat(result).containsExactly(
                entry("Schmidt", "Ronny"),
                entry("Müller", "Petra"));
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
    public void countOccurrence() {
        Map<String, Long> characterOccurrence = Stream.of(
                "A", "A",
                "B",
                "C", "C", "C")
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        assertThat(characterOccurrence).containsExactly(
                entry("A", 2L),
                entry("B", 1L),
                entry("C", 3L));
    }

    @Test
    public void groupByCounting() {
        var sum = Stream.of("Ale", "Ale", "Lager", "Ale")
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        assertThat(sum).containsExactly(entry("Lager", 1L), entry("Ale", 3L));
    }

    @Test
    public void groupingYoungestStudentByGender() {
        Map<Gender, Optional<Student2>> youngestStudentByGender = Stream.of(
                new Student2("Ronny", "Schmidt", 40, Gender.M),
                new Student2("Susi", "Sorglos", 32, Gender.F),
                new Student2("Klaus", "Schmidt", 55, Gender.M),
                new Student2("Petra", "Müller", 62, Gender.F))
                .collect(Collectors.groupingBy(Student2::getGender,
                        Collectors.minBy(Comparator.comparing(Student2::getAge))));

        assertThat(youngestStudentByGender)
                .hasSize(2)
                .contains(
                        entry(Gender.M, Optional.of(new Student2("Ronny", "Schmidt", 40, Gender.M))),
                        entry(Gender.F, Optional.of(new Student2("Susi", "Sorglos", 32, Gender.F))));
    }

    @Test
    public void studentsPartitionedByAge() {
        Map<Boolean, List<Student2>> studentsPartitionedByAge = Stream.of(
                new Student2("Ronny", "Schmidt", 40, Gender.M),
                new Student2("Susi", "Sorglos", 32, Gender.F),
                new Student2("Klaus", "Schmidt", 55, Gender.M),
                new Student2("Petra", "Müller", 62, Gender.F))
                .collect(Collectors.partitioningBy(it -> it.age >= 40));

        assertThat(studentsPartitionedByAge.get(true))
                .hasSize(3)
                .extracting("firstName")
                .containsExactly("Ronny", "Klaus", "Petra");

        assertThat(studentsPartitionedByAge.get(false))
                .hasSize(1)
                .extracting("firstName")
                .containsExactly("Susi");
    }

    @Data
    static class Student2 {
        private final String firstName;
        private final String lastName;
        private final int age;
        private final Gender gender;
    }

    static enum Gender {M, F, D}

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

        assertThat(result).containsExactly(3, 2, 1);
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

    // there is Stream.concat and Streams.concat (guava) with apparently identical functionality
    @Test
    public void concatenateTwoStreams() {
        List<String> result = Stream.concat(
                Stream.of("A", "B"),
                Stream.of("X", "Y", "Z"))
                .collect(Collectors.toList());

        assertThat(result).containsExactly("A", "B", "X", "Y", "Z");
    }

    @Test
    public void collectToAlternativeCollectionType() {
        Stream.of(1, 2, 3)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    // "The resulting stream will only be as long as the shorter of the two input streams;
    // if one stream is longer, its extra elements will be ignored."
    @Test
    public void zipToPrimitiveLists() {
        List<Integer> result = Streams.zip(
                Stream.of(10, 20, 30),
                Stream.of(1, 2, 3),
                (i, j) -> i + j)
                .collect(Collectors.toList());

        assertThat(result).containsExactly(11, 22, 33);
    }

    @Test
    public void zipToListOfNewObjects() {
        List<Student> students = Streams.zip(
                Stream.of("Ronny", "Petra"),
                Stream.of("Schmidt", "Mueller"),
                Student::new)
                .collect(Collectors.toList());

        assertThat(students).containsExactly(
                new Student("Ronny", "Schmidt"),
                new Student("Petra", "Mueller"));
    }

    // saves one line comapred to Streams.zip + stream forEach
    @Test
    public void pairWiseForEach() {
        Streams.forEachPair(
                Stream.of(10, 20, 30),
                Stream.of(1, 2, 3),
                (i, j) -> System.out.println(String.format("%d + %d = %d", i, j, i + j)));

        // output
        // 10 + 1 = 11
        // 20 + 2 = 22
        // 30 + 3 = 33
    }

    @Test
    public void mapWithIndex() {
        Streams.mapWithIndex(
                Stream.of("A", "B", "C"),
                // intentionally explicit, could be also a method reference
                (character, index) -> new AbstractMap.SimpleEntry(character, index))
                .forEach(pair -> System.out.println(String.format("%s %d", pair.getKey(), pair.getValue())));

        // output:
        // A 0
        // B 1
        // C 2
    }


    @Value
    @AllArgsConstructor
    static class Temperature implements Comparable<Temperature> {
        private final double value;

        public Temperature plus(Temperature summand) {
            return new Temperature(value + summand.getValue());
        }

        @Override
        public int compareTo(Temperature other) {
            return Double.compare(this.value, other.value);
        }
    }
}
