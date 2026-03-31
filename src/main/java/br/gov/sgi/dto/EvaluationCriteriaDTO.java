package br.gov.sgi.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class EvaluationCriteriaDTO {
    private String id;
    private String name;
    private BigDecimal weight;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String unit;
}
