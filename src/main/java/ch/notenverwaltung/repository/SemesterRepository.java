package ch.notenverwaltung.repository;

import ch.notenverwaltung.model.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    boolean existsByName(String name);
}
