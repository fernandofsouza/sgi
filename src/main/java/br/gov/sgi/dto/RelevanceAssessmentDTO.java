package br.gov.sgi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class RelevanceAssessmentDTO {
    private String criterionId;
    @Min(1) @Max(5) private Integer score;
}
