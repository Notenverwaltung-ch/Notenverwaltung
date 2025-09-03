package ch.notenverwaltung.service;

import ch.notenverwaltung.model.dto.StudentDTO;
import ch.notenverwaltung.model.entity.Student;
import ch.notenverwaltung.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getActiveStudents() {
        return studentRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentDTO getStudentById(UUID id) {
        return studentRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));
    }

    @Transactional
    public StudentDTO createStudent(StudentDTO studentDTO) {
        Student student = mapToEntity(studentDTO);
        student.setId(null);
        student.setActive(true);
        Student savedStudent = studentRepository.save(student);
        return mapToDTO(savedStudent);
    }

    @Transactional
    public StudentDTO updateStudent(UUID id, StudentDTO studentDTO) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));
        
        existingStudent.setFirstName(studentDTO.getFirstName());
        existingStudent.setLastName(studentDTO.getLastName());
        existingStudent.setEmail(studentDTO.getEmail());
        existingStudent.setDateOfBirth(studentDTO.getDateOfBirth());
        existingStudent.setStudentNumber(studentDTO.getStudentNumber());
        existingStudent.setActive(studentDTO.isActive());
        
        Student updatedStudent = studentRepository.save(existingStudent);
        return mapToDTO(updatedStudent);
    }

    @Transactional
    public void deleteStudent(UUID id) {
        if (!studentRepository.existsById(id)) {
            throw new EntityNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    private StudentDTO mapToDTO(Student student) {
        return StudentDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .dateOfBirth(student.getDateOfBirth())
                .studentNumber(student.getStudentNumber())
                .active(student.isActive())
                .build();
    }

    private Student mapToEntity(StudentDTO studentDTO) {
        return Student.builder()
                .id(studentDTO.getId())
                .firstName(studentDTO.getFirstName())
                .lastName(studentDTO.getLastName())
                .email(studentDTO.getEmail())
                .dateOfBirth(studentDTO.getDateOfBirth())
                .studentNumber(studentDTO.getStudentNumber())
                .active(studentDTO.isActive())
                .build();
    }
}