package ch.notenverwaltung.service;

import ch.notenverwaltung.model.dto.GradeDTO;
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

import java.util.List;
import java.util.UUID;
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
    public GradeDTO getById(UUID id) {
        return gradeRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found with id: " + id));
    }

    @Transactional
    public GradeDTO create(GradeDTO dto) {
        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.getStudentId()));
        TestEntity test = testRepository.findById(dto.getTestId())
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + dto.getTestId()));
        Grade entity = Grade.builder()
                .value(dto.getValue())
                .weight(dto.getWeight())
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
                .studentId(g.getStudent().getId())
                .testId(g.getTest().getId())
                .build();
    }
}
