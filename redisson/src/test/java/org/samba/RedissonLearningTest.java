package org.samba;


import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
public class RedissonLearningTest {

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.9-alpine"))
            .withExposedPorts(6379);
    private Config config;
    private RedissonClient client;


    @BeforeEach
    public void setup() {
        config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:" + redis.getFirstMappedPort());
        client = Redisson.create(config);
    }

    @Value
    @Builder
    static class Person implements Serializable {
        UUID id;
        String firstName;
        String lastName;
    }

    @Test
    public void storeObjectInBucket() {
        var person = Person.builder()
                .id(UUID.randomUUID())
                .firstName("Ronny")
                .lastName("Schmidt").build();

        RBucket<Person> bucket1 = client.getBucket("a-person-bucket");
        bucket1.set(person);

        RBucket<Object> bucket2 = client.getBucket("a-person-bucket");
        assertThat(bucket2.get()).isEqualTo(person);
    }


    @Test
    public void storeMapInBucket() {
        var thisPerson = Person.builder()
                .id(UUID.randomUUID())
                .firstName("Ronny")
                .lastName("Schmidt").build();

        var otherPerson = Person.builder()
                .id(UUID.randomUUID())
                .firstName("Petra")
                .lastName("Mueller").build();

        RMap<UUID, Person> map1 = client.getMap("a-person-map");
        map1.put(thisPerson.id, thisPerson);
        map1.put(otherPerson.id, otherPerson);

        RMap<UUID, Person> map2 = client.getMap("a-person-map");
        assertThat(map2).hasSize(2);

        assertThat(map2).containsValues(thisPerson, otherPerson);
    }


    @Value(staticConstructor = "of")
    static class StudentId implements Serializable {
        UUID id;
    }

    @Value(staticConstructor = "of")
    static class Course implements Serializable {
        String code;
        String description;
    }

    @Test
    public void storeMultiMapInBucket() {
        StudentId someStudentId = StudentId.of(UUID.randomUUID());
        Course course1 = Course.of("C1", "Introduction into Breakdance");
        Course course2 = Course.of("C2", "Advanced Head-Spinning");

        RSetMultimap<StudentId, Course> map1 = client.getSetMultimap("course-register");
        map1.put(someStudentId, course1);
        map1.put(someStudentId, course2);
        map1.put(someStudentId, course2); // duplication is eliminated due to Set

        RSetMultimap<StudentId, Course> map2 = client.getSetMultimap("course-register");
        RSet<Course> studentCourses = map2.get(someStudentId);

        assertThat(studentCourses).hasSize(2);
        assertThat(studentCourses).containsAll(List.of(course1, course2));
    }
}