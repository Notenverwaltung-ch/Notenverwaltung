package ch.notenverwaltung.controller;

import ch.notenverwaltung.model.entity.*;
import ch.notenverwaltung.repository.*;
import ch.notenverwaltung.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/testdata")
@RequiredArgsConstructor
@Tag(name = "Testdata", description = "Seed database with demo data")
public class TestdataController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final SemesterSubjectRepository semesterSubjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final TestRepository testRepository;
    private final GradeRepository gradeRepository;

    @PostMapping("/setup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create demo data: admin, users, subjects, semesters, classes, tests, grades")
    @Transactional
    public ResponseEntity<Map<String, Object>> setup() {
        Map<String, Object> summary = new LinkedHashMap<>();

        // 1) Admin user
        String adminUsername = "admin";
        String adminPassword = "i10sLkbtf0CtZzO7";
        User admin;
        if (userService.existsByUsername(adminUsername)) {
            admin = userRepository.findByUsername(adminUsername).orElseThrow();
        } else {
            admin = userService.createUser(adminUsername, adminPassword, Collections.singletonList("ROLE_ADMIN"));
            admin.setActive(true);
            admin = userRepository.save(admin);
        }
        summary.put("adminUser", admin.getUsername());

        // 2) 10 users with ROLE_USER (+ profile fields)
        List<User> students = new ArrayList<>();
        String[] firstNames = {"Liam","Emma","Noah","Olivia","Ava","Mia","Sophia","Lucas","Amelia","Ethan"};
        String[] lastNames = {"Müller","Meier","Schneider","Fischer","Weber","Huber","Keller","Wagner","Steiner","Koch"};
        Random rnd = new Random();
        for (int i = 1; i <= 10; i++) {
            String uname = "user" + i;
            User u = userRepository.findByUsername(uname)
                    .orElseGet(() -> userService.createUser(uname, "password", Collections.singletonList("ROLE_USER")));
            if (!Boolean.TRUE.equals(u.getActive())) {
                u.setActive(true);
            }
            // set some profile info (names, email, date of birth)
            String fn = firstNames[(i - 1) % firstNames.length];
            String ln = lastNames[(i - 1) % lastNames.length];
            u.setFirstName(fn);
            u.setLastName(ln);
            u.setEmail(uname + "@example.com");
            // random DoB between 2002 and 2008
            int year = 2002 + rnd.nextInt(7);
            int month = 1 + rnd.nextInt(12);
            int day = 1 + Math.min(28, rnd.nextInt(28) + 1);
            u.setDateOfBirth(LocalDate.of(year, month, day));
            u = userRepository.save(u);
            students.add(u);
        }
        summary.put("userCount", students.size());

        // 3) 4 different subjects
        String[] subjectNames = {"Mathematics", "Physics", "Chemistry", "Biology"};
        List<Subject> subjects = new ArrayList<>();
        for (String sname : subjectNames) {
            Subject s = subjectRepository.existsByName(sname)
                    ? subjectRepository.findAll().stream().filter(x -> x.getName().equals(sname)).findFirst().orElse(null)
                    : subjectRepository.save(Subject.builder().name(sname).build());
            if (s != null) subjects.add(s);
        }
        summary.put("subjects", subjects.stream().map(Subject::getName).toList());

        // 4) 4 different semesters (with dates)
        // Use four sequential semesters
        List<Semester> semesters = new ArrayList<>();
        LocalDate base = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        for (int i = 1; i <= 4; i++) {
            String semName = "SEM-" + i + "-" + base.getYear();
            LocalDate start = base.plusMonths((i - 1) * 3L);
            LocalDate end = start.plusMonths(3).minusDays(1);
            Semester sem;
            if (semesterRepository.existsByName(semName)) {
                sem = semesterRepository.findAll().stream().filter(x -> x.getName().equals(semName)).findFirst().orElse(null);
            } else {
                sem = semesterRepository.save(Semester.builder().name(semName).startDate(start).endDate(end).build());
            }
            if (sem != null) semesters.add(sem);
        }
        summary.put("semesters", semesters.stream().map(Semester::getName).toList());

        // 5) A class for every semester and subject
        List<SemesterSubject> semesterSubjects = new ArrayList<>();
        for (Semester sem : semesters) {
            for (Subject sub : subjects) {
                SemesterSubject ss;
                boolean exists = semesterSubjectRepository.existsBySemester_IdAndSubject_Id(sem.getId(), sub.getId());
                if (exists) {
                    ss = semesterSubjectRepository.findAll().stream()
                            .filter(x -> x.getSemester().getId().equals(sem.getId()) && x.getSubject().getId().equals(sub.getId()))
                            .findFirst().orElse(null);
                } else {
                    ss = semesterSubjectRepository.save(SemesterSubject.builder().semester(sem).subject(sub).build());
                }
                if (ss != null) semesterSubjects.add(ss);
            }
        }
        summary.put("semesterSubjectsCount", semesterSubjects.size()); // should be 16

        List<SchoolClass> classes = new ArrayList<>();
        for (SemesterSubject ss : semesterSubjects) {
            Optional<SchoolClass> maybeExisting = schoolClassRepository.findAll().stream()
                    .filter(c -> c.getSemesterSubject().getId().equals(ss.getId()))
                    .findFirst();
            SchoolClass sc = maybeExisting.orElseGet(() -> schoolClassRepository.save(SchoolClass.builder().semesterSubject(ss).build()));
            classes.add(sc);
        }
        summary.put("classesCount", classes.size());

        // 6) Two tests per created class
        List<TestEntity> tests = new ArrayList<>();
        for (SchoolClass sc : classes) {
            List<TestEntity> existing = testRepository.findAll().stream()
                    .filter(t -> t.getSchoolClass().getId().equals(sc.getId()))
                    .toList();
            int toCreate = Math.max(0, 2 - existing.size());
            tests.addAll(existing);
            for (int i = 1; i <= toCreate; i++) {
                // pick a random date within the semester of this class
                Semester sem = sc.getSemesterSubject().getSemester();
                LocalDate start = sem.getStartDate();
                LocalDate end = sem.getEndDate();
                long days = end.toEpochDay() - start.toEpochDay();
                long offset = days > 0 ? Math.max(0, new Random().nextLong(days + 1)) : 0;
                LocalDate testDate = start.plusDays(offset);

                TestEntity t = TestEntity.builder()
                        .name("Test " + (existing.size() + i))
                        .semesterSubject(sc.getSemesterSubject())
                        .schoolClass(sc)
                        .date(testDate)
                        .build();
                tests.add(testRepository.save(t));
            }
        }
        summary.put("testsCount", tests.size());

        // 7) A grade for all 10 users for all the tests
        int gradesCreated = 0;
        for (TestEntity t : tests) {
            for (User s : students) {
                // Avoid duplicate grades for same student and test by checking in-memory (since no unique constraint)
                boolean exists = gradeRepository.findAll().stream()
                        .anyMatch(g -> g.getStudent().getId().equals(s.getId()) && g.getTest() != null && g.getTest().getId().equals(t.getId()));
                if (!exists) {
                    Grade g = Grade.builder()
                            .student(s)
                            .test(t)
                            .value(BigDecimal.valueOf(randomGrade1to6Biased(rnd)))
                            .weight(BigDecimal.valueOf(1.0))
                            .build();
                    gradeRepository.save(g);
                    gradesCreated++;
                }
            }
        }
        summary.put("gradesCreatedForUsers", gradesCreated);

        // 8) Separate USER with 2 grades not connected to any tests
        String orphanUsername = "user-no-tests";
        User orphan = userRepository.findByUsername(orphanUsername)
                .orElseGet(() -> userService.createUser(orphanUsername, "password", Collections.singletonList("ROLE_USER")));
        if (!Boolean.TRUE.equals(orphan.getActive())) {
            orphan.setActive(true);
        }
        // add profile info to orphan as well
        orphan.setFirstName("Alex");
        orphan.setLastName("Schmidt");
        orphan.setEmail(orphanUsername + "@example.com");
        orphan.setDateOfBirth(LocalDate.of(2004, 5, 12));
        orphan = userRepository.save(orphan);
        int orphanGrades = 0;
        // create 2 grades with no test
        final UUID orphanId = orphan.getId();
        List<Grade> existingOrphanGrades = gradeRepository.findAll().stream()
                .filter(g -> g.getStudent().getId().equals(orphanId) && g.getTest() == null)
                .toList();
        for (int i = existingOrphanGrades.size(); i < 2; i++) {
            Grade g = Grade.builder()
                    .student(orphan)
                    .test(null)
                    .value(BigDecimal.valueOf(randomGrade1to6Biased(rnd)))
                    .weight(BigDecimal.ONE)
                    .build();
            gradeRepository.save(g);
            orphanGrades++;
        }
        summary.put("orphanUser", orphan.getUsername());
        summary.put("orphanGradesCreated", orphanGrades);

        return ResponseEntity.ok(summary);
    }

    // Generate grades 1..6 with ~70% in [4,6]
    private double randomGrade1to6Biased(Random rnd) {
        // Two-phase: with 70% pick in [4,6], otherwise in [1,4)
        boolean high = rnd.nextDouble() < 0.7;
        double val;
        if (high) {
            val = 4.0 + rnd.nextDouble() * 2.0; // [4,6)
        } else {
            val = 1.0 + rnd.nextDouble() * 3.0; // [1,4)
        }
        // Round to one decimal commonly used in grading
        return Math.round(val * 10.0) / 10.0;
    }
}
