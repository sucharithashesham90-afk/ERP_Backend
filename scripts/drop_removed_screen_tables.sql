-- ============================================================================
-- Drop tables for the 9 EXCLUSIVE removed screens (backend already deleted).
--
-- SAFE: these tables are used by no kept screen (verified — the entities had
-- zero references from kept code before deletion). Only these 9 are dropped.
-- The shared tables (sales_territories, currencies, geo_*, product_categories,
-- units_of_measure, stock, bill_of_materials, products, vendors, godowns, ...)
-- are intentionally NOT here — kept screens still use them.
--
-- Run this ONCE against the production Postgres (Railway). Irreversible.
-- Take a DB backup/snapshot first.
-- ============================================================================

BEGIN;

DROP TABLE IF EXISTS cash_discount_plans        CASCADE;  -- CashDiscountPlan  (Sales → Cash Discount Plans)
DROP TABLE IF EXISTS free_samples               CASCADE;  -- FreeSample        (Sales → Free Sample Dispatch)
DROP TABLE IF EXISTS purchase_modes             CASCADE;  -- PurchaseMode      (Purchase → Purchase Mode Config)
DROP TABLE IF EXISTS agri_three_party_invoices  CASCADE;  -- ThreePartyInvoice (Purchase → Three Party Invoices / Supplier TP Invoice)
DROP TABLE IF EXISTS inventory_validations      CASCADE;  -- InventoryValidation (Inventory → Inventory Validation)
DROP TABLE IF EXISTS agri_bom_templates         CASCADE;  -- BomTemplate       (Quality → BOM Templates)
DROP TABLE IF EXISTS procurement_pricing        CASCADE;  -- ProcurementPricing (Master Data → Procurement Pricing)
DROP TABLE IF EXISTS process_configurations     CASCADE;  -- ProcessConfiguration (Manufacturing → Process Configuration)
DROP TABLE IF EXISTS agri_chemical_masters      CASCADE;  -- ChemicalMaster    (Agriculture → Chemical Master)

COMMIT;
