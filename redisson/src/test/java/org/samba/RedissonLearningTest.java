package org.samba;


import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@Slf4j
public class RedissonLearningTest {

    @Container
    public GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.9-alpine"))
            .withExposedPorts(6379);
    private Config config;
    private RedissonClient redissonClient;


    @BeforeEach
    public void setup() {
        config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:" + redis.getFirstMappedPort());
        redissonClient = Redisson.create(config);
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

        RBucket<Person> bucket1 = redissonClient.getBucket("a-person-bucket");
        bucket1.set(person);

        RBucket<Object> bucket2 = redissonClient.getBucket("a-person-bucket");
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

        RMap<UUID, Person> map1 = redissonClient.getMap("a-person-map");
        map1.put(thisPerson.id, thisPerson);
        map1.put(otherPerson.id, otherPerson);

        RMap<UUID, Person> map2 = redissonClient.getMap("a-person-map");
        assertThat(map2).hasSize(2);

        assertThat(map2).containsValues(thisPerson, otherPerson);
    }

    @Test
    public void RMapIsDeserializedCopyOfRedisHashAndNotReference() {
        Map<String, Department> departments = redissonClient.getMap("departmentMap");
        // initialize with empty department
        departments.put("engineering", Department.of());

        // get copy of department RMap from Redis
        Department department = departments.get("engineering");
        // alter local copy (!evil via getter!)
        department.getCourses().put(StudentId.of(UUID.randomUUID()), Course.of("AB1", "Intro into something"));

        // Redis content is unchanged
        assertThat(departments.get("engineering").getCourses()).isEmpty();

        // to make changes visible, update the engineering department, this writes back to Redis
        departments.put("engineering", department);

        // now pushed back changes are present
        assertThat(departments.get("engineering").getCourses()).isNotEmpty();
    }

    @Test
    public void listMultimapKeepsDuplicateKeyValuePairs() {
        RListMultimap<String, String> map = redissonClient.getListMultimap("multiMap");

        map.put("Jonny", "Marr");
        map.put("Jonny", "Marr");
        map.put("Jonny", "Schmidt");
        map.put("Ian", "Curtis");

        assertThat(map.size()).isEqualTo(4);
    }

    @Test
    public void setMultimapRemovesDuplicateKeyValuePairs() {
        RSetMultimap<String, String> map = redissonClient.getSetMultimap("multiMap2");

        map.put("Jonny", "Marr");
        map.put("Jonny", "Marr");
        map.put("Jonny", "Schmidt");
        map.put("Ian", "Curtis");

        assertThat(map.size()).isEqualTo(3);
        assertThat(map.entries()).containsExactlyInAnyOrder(
                entry("Jonny", "Marr"),
                entry("Jonny", "Schmidt"),
                entry("Ian", "Curtis")
        );
    }


    @Value(staticConstructor = "of")
    static class Department implements Serializable {
        Map<StudentId, Course> courses = new HashMap<>();
    }

    @Value(staticConstructor = "of")
    static class OtherDepartment implements Serializable {
        Map<StudentId, Course> courses;
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

        RSetMultimap<StudentId, Course> map1 = redissonClient.getSetMultimap("course-register");
        map1.put(someStudentId, course1);
        map1.put(someStudentId, course2);
        map1.put(someStudentId, course2); // duplication is eliminated due to Set

        RSetMultimap<StudentId, Course> map2 = redissonClient.getSetMultimap("course-register");
        RSet<Course> studentCourses = map2.get(someStudentId);

        assertThat(studentCourses).hasSize(2);
        assertThat(studentCourses).containsAll(List.of(course1, course2));
    }

    @Test
    public void redisLocks() {

        CompletableFuture.runAsync(() -> {
            sleep(1);
            log.info("Client 2 tries to acquired lock");
            RLock lock = redissonClient.getLock("lock");
            lock.lock();
            log.info("Client 2 acquired lock, sleeping now");
            sleep(5);
            log.info("Client 2 done sleeping, about to release");
            lock.unlock();
            log.info("Client 2 released lock.");
        });

        log.info("Client 1 tries to acquired lock");
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
        log.info("Client 1 acquired lock, sleeping now");
        sleep(5);
        lock.unlock();
        log.info("Client 1 released lock.");
    }

    @SneakyThrows
    private void sleep(int seconds) {
        Thread.sleep(seconds * 1000);
    }

}