-- ============================================================================
-- BROKEN MODEL — Pipeline Triage Demo (Part 1: Typo)
-- ============================================================================
-- This model is failing in CI.
-- Error: column 'custmer_id' does not exist
-- ============================================================================

SELECT
    customer_id,
    SUM(amount)    AS total_revenue,
    COUNT(order_id) AS order_count
FROM {{ ref('orders') }}
GROUP BY custmer_id
