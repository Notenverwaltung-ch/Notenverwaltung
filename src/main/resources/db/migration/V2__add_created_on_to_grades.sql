-- Add created_on date column to grades and populate with current date
ALTER TABLE public.grades ADD COLUMN IF NOT EXISTS created_on DATE;

-- Backfill existing rows with current date where null
UPDATE public.grades SET created_on = CURRENT_DATE WHERE created_on IS NULL;

-- Enforce NOT NULL constraint
ALTER TABLE public.grades ALTER COLUMN created_on SET NOT NULL;
