package ch.notenverwaltung.repository;

import ch.notenverwaltung.model.entity.SemesterSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SemesterSubjectRepository extends JpaRepository<SemesterSubject, UUID> {
}
