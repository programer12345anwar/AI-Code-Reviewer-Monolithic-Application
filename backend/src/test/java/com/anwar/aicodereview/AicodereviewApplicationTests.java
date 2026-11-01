package com.anwar.aicodereview;

import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.repository.CodeSubmissionReporsitory;
import com.anwar.aicodereview.repository.CodeVersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AicodereviewApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CodeSubmissionReporsitory submissionRepository;

    @Autowired
    private CodeVersonRepository versionRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void actuatorHealthIsAvailableWithoutExposingDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void submissionsAndVersionsPersistWithGeneratedUuidsAndRelationship() {
        CodeSubmission submission = new CodeSubmission();
        submission.setUserId(UUID.randomUUID());
        submission.setFilename("Example.java");
        submission.setLanguage("java");
        submission.setCode("class Example {}");
        CodeSubmission savedSubmission = submissionRepository.save(submission);

        CodeVersion version = new CodeVersion();
        version.setSubmission(savedSubmission);
        version.setVersionNumber(1);
        version.setCode(savedSubmission.getCode());
        CodeVersion savedVersion = versionRepository.save(version);

        assertNotNull(savedSubmission.getId());
        assertNotNull(savedVersion.getId());
        assertEquals(
                savedSubmission.getId(),
                versionRepository.findById(savedVersion.getId()).orElseThrow()
                        .getSubmission().getId());
    }
}
