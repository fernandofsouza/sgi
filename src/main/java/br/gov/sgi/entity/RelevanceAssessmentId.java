package br.gov.sgi.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RelevanceAssessmentId implements java.io.Serializable {
    private String indicatorId;
    private String criterionId;
}
