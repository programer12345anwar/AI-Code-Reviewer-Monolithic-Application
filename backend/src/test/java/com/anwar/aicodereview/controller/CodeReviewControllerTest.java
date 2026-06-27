package com.anwar.aicodereview.controller;

import com.anwar.aicodereview.service.CodeService;
import com.anwar.aicodereview.service.VersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CodeReviewController.class)
class CodeReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CodeService codeService;

    @MockBean
    private VersionService versionService;

    @Test
    void helloEndpointShouldReturnSuccessPayload() throws Exception {
        mockMvc.perform(get("/api/v1/code/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("backend")));
    }
}
