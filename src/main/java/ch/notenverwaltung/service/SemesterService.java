package ch.notenverwaltung.service;

import ch.notenverwaltung.model.dto.SemesterDTO;
import ch.notenverwaltung.model.entity.Semester;
import ch.notenverwaltung.repository.SemesterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;

    @Transactional(readOnly = true)
    public List<SemesterDTO> getAll() {
        return semesterRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SemesterDTO> getAll(org.springframework.data.domain.Pageable pageable) {
        return semesterRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public SemesterDTO getById(UUID id) {
        return semesterRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with id: " + id));
    }

    @Transactional
    public SemesterDTO create(SemesterDTO dto) {
        if (semesterRepository.existsByName(dto.getName())) {
            throw new ch.notenverwaltung.exception.AlreadyExistsException("Semester with name '" + dto.getName() + "' already exists");
        }
        Semester entity = Semester.builder()
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
        return toDTO(semesterRepository.save(entity));
    }

    @Transactional
    public SemesterDTO update(UUID id, SemesterDTO dto) {
        Semester entity = semesterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with id: " + id));
        entity.setName(dto.getName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        return toDTO(semesterRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!semesterRepository.existsById(id)) {
            throw new EntityNotFoundException("Semester not found with id: " + id);
        }
        semesterRepository.deleteById(id);
    }

    private SemesterDTO toDTO(Semester s) {
        return SemesterDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .build();
    }
}
