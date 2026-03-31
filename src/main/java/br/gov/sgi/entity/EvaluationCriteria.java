package br.gov.sgi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "evaluation_criteria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EvaluationCriteria {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "target_value", nullable = false, precision = 18, scale = 4)
    private BigDecimal targetValue;

    @Column(name = "current_value", nullable = false, precision = 18, scale = 4)
    private BigDecimal currentValue = BigDecimal.ZERO;

    @Column(name = "unit", length = 50)
    private String unit;

    @PrePersist
    protected void onCreate() { if (id == null) id = UUID.randomUUID().toString(); }
}
