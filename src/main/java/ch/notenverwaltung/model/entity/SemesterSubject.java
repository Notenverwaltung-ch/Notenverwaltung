package ch.notenverwaltung.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "semester_subjects", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"semester_id", "subject_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterSubject {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
