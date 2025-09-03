package ch.notenverwaltung.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {
    private UUID id;
    private BigDecimal value;
    private BigDecimal weight;
    private UUID studentId;
    private UUID testId;
}
