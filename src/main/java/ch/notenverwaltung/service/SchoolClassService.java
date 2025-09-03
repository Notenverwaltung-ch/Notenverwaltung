package ch.notenverwaltung.service;

import ch.notenverwaltung.model.dto.ClassDTO;
import ch.notenverwaltung.model.entity.SchoolClass;
import ch.notenverwaltung.model.entity.SemesterSubject;
import ch.notenverwaltung.repository.SchoolClassRepository;
import ch.notenverwaltung.repository.SemesterSubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final SemesterSubjectRepository semesterSubjectRepository;

    @Transactional(readOnly = true)
    public List<ClassDTO> getAll() {
        return schoolClassRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ClassDTO> getAll(org.springframework.data.domain.Pageable pageable) {
        return schoolClassRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ClassDTO getById(UUID id) {
        return schoolClassRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + id));
    }

    @Transactional
    public ClassDTO create(ClassDTO dto) {
        SemesterSubject ss = semesterSubjectRepository.findById(dto.getSemesterSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("SemesterSubject not found with id: " + dto.getSemesterSubjectId()));
        SchoolClass entity = SchoolClass.builder().semesterSubject(ss).build();
        return toDTO(schoolClassRepository.save(entity));
    }

    @Transactional
    public ClassDTO update(UUID id, ClassDTO dto) {
        SchoolClass entity = schoolClassRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + id));
        SemesterSubject ss = semesterSubjectRepository.findById(dto.getSemesterSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("SemesterSubject not found with id: " + dto.getSemesterSubjectId()));
        entity.setSemesterSubject(ss);
        return toDTO(schoolClassRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!schoolClassRepository.existsById(id)) {
            throw new EntityNotFoundException("Class not found with id: " + id);
        }
        schoolClassRepository.deleteById(id);
    }

    private ClassDTO toDTO(SchoolClass c) {
        return ClassDTO.builder()
                .id(c.getId())
                .semesterSubjectId(c.getSemesterSubject().getId())
                .build();
    }
}
