package br.gov.sgi.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
