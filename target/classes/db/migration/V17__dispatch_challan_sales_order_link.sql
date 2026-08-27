-- A dispatch challan is raised against a sales order, not a delivery order.
--
-- The screen used to pick a delivery order and store it in delivery_order_id / _number. Those
-- columns stay exactly as they are: challans already filed against a delivery order keep their
-- link, and nothing here rewrites history. New challans record the sales order instead.
--
-- Kept as their own columns rather than reusing the delivery-order ones, so a challan raised
-- against a sales order is not filed under a name that means something else.

ALTER TABLE agri_dispatch_challans
    ADD COLUMN IF NOT EXISTS sales_order_id     UUID,
    ADD COLUMN IF NOT EXISTS sales_order_number VARCHAR(100);
