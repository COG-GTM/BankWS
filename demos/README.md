# Barclays EDP Demo — Devin for Data Engineering

Three demos showing how Devin accelerates the data engineering workload
that sits between Barclays' data-as-a-product vision and production reality.

---

## Demo 1 — Data Ingestion + Schema Change

> *Devin builds a connector from a public data source into a target schema,
> then handles a schema change automatically.*

| File | Description |
|------|-------------|
| `demo1_ingestion/ingest_repos.py` | V1 — Ingests dbt-labs GitHub repos into SQLite |
| `demo1_ingestion/ingest_repos_v2_schema_change.py` | V2 — Adds `forks_count` column with safe schema migration |

**Run it:**
```bash
cd demos/demo1_ingestion
python ingest_repos.py                       # V1 — base connector
python ingest_repos_v2_schema_change.py      # V2 — schema evolution
```

---

## Demo 2 — Transform SQL into a dbt Model

> *Devin converts raw SQL transformation logic into a clean, documented dbt
> model mapped to a data product contract.*

| File | Description |
|------|-------------|
| `demo2_dbt_transform/raw_customers.sql` | Original raw SQL (before transformation) |
| `demo2_dbt_transform/customers.sql` | Clean dbt model with comments, derived fields |
| `demo2_dbt_transform/schema.yml` | Column descriptions + data quality tests |

**Key additions:**
- `customer_status` derived field (prospect / new / active / lapsed)
- 7 data quality tests (unique, not_null, accepted_values)
- Catalogue-ready column documentation

---

## Demo 3 — Pipeline Triage

> *Devin investigates broken dbt models, identifies root causes, patches or
> escalates as appropriate.*

| File | Description |
|------|-------------|
| `demo3_pipeline_triage/broken_revenue_model.sql` | Broken model with `custmer_id` typo |
| `demo3_pipeline_triage/fixed_revenue_model.sql` | Autonomous fix — typo corrected |
| `demo3_pipeline_triage/broken_orders_model.sql` | Model failing not_null test |
| `demo3_pipeline_triage/fixed_orders_model.sql` | Recommended fix with escalation notes |
| `demo3_pipeline_triage/triage_analysis.md` | Full triage report with options analysis |

**Escalation boundary:** Demo 3 Part 2 shows Devin stopping to ask for
confirmation before applying a fix that involves a business decision.
