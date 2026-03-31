package br.gov.sgi.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
