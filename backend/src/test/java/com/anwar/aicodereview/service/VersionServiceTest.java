package test.java.com.anwar.aicodereview.service;

import com.anwar.aicodereview.model.CodeSubmission;
import com.anwar.aicodereview.model.CodeVersion;
import com.anwar.aicodereview.repository.CodeSubmissionReporsitory;
import com.anwar.aicodereview.repository.CodeVersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private CodeVersonRepository versionRepository;

    @Mock
    private CodeSubmissionReporsitory submissionRepository;

    @Mock
    private AiAnalysisService aiAnalysisService;

    @InjectMocks
    private VersionService versionService;

    @Test
    void createNextVersionShouldIncrementVersionNumber() {
        UUID submissionId = UUID.randomUUID();
        CodeSubmission submission = new CodeSubmission();
        submission.setId(submissionId);

        CodeVersion latest = new CodeVersion();
        latest.setVersionNumber(2);

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(versionRepository.findTopBySubmissionIdOrderByVersionNumberDesc(submissionId)).thenReturn(Optional.of(latest));
        when(versionRepository.save(org.mockito.ArgumentMatchers.any(CodeVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeVersion created = versionService.createNextVersion(submissionId, "new code");

        assertEquals(3, created.getVersionNumber());
    }
}
