package ch.notenverwaltung.model.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterSubjectDTO {
    private UUID id;
    private UUID semesterId;
    private UUID subjectId;
}
