package br.gov.sgi.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ScaleLabelDTO {
    @Min(1) @Max(5) private Integer value;
    private String label;
}
