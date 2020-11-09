package org.samba.recordstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.factcast.test.AbstractFactCastIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles({"integration"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class IntegrationTest extends AbstractFactCastIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // TODO fails with: org.springframework.web.util.NestedServletException: Request processing failed; nested exception is org.springframework.http.converter.HttpMessageConversionException: Type definition error: [simple type, class java.time.LocalDate]; nested exception is com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `java.time.LocalDate` (no Creators, like default constructor, exist): no String-argument constructor/factory method to deserialize from String value ('2020-01-30')
    // seems like a config/ missing dependency problem ?!
    @Test
    public void makeMeRun() throws Exception {
        var mapper = new ObjectMapper();
        var newRecord = mapper.createObjectNode();
        newRecord.put("artist", "The Dancing Monkeys");
        newRecord.put("title", "Dancing Time");
        newRecord.put("label", "Ape Records");
        newRecord.put("format", "12");
        newRecord.put("releaseDate", "2020-01-30");

        var json = mapper.writeValueAsString(newRecord);

        this.mockMvc.perform(post("/recordstore")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print());
//        assertTrue(controller != null);
    }
}
