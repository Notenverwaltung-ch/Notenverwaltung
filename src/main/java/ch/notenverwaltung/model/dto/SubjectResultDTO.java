package ch.notenverwaltung.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResultDTO {
    private UUID subjectId;
    private String subjectName;
    private BigDecimal calculatedGrade; // weighted average for this subject
    @Builder.Default
    private List<GradeItemDTO> grades = new ArrayList<>();
}
