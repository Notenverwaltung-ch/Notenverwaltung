package ch.notenverwaltung.repository;

import ch.notenverwaltung.model.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    org.springframework.data.domain.Page<Grade> findByStudent_Id(UUID studentId, org.springframework.data.domain.Pageable pageable);
}
