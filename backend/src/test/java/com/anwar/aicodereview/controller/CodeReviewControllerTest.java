package com.anwar.aicodereview.controller;

import com.anwar.aicodereview.config.SecurityConfig;
import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.service.JwtService;
import com.anwar.aicodereview.service.CodeService;
import com.anwar.aicodereview.service.VersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;

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
@TestPropertySource(properties =
        "cors.allowed-origins=http://localhost:3000,https://ai-code-reviewer-monolithic-applica-gray.vercel.app")
class CodeReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CodeService codeService;

    @MockBean
    private VersionService versionService;

    @MockBean
    private JwtService jwtService;

    @Test
    void helloEndpointShouldReturnSuccessPayload() throws Exception {
        mockMvc.perform(get("/api/v1/code/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("Backend")));
    }

    @Test
    void invalidSubmissionIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/code/analyze/undefined")
                        .with(authenticatedAs(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void protectedEndpointsShouldRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/code/submissions/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void submissionsCannotBeReadUsingAnotherUsersId() throws Exception {
        mockMvc.perform(get("/api/v1/code/submissions/{userId}", UUID.randomUUID())
                        .with(authenticatedAs(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    void authenticatedUserCanRequestTheirPagedHistory() throws Exception {
        UUID userId = UUID.randomUUID();
        when(codeService.getUserSubmissions(org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any())).thenReturn(
                new org.springframework.data.domain.PageImpl<>(List.of(),
                        org.springframework.data.domain.PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/v1/code/submissions/me").with(authenticatedAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.meta.page").value(0))
                .andExpect(jsonPath("$.meta.size").value(20));
    }

    @Test
    void corsAllowsConfiguredOriginAndRejectsOtherOrigins() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .options("/api/v1/auth/register")
                        .header("Origin", "https://ai-code-reviewer-monolithic-applica-gray.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "https://ai-code-reviewer-monolithic-applica-gray.vercel.app"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .options("/api/v1/auth/login")
                        .header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void oversizedJsonRequestIsRejectedBeforeAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("x".repeat((int) com.anwar.aicodereview.config.RequestBodySizeFilter.MAX_REQUEST_BODY_BYTES + 1)))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error").value("PAYLOAD_TOO_LARGE"));
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

        UUID userId = UUID.randomUUID();
        when(versionService.getVersions(submissionId, userId)).thenReturn(List.of(version));

        mockMvc.perform(get("/api/v1/code/version/{submissionId}", submissionId)
                        .with(authenticatedAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].versionNumber").value(1))
                .andExpect(jsonPath("$.data[0].submission").doesNotExist())
                .andExpect(content().string(not(containsString("hibernateLazyInitializer"))));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor authenticatedAs(UUID userId) {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(userId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }
}
