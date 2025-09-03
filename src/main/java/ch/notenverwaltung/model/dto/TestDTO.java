package ch.notenverwaltung.model.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDTO {
    private UUID id;
    private String name;
    private UUID semesterSubjectId;
    private UUID classId;
}
