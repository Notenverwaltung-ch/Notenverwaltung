-- Change created_on from DATE to TIMESTAMP WITHOUT TIME ZONE and preserve values
ALTER TABLE public.grades
    ALTER COLUMN created_on TYPE TIMESTAMP WITHOUT TIME ZONE
    USING created_on::timestamp;

-- Ensure not null remains enforced
ALTER TABLE public.grades ALTER COLUMN created_on SET NOT NULL;
