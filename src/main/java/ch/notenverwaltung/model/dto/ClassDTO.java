package ch.notenverwaltung.model.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDTO {
    private UUID id;
    private UUID semesterSubjectId;
    private String name;
}
