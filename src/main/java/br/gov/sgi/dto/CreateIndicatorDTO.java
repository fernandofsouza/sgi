package br.gov.sgi.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateIndicatorDTO {
    @NotBlank private String title;
    private String description;
    private String pdgId;
    private String creationStatus;
    private String progressStatus;
    @Min(0) @Max(100) private Integer progress = 0;
    private LocalDate targetDate;
    private String parentId;
    private String editorId;
    private String validatorId;
    private String observation;
    @NotNull private Integer referenceYear;
    @NotBlank private String referenceRange;
    @NotBlank private String referenceLabel;
    private List<EvaluationCriteriaDTO> criteria;
    private List<AchievementScaleLabelDTO> achievementScale;
    private List<String> assigneeIds;
    private List<RelevanceAssessmentDTO> relevanceAssessments;
}
