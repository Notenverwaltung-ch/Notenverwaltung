-- Users: legt zwei Benutzer (Studierende) an, die sich anmelden können
INSERT INTO users (id, username, password, email, first_name, last_name, active, created_at, updated_at)
VALUES (gen_random_uuid(), 'max', '{bcrypt}$2a$10$abcdefghijklmnopqrstuv', 'max@uni.de', 'Max', 'Mustermann', true,
        now(), now()),
       (gen_random_uuid(), 'erika', '{bcrypt}$2a$10$abcdefghijklmnopqrstuv', 'erika@uni.de', 'Erika', 'Musterfrau',
        true, now(), now());

-- User-Rollen: weist beiden die Rolle STUDENT zu
INSERT INTO user_roles (user_id, role)
SELECT u.id, 'STUDENT'
FROM users u
WHERE u.username IN ('max', 'erika');

-- Subjects: legt zwei Fächer an, auf die sich spätere Semester/Test-Daten beziehen
INSERT INTO subjects (id, name)
VALUES (gen_random_uuid(), 'Mathematik'),
       (gen_random_uuid(), 'Informatik');

-- Semesters: definiert einen Beispiel-Semester-Zeitraum
INSERT INTO semesters (id, name, start_date, end_date)
VALUES (gen_random_uuid(), 'HS2025', '2025-09-01', '2026-01-31');

-- Semester_Subjects: ordnet Fächer einem Semester zu (eine Kombination pro Zeile)
-- Zweck: bildet die Lehrangebote in einem Semester ab
INSERT INTO semester_subjects (id, semester_id, subject_id)
SELECT gen_random_uuid(), s.id, sub.id
FROM semesters s
         CROSS JOIN subjects sub
WHERE s.name = 'HS2025'
  AND sub.name IN ('Mathematik', 'Informatik');

-- Classes: legt eine Klasse für Mathematik und eine für Informatik im Semester HS2025 an
INSERT INTO classes (id, semester_subject_id, name)
SELECT gen_random_uuid(), ss.id, n
FROM (SELECT 'Mathematik' AS sub_name, 'Klasse M-1' AS n
      UNION ALL
      SELECT 'Informatik', 'Klasse I-1') x
         JOIN subjects sub ON sub.name = x.sub_name
         JOIN semesters sem ON sem.name = 'HS2025'
         JOIN semester_subjects ss ON ss.subject_id = sub.id AND ss.semester_id = sem.id;

-- Tests: legt je einen Test pro Klasse an (z. B. erste Prüfung)
INSERT INTO tests (id, name, class_id, semester_subject_id, date)
SELECT gen_random_uuid(), CONCAT(sub.name, ' Test 1'), c.id, ss.id, '2025-10-15'
FROM classes c
         JOIN semester_subjects ss ON ss.id = c.semester_subject_id
         JOIN subjects sub ON sub.id = ss.subject_id;

-- Grades: vergibt Beispielnoten für max und erika auf die angelegten Tests
INSERT INTO grades (id, value, weight, user_id, test_id, comment)
SELECT gen_random_uuid(), v.value, 50.00, u.id, t.id, v.cmt
FROM (SELECT 'max' AS uname, 5.50::numeric(4,2) AS value, 'Erste Prüfung'::varchar(255) AS cmt
      UNION ALL
      SELECT 'erika', 4.75:: numeric (4, 2), 'Erste Prüfung') v
         JOIN users u ON u.username = v.uname
         JOIN tests t ON t.name LIKE '% Test 1';
