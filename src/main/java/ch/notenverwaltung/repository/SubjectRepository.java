package ch.notenverwaltung.repository;

import ch.notenverwaltung.model.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    boolean existsByName(String name);
}
