package com.anwar.aicodereview.controller;

import com.anwar.aicodereview.config.SecurityConfig;
import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.service.CodeService;
import com.anwar.aicodereview.service.VersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CodeReviewController.class)
@Import(SecurityConfig.class)
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
                .andExpect(jsonPath("$.message").value(containsString("Backend")));
    }

    @Test
    void invalidSubmissionIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/code/analyze/undefined"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void versionResponseShouldNotSerializeSubmissionEntity() throws Exception {
        UUID submissionId = UUID.randomUUID();

        CodeSubmission submission = new CodeSubmission();
        submission.setId(submissionId);
        submission.setUserId(UUID.randomUUID());
        submission.setFilename("Example.java");
        submission.setLanguage("java");
        submission.setCode("class Example {}");

        CodeVersion version = new CodeVersion();
        version.setId(UUID.randomUUID());
        version.setSubmission(submission);
        version.setVersionNumber(1);
        version.setCode("class Example {}");
        version.setAnalysis("Pending analysis...");

        when(versionService.getVersions(submissionId)).thenReturn(List.of(version));

        mockMvc.perform(get("/api/v1/code/version/{submissionId}", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].versionNumber").value(1))
                .andExpect(jsonPath("$.data[0].submission").doesNotExist())
                .andExpect(content().string(not(containsString("hibernateLazyInitializer"))));
    }
}
