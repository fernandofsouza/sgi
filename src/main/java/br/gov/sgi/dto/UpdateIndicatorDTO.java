package br.gov.sgi.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateIndicatorDTO {
    private String title;
    private String description;
    private String pdgId;
    private String creationStatus;
    private String progressStatus;
    @Min(0) @Max(100) private Integer progress;
    private LocalDate targetDate;
    private String editorId;
    private String validatorId;
    private String observation;
    private Integer referenceYear;
    private String referenceRange;
    private String referenceLabel;
    private List<EvaluationCriteriaDTO> criteria;
    private List<AchievementScaleLabelDTO> achievementScale;
    private List<String> assigneeIds;
}
