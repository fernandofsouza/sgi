package br.gov.sgi.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ─── Indicator Summary (listagens) ───────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class IndicatorSummaryDTO {
    private String id;
    private Integer seqId;
    private String title;
    private String pdgId;
    private String creationStatus;
    private String progressStatus;
    private Integer progress;
    private LocalDate targetDate;
    private String parentId;
    private Integer referenceYear;
    private String referenceRange;
    private String referenceLabel;
    private List<TeamMemberDTO> assignees;
    private Integer checkInCount;
    private LocalDateTime updatedAt;
}

// ─── Indicator Detail (detalhe completo) ─────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class IndicatorDetailDTO {
    private String id;
    private Integer seqId;
    private String title;
    private String description;
    private String pdgId;
    private String creationStatus;
    private String progressStatus;
    private Integer progress;
    private LocalDate targetDate;
    private String parentId;
    private List<String> childrenIds;
    private TeamMemberDTO editor;
    private TeamMemberDTO validator;
    private String observation;
    private Integer referenceYear;
    private String referenceRange;
    private String referenceLabel;
    private List<EvaluationCriteriaDTO> criteria;
    private List<AchievementScaleLabelDTO> achievementScale;
    private List<TeamMemberDTO> assignees;
    private List<CheckInDTO> checkIns;
    private List<RelevanceAssessmentDTO> relevanceAssessments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// ─── Create / Update Indicator ───────────────────────────────────────────────
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

// ─── CheckIn ─────────────────────────────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CheckInDTO {
    private String id;
    private String indicatorId;
    private LocalDate checkDate;
    private Integer progress;
    private String notes;
    private TeamMemberDTO author;
    private List<CriteriaUpdateDTO> criteriaUpdates;
    private LocalDateTime createdAt;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateCheckInDTO {
    @NotNull private LocalDate checkDate;
    @NotNull @Min(0) @Max(100) private Integer progress;
    private String notes;
    @NotBlank private String authorId;
    private List<CriteriaUpdateDTO> criteriaUpdates;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class CriteriaUpdateDTO {
    private String criteriaId;
    private BigDecimal value;
}

// ─── Shared DTOs ─────────────────────────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TeamMemberDTO {
    private String id;
    private String name;
    private String role;
    private String email;
    private String avatarUrl;
    private Boolean active;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class EvaluationCriteriaDTO {
    private String id;
    private String name;
    private BigDecimal weight;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String unit;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class AchievementScaleLabelDTO {
    private Integer value;
    private String label;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class RelevanceAssessmentDTO {
    private String criterionId;
    @Min(1) @Max(5) private Integer score;
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RelevanceCriteriaDTO {
    private String id;
    private String name;
    private String description;
    private Boolean active;
    private List<ScaleLabelDTO> scaleLabels;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class ScaleLabelDTO {
    @Min(1) @Max(5) private Integer value;
    private String label;
}

@Data @NoArgsConstructor @AllArgsConstructor
public class AuditLogDTO {
    private String action;
    private String entityType;
    private String entityId;
    private Object details;
    private String userId;
    private String userName;
}
