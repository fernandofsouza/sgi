package br.gov.sgi.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateCheckInDTO {
    @NotNull private LocalDate checkDate;
    @NotNull @Min(0) @Max(100) private Integer progress;
    private String notes;
    @NotBlank private String authorId;
    private List<CriteriaUpdateDTO> criteriaUpdates;
}
