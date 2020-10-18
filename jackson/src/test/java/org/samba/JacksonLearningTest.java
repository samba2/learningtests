package org.samba;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JacksonLearningTest {

    // TODO continue from here: https://www.baeldung.com/jackson-object-mapper-tutorial#3-handling-date-formats

    @Test
    public void deserializeJsonToObject() throws JsonProcessingException {
        val mapper = new ObjectMapper();
        val studentJson = "{\"name\":\"Mahesh\", \"age\":21}";

        Student student = mapper.readValue(studentJson, Student.class);
        assertEquals("Mahesh", student.getName());
        assertEquals(21, student.getAge());
    }

    @Test
    public void serializeJson() throws JsonProcessingException {
        val student = new Student("org.samba.Ronny", 20);

        val mapper = new ObjectMapper();
        val jsonString = mapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(student);

        System.out.println(jsonString);
    }

    @Test
    public void readJsonProperty() throws JsonProcessingException {
        val studentJson = "{\"name\":\"Mahesh\", \"age\":21}";
        val mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readTree(studentJson);
        assertEquals("Mahesh", jsonNode.get("name").asText());
    }

    @Test
    public void ignoreNewJsonProperties() throws JsonProcessingException {
        val studentJson = "{\"name\":\"Mahesh\", \"age\":21, \"lastname\":\"Schmidt\"}";
        val mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Student student = mapper.readValue(studentJson, Student.class);
        assertEquals("Mahesh", student.getName());
        assertEquals(21, student.getAge());
    }

    @Test
    public void customSerializer() throws JsonProcessingException {
        val mapper = new ObjectMapper();
        val module = new SimpleModule("CustomCarSerializer");
        module.addSerializer(Student.class, new CustomStudentSerializer());
        mapper.registerModule(module);

        val student = new Student("org.samba.Ronny", 21);
        val jsonWithExtraProperty = mapper.writeValueAsString(student);

        System.out.println(jsonWithExtraProperty);
    }

    @Test
    public void enrichPojo() throws JsonProcessingException {
        val studentJson = "{\"name\":\"Mahesh\", \"age\":21}";
        val mapper = new ObjectMapper();

        val module = new SimpleModule("CustomStudentDeSerializer");
        module.addDeserializer(EnrichedStudent.class, new CustomStudentDeSerializer());
        mapper.registerModule(module);

        val enrichedStudent = mapper.readValue(studentJson, EnrichedStudent.class);
        assertEquals("Mahesh", enrichedStudent.getName());
        assertEquals(21, enrichedStudent.getAge());
        assertTrue(enrichedStudent.isFullAge());
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Student {
        private String name;
        private int age;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class EnrichedStudent {
        private String name;
        private int age;
        private boolean fullAge;
    }

    static public class CustomStudentSerializer extends StdSerializer<Student> {

        public CustomStudentSerializer() {
            this(null);
        }

        public CustomStudentSerializer(Class<Student> t) {
            super(t);
        }

        @Override
        public void serialize(Student student, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", student.getName());
            jsonGenerator.writeNumberField("age", student.getAge());
            val fullAged = student.getAge() >= 21;
            jsonGenerator.writeBooleanField("fullAged", fullAged);
            jsonGenerator.writeEndObject();
        }
    }

    static public class CustomStudentDeSerializer extends StdDeserializer<EnrichedStudent> {

        protected CustomStudentDeSerializer() {
            this(null);
        }

        protected CustomStudentDeSerializer(JavaType valueType) {
            super(valueType);
        }

        @Override
        public EnrichedStudent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            val codec = jsonParser.getCodec();
            JsonNode node = codec.readTree(jsonParser);
            val age = node.get("age").asInt();

            val student = new EnrichedStudent();
            student.setName(node.get("name").asText());
            student.setAge(age);
            student.setFullAge(age >= 21);

            return student;
        }
    }
}
