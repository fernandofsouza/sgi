package br.gov.sgi.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class AuditLogDTO {
    private String action;
    private String entityType;
    private String entityId;
    private Object details;
    private String userId;
    private String userName;
}
