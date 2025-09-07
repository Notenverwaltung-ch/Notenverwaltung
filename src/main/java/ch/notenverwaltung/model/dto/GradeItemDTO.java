package ch.notenverwaltung.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeItemDTO {
    private UUID gradeId;
    private UUID testId;
    private String testName;
    private LocalDate testDate;
    private BigDecimal value;
    private BigDecimal weight;
    private String gradeComment;
}
