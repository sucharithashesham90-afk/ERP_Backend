-- Drop legacy check constraints on production_jobs left over from when the
-- planning module used this table. The manufacturing module uses different
-- enum values (DRAFT/PLANNED for status; any string for job_type).
-- plan_id has no meaning in the manufacturing context so make it nullable.

ALTER TABLE production_jobs DROP CONSTRAINT IF EXISTS production_jobs_job_type_check;
ALTER TABLE production_jobs DROP CONSTRAINT IF EXISTS production_jobs_status_check;
ALTER TABLE production_jobs ALTER COLUMN plan_id DROP NOT NULL;
