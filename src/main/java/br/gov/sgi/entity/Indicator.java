package br.gov.sgi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "indicators")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Indicator {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "seq_id", insertable = false, updatable = false)
    private Integer seqId;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "pdg_id", length = 100)
    private String pdgId;

    @Column(name = "creation_status", nullable = false, length = 100)
    private String creationStatus;

    @Column(name = "progress_status", nullable = false, length = 100)
    private String progressStatus;

    @Column(name = "progress", nullable = false)
    private Integer progress = 0;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Indicator parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Indicator> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id")
    private TeamMember editor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validator_id")
    private TeamMember validator;

    @Column(name = "observation", columnDefinition = "NVARCHAR(MAX)")
    private String observation;

    @Column(name = "reference_year", nullable = false)
    private Integer referenceYear;

    @Column(name = "reference_range", nullable = false, length = 20)
    private String referenceRange;

    @Column(name = "reference_label", nullable = false, length = 10)
    private String referenceLabel;

    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationCriteria> criteria = new ArrayList<>();

    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AchievementScaleLabel> achievementScale = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "indicator_assignees",
        joinColumns = @JoinColumn(name = "indicator_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<TeamMember> assignees = new ArrayList<>();

    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("checkDate DESC")
    private List<CheckIn> checkIns = new ArrayList<>();

    @OneToMany(mappedBy = "indicator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RelevanceAssessment> relevanceAssessments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (id == null) id = UUID.randomUUID().toString();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
