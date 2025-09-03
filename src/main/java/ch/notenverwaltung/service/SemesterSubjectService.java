package ch.notenverwaltung.service;

import ch.notenverwaltung.model.dto.SemesterSubjectDTO;
import ch.notenverwaltung.model.entity.Semester;
import ch.notenverwaltung.model.entity.SemesterSubject;
import ch.notenverwaltung.model.entity.Subject;
import ch.notenverwaltung.repository.SemesterRepository;
import ch.notenverwaltung.repository.SemesterSubjectRepository;
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
public class SemesterSubjectService {

    private final SemesterSubjectRepository semesterSubjectRepository;
    private final SemesterRepository semesterRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<SemesterSubjectDTO> getAll() {
        return semesterSubjectRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SemesterSubjectDTO getById(UUID id) {
        return semesterSubjectRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("SemesterSubject not found with id: " + id));
    }

    @Transactional
    public SemesterSubjectDTO create(SemesterSubjectDTO dto) {
        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with id: " + dto.getSemesterId()));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id: " + dto.getSubjectId()));
        SemesterSubject entity = SemesterSubject.builder().semester(semester).subject(subject).build();
        return toDTO(semesterSubjectRepository.save(entity));
    }

    @Transactional
    public SemesterSubjectDTO update(UUID id, SemesterSubjectDTO dto) {
        SemesterSubject entity = semesterSubjectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SemesterSubject not found with id: " + id));
        Semester semester = semesterRepository.findById(dto.getSemesterId())
                .orElseThrow(() -> new EntityNotFoundException("Semester not found with id: " + dto.getSemesterId()));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found with id: " + dto.getSubjectId()));
        entity.setSemester(semester);
        entity.setSubject(subject);
        return toDTO(semesterSubjectRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!semesterSubjectRepository.existsById(id)) {
            throw new EntityNotFoundException("SemesterSubject not found with id: " + id);
        }
        semesterSubjectRepository.deleteById(id);
    }

    private SemesterSubjectDTO toDTO(SemesterSubject ss) {
        return SemesterSubjectDTO.builder()
                .id(ss.getId())
                .semesterId(ss.getSemester().getId())
                .subjectId(ss.getSubject().getId())
                .build();
    }
}
