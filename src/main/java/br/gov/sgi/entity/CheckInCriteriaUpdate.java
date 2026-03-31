package br.gov.sgi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "check_in_criteria_updates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckInCriteriaUpdate {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckIn checkIn;

    @Column(name = "criteria_id", nullable = false, length = 50)
    private String criteriaId;

    @Column(name = "value", nullable = false, precision = 18, scale = 4)
    private BigDecimal value;

    @PrePersist
    protected void onCreate() { if (id == null) id = UUID.randomUUID().toString(); }
}
