package org.samba;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.OutputStream;
import java.io.PrintWriter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@Slf4j
@WebMvcTest
public class AppTest {

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    public void renameMe() {
        var result = this.mockMvc.perform(get("/greeting?name=petra"))
                .andExpect(status().isOk())
                .andReturn();

        prettyPrintResult(result);
    }

    @SneakyThrows
    private static void prettyPrintResult(MvcResult result) {
        var objectMapper = new ObjectMapper();
        var content = result.getResponse().getContentAsString();
        var jsonNode = objectMapper.readTree(content);
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
    }

}


