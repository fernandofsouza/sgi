package br.gov.sgi.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class CriteriaUpdateDTO {
    private String criteriaId;
    private BigDecimal value;
}
