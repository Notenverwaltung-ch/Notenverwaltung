package ch.notenverwaltung.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDTO {
    private UUID id;
    private String name;
    private String comment; // optional
    private LocalDate date;
    private UUID semesterSubjectId;
    private UUID classId;
}
