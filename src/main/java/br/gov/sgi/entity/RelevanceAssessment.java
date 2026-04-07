package br.gov.sgi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "relevance_assessments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RelevanceAssessment {

    @EmbeddedId
    private RelevanceAssessmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("indicatorId")
    @JoinColumn(name = "indicator_id")
    private Indicator indicator;

    @Column(name = "score", nullable = false)
    private Integer score;
}
