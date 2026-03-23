-- ============================================================================
-- BROKEN MODEL — Pipeline Triage Demo (Part 2: not_null test failure)
-- ============================================================================
-- The orders model fails a not_null test on customer_id.
-- The source table recently added rows where customer_id can be NULL
-- for guest checkouts.
-- ============================================================================

SELECT
    order_id,
    customer_id,
    order_date,
    status,
    amount
FROM {{ ref('stg_orders') }}
