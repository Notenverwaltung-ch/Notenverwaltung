package ch.notenverwaltung.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeViewDTO {
    private UUID id;
    private BigDecimal value;
    private BigDecimal weight;
    private String comment;
    private String studentUsername;
    private String testName;
    private LocalDateTime createdOn;
}
