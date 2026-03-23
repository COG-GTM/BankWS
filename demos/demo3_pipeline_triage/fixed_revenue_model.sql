-- ============================================================================
-- FIXED MODEL — Pipeline Triage Demo (Part 1: Typo Resolved)
-- ============================================================================
-- Root cause: Typo in GROUP BY clause — 'custmer_id' should be 'customer_id'.
-- The SELECT clause correctly references 'customer_id', but the GROUP BY
-- used 'custmer_id' (missing the 'o'), causing a column-not-found error.
--
-- Fix: Corrected the GROUP BY column name to match the SELECT clause.
-- ============================================================================

SELECT
    customer_id,
    SUM(amount)    AS total_revenue,
    COUNT(order_id) AS order_count
FROM {{ ref('orders') }}
GROUP BY customer_id
