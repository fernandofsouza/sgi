package br.gov.sgi.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
