package ch.notenverwaltung.service;

import ch.notenverwaltung.model.dto.SubjectDTO;
import ch.notenverwaltung.model.entity.Subject;
import ch.notenverwaltung.repository.SubjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<SubjectDTO> getAll() {
        return subjectRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SubjectDTO> getAll(org.springframework.data.domain.Pageable pageable) {
        return subjectRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public SubjectDTO getById(UUID id) {
        return subjectRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id: " + id));
    }

    @Transactional
    public SubjectDTO create(SubjectDTO dto) {
        if (subjectRepository.existsByName(dto.getName())) {
            throw new ch.notenverwaltung.exception.AlreadyExistsException("Subject with name '" + dto.getName() + "' already exists");
        }
        Subject entity = Subject.builder().name(dto.getName()).build();
        return toDTO(subjectRepository.save(entity));
    }

    @Transactional
    public SubjectDTO update(UUID id, SubjectDTO dto) {
        Subject entity = subjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id: " + id));
        entity.setName(dto.getName());
        return toDTO(subjectRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!subjectRepository.existsById(id)) {
            throw new EntityNotFoundException("Subject not found with id: " + id);
        }
        try {
            subjectRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Re-throw to be handled by ApiExceptionHandler as 409 Conflict
            throw ex;
        }
    }

    private SubjectDTO toDTO(Subject subject) {
        return SubjectDTO.builder().id(subject.getId()).name(subject.getName()).build();
    }
}
