-- 1) Alle Noten eines Benutzers (Student) anhand des Usernamens
SELECT u.first_name, u.last_name, g.value, t.name AS test_name, sub.name AS subject, sem.name AS semester
FROM grades g
JOIN users u ON g.student_id = u.id
JOIN tests t ON g.test_id = t.id
JOIN semester_subjects ss ON t.semester_subject_id = ss.id
JOIN subjects sub ON ss.subject_id = sub.id
JOIN semesters sem ON ss.semester_id = sem.id
WHERE u.username = 'max'
ORDER BY sem.start_date, sub.name, t.date;

-- 2) Alle Tests in einem bestimmten Semester, gruppiert nach Benutzer (nur Tests, Note optional)
SELECT sem.name AS semester,
       u.username,
       u.first_name,
       u.last_name,
       sub.name      AS subject,
       c.name        AS class,
       t.name        AS test_name,
       t.date        AS test_date,
       g.value       AS grade
FROM semesters sem
JOIN semester_subjects ss ON ss.semester_id = sem.id
JOIN subjects sub ON sub.id = ss.subject_id
JOIN classes c ON c.semester_subject_id = ss.id
JOIN tests t ON t.class_id = c.id
LEFT JOIN grades g ON g.test_id = t.id
LEFT JOIN users u ON u.id = g.student_id
WHERE sem.name = 'SEM-1-2025'
ORDER BY u.username NULLS LAST, sub.name, t.date;

-- 3) Alle Tests eines Semesters mit Durchschnittsnote pro Test
SELECT sem.name AS semester,
       sub.name AS subject,
       t.name   AS test_name,
       t.date,
       ROUND(AVG(g.value), 2) AS avg_grade,
       COUNT(g.id) AS grades_count
FROM tests t
JOIN classes c ON c.id = t.class_id
JOIN semester_subjects ss ON ss.id = c.semester_subject_id
JOIN semesters sem ON sem.id = ss.semester_id
JOIN subjects sub ON sub.id = ss.subject_id
LEFT JOIN grades g ON g.test_id = t.id
WHERE sem.name = 'SEM-1-2025'
GROUP BY sem.name, sub.name, t.name, t.date
ORDER BY sub.name, t.date;

-- 4) Notenübersicht je Benutzer und Fach in einem Semester (Pivot-ähnlich)
SELECT u.username,
       sub.name AS subject,
       ROUND(AVG(g.value), 2) AS avg_grade_subject
FROM users u
JOIN grades g ON g.student_id = u.id
JOIN tests t ON t.id = g.test_id
JOIN classes c ON c.id = t.class_id
JOIN semester_subjects ss ON ss.id = c.semester_subject_id
JOIN subjects sub ON sub.id = ss.subject_id
JOIN semesters sem ON sem.id = ss.semester_id
WHERE sem.name = 'SEM-1-2025'
GROUP BY u.username, sub.name
ORDER BY u.username, sub.name;

-- 5) Fehlende Bewertungen: Tests ohne Note je Benutzer (zeigt offene Bewertungen)
SELECT u.username,
       sub.name AS subject,
       t.name   AS test_name,
       t.date   AS test_date
FROM users u
JOIN semesters sem ON sem.name = 'SEM-1-2025'
JOIN semester_subjects ss ON ss.semester_id = sem.id
JOIN subjects sub ON sub.id = ss.subject_id
JOIN classes c ON c.semester_subject_id = ss.id
JOIN tests t ON t.class_id = c.id
LEFT JOIN grades g ON g.test_id = t.id AND g.student_id = u.id
WHERE g.id IS NULL
ORDER BY u.username, sub.name, t.date;

-- 6) Rangliste pro Test (beste zuerst) inkl. Semester
SELECT sem.name AS semester,
       t.name   AS test_name,
       u.username,
       g.value  AS grade
FROM grades g
JOIN tests t ON t.id = g.test_id
JOIN users u ON u.id = g.student_id
JOIN classes c ON c.id = t.class_id
JOIN semester_subjects ss ON ss.id = c.semester_subject_id
JOIN semesters sem ON sem.id = ss.semester_id
WHERE t.name LIKE '%Test 1%'
ORDER BY sem.name, t.name, g.value DESC NULLS LAST;

-- 7) Workload je Fach im Semester: Anzahl Klassen, Tests, Bewertungen
WITH test_counts AS (
  SELECT ss.id,
         COUNT(DISTINCT c.id) AS classes_cnt,
         COUNT(DISTINCT t.id) AS tests_cnt,
         COUNT(g.id)          AS grades_cnt
  FROM semester_subjects ss
  LEFT JOIN classes c ON c.semester_subject_id = ss.id
  LEFT JOIN tests t   ON t.class_id = c.id
  LEFT JOIN grades g  ON g.test_id = t.id
  GROUP BY ss.id
)
SELECT sem.name AS semester,
       sub.name AS subject,
       tc.classes_cnt,
       tc.tests_cnt,
       tc.grades_cnt
FROM test_counts tc
JOIN semester_subjects ss ON ss.id = tc.id
JOIN semesters sem ON sem.id = ss.semester_id
JOIN subjects sub ON sub.id = ss.subject_id
WHERE sem.name = 'SEM-1-2025'
ORDER BY sub.name;
