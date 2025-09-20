package ch.notenverwaltung.service;

import ch.notenverwaltung.model.dto.GradeDTO;
import ch.notenverwaltung.model.dto.GradeItemDTO;
import ch.notenverwaltung.model.dto.SemesterGradeRow;
import ch.notenverwaltung.model.dto.StudentSemesterResultDTO;
import ch.notenverwaltung.model.dto.SubjectResultDTO;
import ch.notenverwaltung.model.entity.Grade;
import ch.notenverwaltung.model.entity.User;
import ch.notenverwaltung.model.entity.TestEntity;
import ch.notenverwaltung.repository.GradeRepository;
import ch.notenverwaltung.repository.UserRepository;
import ch.notenverwaltung.repository.TestRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;
    private final TestRepository testRepository;

    @Transactional(readOnly = true)
    public List<GradeDTO> getAll() {
        return gradeRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<GradeDTO> find(org.springframework.data.domain.Pageable pageable,
                                                              java.util.UUID studentId,
                                                              java.util.UUID testId,
                                                              java.math.BigDecimal valueMin,
                                                              java.math.BigDecimal valueMax,
                                                              boolean isAdmin,
                                                              String username) {
        org.springframework.data.domain.Page<Grade> page;
        if (studentId != null) {
            page = gradeRepository.findByStudent_Id(studentId, pageable);
        } else if (!isAdmin) {
            java.util.UUID currentUserId = userRepository.findByUsername(username)
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: " + username))
                    .getId();
            page = gradeRepository.findByStudent_Id(currentUserId, pageable);
        } else {
            page = gradeRepository.findAll(pageable);
        }
        java.util.List<GradeDTO> content = page.getContent().stream()
                .filter(g -> testId == null || (g.getTest() != null && g.getTest().getId().equals(testId)))
                .filter(g -> valueMin == null || g.getValue().compareTo(valueMin) >= 0)
                .filter(g -> valueMax == null || g.getValue().compareTo(valueMax) <= 0)
                .map(this::toDTO)
                .collect(java.util.stream.Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<StudentSemesterResultDTO> getSemesterGrades(UUID semesterId, UUID studentId) {
        List<SemesterGradeRow> rows = gradeRepository.findSemesterGrades(semesterId, studentId);
        // Group by student
        Map<UUID, List<SemesterGradeRow>> byStudent = rows.stream()
                .collect(Collectors.groupingBy(SemesterGradeRow::getStudentId, LinkedHashMap::new, Collectors.toList()));

        List<StudentSemesterResultDTO> result = new ArrayList<>();
        for (Map.Entry<UUID, List<SemesterGradeRow>> studentEntry : byStudent.entrySet()) {
            UUID sId = studentEntry.getKey();
            List<SemesterGradeRow> sRows = studentEntry.getValue();
            String firstName = sRows.stream().map(SemesterGradeRow::getStudentFirstName).filter(Objects::nonNull).findFirst().orElse(null);
            String lastName = sRows.stream().map(SemesterGradeRow::getStudentLastName).filter(Objects::nonNull).findFirst().orElse(null);

            // Group by subject
            Map<UUID, List<SemesterGradeRow>> bySubject = sRows.stream()
                    .collect(Collectors.groupingBy(SemesterGradeRow::getSubjectId, LinkedHashMap::new, Collectors.toList()));

            List<SubjectResultDTO> subjects = new ArrayList<>();
            BigDecimal totalWeightedSum = BigDecimal.ZERO;
            BigDecimal totalWeight = BigDecimal.ZERO;

            for (Map.Entry<UUID, List<SemesterGradeRow>> subjEntry : bySubject.entrySet()) {
                UUID subjId = subjEntry.getKey();
                List<SemesterGradeRow> subjRows = subjEntry.getValue();
                String subjectName = subjRows.stream().map(SemesterGradeRow::getSubjectName).filter(Objects::nonNull).findFirst().orElse(null);

                List<GradeItemDTO> gradeItems = subjRows.stream().map(r ->
                        GradeItemDTO.builder()
                                .gradeId(r.getGradeId())
                                .testId(r.getTestId())
                                .testName(r.getTestName())
                                .testDate(r.getTestDate())
                                .value(r.getValue())
                                .weight(r.getWeight())
                                .gradeComment(r.getGradeComment())
                                .build()
                ).collect(Collectors.toList());

                BigDecimal subjectWeightedSum = subjRows.stream()
                        .map(r -> safe(r.getValue()).multiply(safe(r.getWeight())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal subjectWeight = subjRows.stream()
                        .map(r -> safe(r.getWeight()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal subjectAvg = subjectWeight.compareTo(BigDecimal.ZERO) == 0 ? null : subjectWeightedSum.divide(subjectWeight, 2, RoundingMode.HALF_UP);

                if (subjectWeight.compareTo(BigDecimal.ZERO) > 0) {
                    totalWeightedSum = totalWeightedSum.add(subjectWeightedSum);
                    totalWeight = totalWeight.add(subjectWeight);
                }

                subjects.add(SubjectResultDTO.builder()
                        .subjectId(subjId)
                        .subjectName(subjectName)
                        .calculatedGrade(subjectAvg)
                        .grades(gradeItems)
                        .build());
            }

            BigDecimal overall = totalWeight.compareTo(BigDecimal.ZERO) == 0 ? null : totalWeightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP);

            StudentSemesterResultDTO dto = StudentSemesterResultDTO.builder()
                    .studentId(sId)
                    .studentFirstName(firstName)
                    .studentLastName(lastName)
                    .overallGrade(overall)
                    .subjects(subjects)
                    .build();
            result.add(dto);
        }
        return result;
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @Transactional(readOnly = true)
    public GradeDTO getById(UUID id) {
        return gradeRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found with id: " + id));
    }

    @Transactional
    public GradeDTO create(GradeDTO dto) {
        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.getStudentId()));
        TestEntity test = null;
        if (dto.getTestId() != null) {
            test = testRepository.findById(dto.getTestId())
                    .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + dto.getTestId()));
        }
        Grade entity = Grade.builder()
                .value(dto.getValue())
                .weight(dto.getWeight())
                .comment(dto.getComment())
                .student(student)
                .test(test)
                .build();
        return toDTO(gradeRepository.save(entity));
    }

    @Transactional
    public GradeDTO update(UUID id, GradeDTO dto) {
        Grade entity = gradeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found with id: " + id));
        entity.setValue(dto.getValue());
        entity.setWeight(dto.getWeight());
        entity.setComment(dto.getComment());
        if (dto.getStudentId() != null) {
            entity.setStudent(userRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.getStudentId())));
        }
        if (dto.getTestId() != null) {
            entity.setTest(testRepository.findById(dto.getTestId())
                    .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + dto.getTestId())));
        }
        return toDTO(gradeRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!gradeRepository.existsById(id)) {
            throw new EntityNotFoundException("Grade not found with id: " + id);
        }
        gradeRepository.deleteById(id);
    }

    private GradeDTO toDTO(Grade g) {
        return GradeDTO.builder()
                .id(g.getId())
                .value(g.getValue())
                .weight(g.getWeight())
                .comment(g.getComment())
                .studentId(g.getStudent().getId())
                .testId(g.getTest() != null ? g.getTest().getId() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ch.notenverwaltung.model.dto.GradeViewDTO> findView(
            org.springframework.data.domain.Pageable pageable,
            String studentUsername,
            String testName,
            java.math.BigDecimal valueMin,
            java.math.BigDecimal valueMax,
            boolean isAdmin,
            String currentUsername
    ) {
        org.springframework.data.domain.Page<Grade> page;
        if (!isAdmin) {
            java.util.UUID currentUserId = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: " + currentUsername))
                    .getId();
            page = gradeRepository.findByStudent_Id(currentUserId, pageable);
        } else {
            page = gradeRepository.findAll(pageable);
        }
        java.util.List<ch.notenverwaltung.model.dto.GradeViewDTO> content = page.getContent().stream()
                .filter(g -> studentUsername == null || studentUsername.isBlank() || (g.getStudent() != null && g.getStudent().getUsername() != null && g.getStudent().getUsername().toLowerCase().contains(studentUsername.toLowerCase())))
                .filter(g -> testName == null || testName.isBlank() || (g.getTest() != null && g.getTest().getName() != null && g.getTest().getName().toLowerCase().contains(testName.toLowerCase())))
                .filter(g -> valueMin == null || g.getValue().compareTo(valueMin) >= 0)
                .filter(g -> valueMax == null || g.getValue().compareTo(valueMax) <= 0)
                .map(this::toViewDTO)
                .collect(java.util.stream.Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(content, pageable, page.getTotalElements());
    }

    private ch.notenverwaltung.model.dto.GradeViewDTO toViewDTO(Grade g) {
        return ch.notenverwaltung.model.dto.GradeViewDTO.builder()
                .value(g.getValue())
                .weight(g.getWeight())
                .comment(g.getComment())
                .studentUsername(g.getStudent() != null ? g.getStudent().getUsername() : null)
                .testName(g.getTest() != null ? g.getTest().getName() : null)
                .build();
    }
}
