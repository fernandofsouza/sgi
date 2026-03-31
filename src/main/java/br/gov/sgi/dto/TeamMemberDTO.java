package br.gov.sgi.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TeamMemberDTO {
    private String id;
    private String name;
    private String role;
    private String email;
    private String avatarUrl;
    private Boolean active;
}
