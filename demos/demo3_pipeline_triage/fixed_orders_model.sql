-- ============================================================================
-- FIXED MODEL — Pipeline Triage Demo (Part 2: Guest Checkout Handling)
-- ============================================================================
-- Root cause: The source table added guest checkout rows where customer_id
-- is NULL. The existing not_null test on customer_id now fails.
--
-- Analysis of options:
--   Option A: Filter out NULLs  — loses guest order data entirely.
--   Option B: COALESCE to a sentinel value (-1) — preserves data but
--             introduces a magic number that downstream consumers must know.
--   Option C: Separate guest model — cleanest separation of concerns but
--             requires downstream refactoring.
--
-- RECOMMENDATION: Option B (COALESCE) as an immediate fix to unblock the
-- pipeline, combined with a follow-up ticket to implement Option C for a
-- proper long-term solution. This preserves all guest order data while
-- keeping the not_null contract intact.
--
-- *** This is an escalation point — Devin recommends Option B but asks
--     for engineer confirmation before applying, because the choice of
--     sentinel value (-1 vs. a dedicated 'guest' dimension key) is a
--     business decision. ***
-- ============================================================================

SELECT
    order_id,
    -- Preserve guest orders: coalesce NULL customer_id to -1 (guest sentinel).
    -- Downstream consumers should treat customer_id = -1 as a guest checkout.
    coalesce(customer_id, -1) AS customer_id,
    order_date,
    status,
    amount,
    -- Flag for downstream filtering
    case
        when customer_id is null then true
        else false
    end as is_guest_checkout
FROM {{ ref('stg_orders') }}
