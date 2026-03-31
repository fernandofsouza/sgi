package br.gov.sgi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// ─── CheckIn ──────────────────────────────────────────────────────────────────
@Entity
@Table(name = "check_ins")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class CheckIn {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @Column(name = "progress", nullable = false)
    private Integer progress;

    @Column(name = "notes", columnDefinition = "NVARCHAR(MAX)")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private TeamMember author;

    @OneToMany(mappedBy = "checkIn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CheckInCriteriaUpdate> criteriaUpdates = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (id == null) id = UUID.randomUUID().toString();
    }
}

// ─── CheckInCriteriaUpdate ───────────────────────────────────────────────────
@Entity
@Table(name = "check_in_criteria_updates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class CheckInCriteriaUpdate {

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

// ─── EvaluationCriteria ──────────────────────────────────────────────────────
@Entity
@Table(name = "evaluation_criteria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class EvaluationCriteria {

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

// ─── RelevanceCriteria ───────────────────────────────────────────────────────
@Entity
@Table(name = "relevance_criteria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class RelevanceCriteria {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "criterion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelevanceScaleLabel> scaleLabels = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (id == null) id = UUID.randomUUID().toString();
    }
}

// ─── RelevanceScaleLabel ─────────────────────────────────────────────────────
@Entity
@Table(name = "relevance_scale_labels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class RelevanceScaleLabel {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private RelevanceCriteria criterion;

    @Column(name = "scale_value", nullable = false)
    private Integer scaleValue;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @PrePersist
    protected void onCreate() { if (id == null) id = UUID.randomUUID().toString(); }
}

// ─── RelevanceAssessment ─────────────────────────────────────────────────────
@Entity
@Table(name = "relevance_assessments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class RelevanceAssessment {

    @EmbeddedId
    private RelevanceAssessmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("indicatorId")
    @JoinColumn(name = "indicator_id")
    private Indicator indicator;

    @Column(name = "criterion_id", insertable = false, updatable = false, length = 50)
    private String criterionId;

    @Column(name = "score", nullable = false)
    private Integer score;
}

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class RelevanceAssessmentId implements java.io.Serializable {
    private String indicatorId;
    private String criterionId;
}

// ─── AchievementScaleLabel ───────────────────────────────────────────────────
@Entity
@Table(name = "achievement_scale_labels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class AchievementScaleLabel {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;

    @Column(name = "scale_value", nullable = false)
    private Integer scaleValue;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @PrePersist
    protected void onCreate() { if (id == null) id = UUID.randomUUID().toString(); }
}

// ─── AuditLog ────────────────────────────────────────────────────────────────
@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 50)
    private String entityId;

    @Column(name = "details", columnDefinition = "NVARCHAR(MAX)")
    private String details;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "user_name", length = 200)
    private String userName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
