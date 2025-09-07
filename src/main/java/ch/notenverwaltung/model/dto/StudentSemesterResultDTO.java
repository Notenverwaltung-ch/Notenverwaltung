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
public class StudentSemesterResultDTO {
    private UUID studentId;
    private String studentFirstName;
    private String studentLastName;
    private BigDecimal overallGrade; // weighted average across all subjects/grades
    @Builder.Default
    private List<SubjectResultDTO> subjects = new ArrayList<>();
}
