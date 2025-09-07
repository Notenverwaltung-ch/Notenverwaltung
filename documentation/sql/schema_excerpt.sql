-- Tabelle: users
CREATE TABLE public.users (
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

-- Tabelle: user_roles
CREATE TABLE public.user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(255),
    CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id)
);

-- Tabelle: subjects
CREATE TABLE public.subjects (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT subjects_pkey PRIMARY KEY (id),
    CONSTRAINT uk_aodt3utnw0lsov4k9ta88dbpr UNIQUE (name)
);

-- Tabelle: tests
CREATE TABLE public.tests (
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