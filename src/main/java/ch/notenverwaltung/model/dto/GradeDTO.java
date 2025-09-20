package ch.notenverwaltung.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {
    private UUID id;
    private BigDecimal value;
    private BigDecimal weight;
    private String comment;
    private UUID studentId;
    private UUID testId;
    private LocalDateTime createdOn;
}
