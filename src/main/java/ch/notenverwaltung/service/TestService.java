package ch.notenverwaltung.service;

import ch.notenverwaltung.model.dto.TestDTO;
import ch.notenverwaltung.model.entity.SchoolClass;
import ch.notenverwaltung.model.entity.SemesterSubject;
import ch.notenverwaltung.model.entity.TestEntity;
import ch.notenverwaltung.repository.SchoolClassRepository;
import ch.notenverwaltung.repository.SemesterSubjectRepository;
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
public class TestService {

    private final TestRepository testRepository;
    private final SemesterSubjectRepository semesterSubjectRepository;
    private final SchoolClassRepository schoolClassRepository;

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<TestDTO> getAll(org.springframework.data.domain.Pageable pageable) {
        return testRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public TestDTO getById(UUID id) {
        return testRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + id));
    }

    @Transactional
    public TestDTO create(TestDTO dto) {
        SemesterSubject ss = semesterSubjectRepository.findById(dto.getSemesterSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("SemesterSubject not found with id: " + dto.getSemesterSubjectId()));
        SchoolClass sc = schoolClassRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + dto.getClassId()));
        TestEntity entity = TestEntity.builder()
                .name(dto.getName())
                .semesterSubject(ss)
                .schoolClass(sc)
                .build();
        return toDTO(testRepository.save(entity));
    }

    @Transactional
    public TestDTO update(UUID id, TestDTO dto) {
        TestEntity entity = testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id: " + id));
        entity.setName(dto.getName());
        if (dto.getSemesterSubjectId() != null) {
            entity.setSemesterSubject(semesterSubjectRepository.findById(dto.getSemesterSubjectId())
                    .orElseThrow(() -> new EntityNotFoundException("SemesterSubject not found with id: " + dto.getSemesterSubjectId())));
        }
        if (dto.getClassId() != null) {
            entity.setSchoolClass(schoolClassRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + dto.getClassId())));
        }
        return toDTO(testRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!testRepository.existsById(id)) {
            throw new EntityNotFoundException("Test not found with id: " + id);
        }
        testRepository.deleteById(id);
    }

    private TestDTO toDTO(TestEntity t) {
        return TestDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .semesterSubjectId(t.getSemesterSubject().getId())
                .classId(t.getSchoolClass().getId())
                .build();
    }
}
