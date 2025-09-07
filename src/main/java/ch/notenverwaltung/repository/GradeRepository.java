package ch.notenverwaltung.repository;

import ch.notenverwaltung.model.dto.SemesterGradeRow;
import ch.notenverwaltung.model.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    org.springframework.data.domain.Page<Grade> findByStudent_Id(UUID studentId, org.springframework.data.domain.Pageable pageable);

    @Query("select new ch.notenverwaltung.model.dto.SemesterGradeRow(" +
            " g.id, u.id, u.firstName, u.lastName, subj.id, subj.name, t.id, t.name, t.date, g.value, g.weight, g.comment) " +
            " from Grade g " +
            " join g.student u " +
            " left join g.test t " +
            " left join t.semesterSubject ss " +
            " left join ss.subject subj " +
            " left join ss.semester sem " +
            " where sem.id = :semesterId " +
            " and ( u.id = COALESCE(:studentId, u.id) )")
    List<SemesterGradeRow> findSemesterGrades(@Param("semesterId") UUID semesterId,
                                              @Param("studentId") UUID studentId);
}
