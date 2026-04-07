package br.gov.sgi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemConfig {

    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT", nullable = false)
    private String configValue;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
