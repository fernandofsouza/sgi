package br.gov.sgi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "achievement_scale_labels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AchievementScaleLabel {

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
