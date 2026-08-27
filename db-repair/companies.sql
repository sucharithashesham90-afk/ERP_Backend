-- Repair public.companies to match the entity.
-- Every statement is a no-op if the column is already there, so this is safe to re-run.
-- Replace public with erp_generic for the other deployment.

ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS active boolean;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS financial_year_start varchar(2);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS pdf_watermark_enabled boolean DEFAULT true;
UPDATE public.companies SET pdf_watermark_enabled = true WHERE pdf_watermark_enabled IS NULL;
ALTER TABLE public.companies ALTER COLUMN pdf_watermark_enabled SET NOT NULL;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS created_at timestamp(6);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS deleted_at timestamp(6);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS updated_at timestamp(6);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS currency varchar(10);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS pan varchar(10);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS tan varchar(10);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS bank_ifsc varchar(11);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS tenant_id uuid;
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS gstin varchar(20);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS phone varchar(20);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS postal_code varchar(20);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS cin varchar(21);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS bank_account varchar(30);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS industry varchar(50);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS registration_number varchar(50);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS tax_number varchar(50);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS bank_branch varchar(100);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS bank_name varchar(100);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS city varchar(100);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS country varchar(100);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS created_by varchar(100);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS email varchar(100);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS state varchar(100);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS updated_by varchar(100);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS legal_name varchar(200);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS name varchar(200);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS website varchar(200);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS address varchar(500);
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS logo text;
