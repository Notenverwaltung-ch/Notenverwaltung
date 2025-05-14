package ch.notenverwaltung.repository;

import ch.notenverwaltung.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByStudentNumber(String studentNumber);

    List<Student> findByActiveTrue();

    List<Student> findByLastName(String lastName);
}