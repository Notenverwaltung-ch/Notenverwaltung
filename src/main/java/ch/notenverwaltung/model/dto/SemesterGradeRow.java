package ch.notenverwaltung.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemesterGradeRow {
    private UUID gradeId;
    private UUID studentId;
    private String studentFirstName;
    private String studentLastName;
    private UUID subjectId;
    private String subjectName;
    private UUID testId;
    private String testName;
    private LocalDate testDate;
    private BigDecimal value;
    private BigDecimal weight;
    private String gradeComment;
}
