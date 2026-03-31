package br.gov.sgi.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RelevanceCriteriaDTO {
    private String id;
    private String name;
    private String description;
    private Boolean active;
    private List<ScaleLabelDTO> scaleLabels;
}
