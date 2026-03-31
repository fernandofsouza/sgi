package br.gov.sgi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "check_ins")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckIn {

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

    @Column(name = "notes", columnDefinition = "TEXT")
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
