package ch.notenverwaltung.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private String studentNumber;
    private boolean active;
}