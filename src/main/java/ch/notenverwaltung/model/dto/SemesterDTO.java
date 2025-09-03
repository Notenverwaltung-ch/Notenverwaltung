package ch.notenverwaltung.model.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterDTO {
    private UUID id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
