-- Tables
CREATE TABLE IF NOT EXISTS public.users (
    id UUID NOT NULL,
    active BOOLEAN,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE,
    date_of_birth DATE,
    email VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
    username VARCHAR(255) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
    CONSTRAINT uk_r43af9ap4edm43mmtq01oddj6 UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS public.subjects (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT subjects_pkey PRIMARY KEY (id),
    CONSTRAINT uk_aodt3utnw0lsov4k9ta88dbpr UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS public.semesters (
    id UUID NOT NULL,
    end_date DATE NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    CONSTRAINT semesters_pkey PRIMARY KEY (id),
    CONSTRAINT uk_ci1s5s8npb7j044md3s0wdhsh UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS public.semester_subjects (
    id UUID NOT NULL,
    semester_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    CONSTRAINT semester_subjects_pkey PRIMARY KEY (id),
    CONSTRAINT ukc547nh8l37tcmsjdrtagtb0rl UNIQUE (semester_id, subject_id),
    CONSTRAINT fktnt05hsq5hx7laf8pbvrjf70v FOREIGN KEY (semester_id) REFERENCES public.semesters(id),
    CONSTRAINT fk3bmk5k5db3vdmsdscd7rtj3g8 FOREIGN KEY (subject_id) REFERENCES public.subjects(id)
);

CREATE TABLE IF NOT EXISTS public.classes (
    id UUID NOT NULL,
    name VARCHAR(100),
    semester_subject_id UUID NOT NULL,
    CONSTRAINT classes_pkey PRIMARY KEY (id),
    CONSTRAINT fkigenspuohypmjma7y99qa38qg FOREIGN KEY (semester_subject_id) REFERENCES public.semester_subjects(id)
);

CREATE TABLE IF NOT EXISTS public.tests (
    id UUID NOT NULL,
    comment VARCHAR(1000),
    date DATE NOT NULL,
    name VARCHAR(255) NOT NULL,
    class_id UUID NOT NULL,
    semester_subject_id UUID NOT NULL,
    CONSTRAINT tests_pkey PRIMARY KEY (id),
    CONSTRAINT fkc2xm9sb3bnle73gvvr5pwhnms FOREIGN KEY (class_id) REFERENCES public.classes(id),
    CONSTRAINT fkkuuk24xil58od8t6f8g94ngtd FOREIGN KEY (semester_subject_id) REFERENCES public.semester_subjects(id)
);

CREATE TABLE IF NOT EXISTS public.grades (
    id UUID NOT NULL,
    comment VARCHAR(255),
    value NUMERIC(5,2) NOT NULL,
    weight NUMERIC(5,2) NOT NULL,
    student_id UUID NOT NULL,
    test_id UUID,
    CONSTRAINT grades_pkey PRIMARY KEY (id),
    CONSTRAINT fk2udi8qqpoqmopyp47iy76jeq6 FOREIGN KEY (student_id) REFERENCES public.users(id),
    CONSTRAINT fkdt8vl03iq6po3f5snur6l5cvs FOREIGN KEY (test_id) REFERENCES public.tests(id)
);

CREATE TABLE IF NOT EXISTS public.user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(255),
    CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id)
);

-- Indexes
-- (no extra indexes beyond constraints in this schema)
