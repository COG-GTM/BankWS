-- ============================================================================
-- customers.sql — Clean dbt model (transformed by Devin)
-- ============================================================================
-- Data Product: Customer Profiles
-- Description:  One row per customer with order history summary, lifetime
--               value, and derived customer_status field.
-- ============================================================================

{{
    config(
        materialized = 'table',
        tags         = ['customer_profiles', 'data_product']
    )
}}


-- Step 1: Pull base customer attributes from the staging layer.
--         stg_customers contains one row per customer with PII fields.
with customers as (

    select
        customer_id,
        first_name,
        last_name
    from {{ ref('stg_customers') }}

),

-- Step 2: Aggregate order-level metrics per customer.
--         We derive first/last order dates and total order count.
customer_orders as (

    select
        customer_id,
        min(order_date)   as first_order_date,
        max(order_date)   as most_recent_order_date,
        count(order_id)   as number_of_orders
    from {{ ref('stg_orders') }}
    group by customer_id

),

-- Step 3: Aggregate payment totals per customer.
--         Payments are joined to orders to resolve the customer linkage.
customer_payments as (

    select
        orders.customer_id,
        sum(payments.amount) as total_amount
    from {{ ref('stg_payments') }} as payments
    inner join {{ ref('stg_orders') }} as orders
        on payments.order_id = orders.order_id
    group by orders.customer_id

),

-- Step 4: Join all CTEs and compute derived fields.
--         customer_status is derived from order history to satisfy the
--         Customer Profiles data product contract.
final as (

    select
        customers.customer_id,
        customers.first_name,
        customers.last_name,

        -- Order history
        customer_orders.first_order_date,
        customer_orders.most_recent_order_date,
        customer_orders.number_of_orders,

        -- Revenue
        coalesce(customer_payments.total_amount, 0) as customer_lifetime_value,

        -- Derived: customer_status (required by Customer Profiles contract)
        case
            when customer_orders.customer_id is null
                then 'prospect'
            when customer_orders.number_of_orders = 1
                then 'new'
            when customer_orders.most_recent_order_date >= dateadd('day', -90, current_date)
                then 'active'
            else 'lapsed'
        end as customer_status

    from customers

    left join customer_orders
        on customers.customer_id = customer_orders.customer_id

    left join customer_payments
        on customers.customer_id = customer_payments.customer_id

)

select * from final
