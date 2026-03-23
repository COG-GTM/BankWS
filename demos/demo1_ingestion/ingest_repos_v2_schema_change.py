"""
Demo 1 — Schema Change Handling (V2)
======================================
The upstream source has added a new field: forks_count.
This updated connector adds the new column to the existing table
without breaking existing data, then re-ingests with the new field.

Usage:
    python ingest_repos_v2_schema_change.py
"""

import json
import sqlite3
import urllib.request

GITHUB_API = "https://api.github.com"
ORG = "dbt-labs"
DB_PATH = "raw_repos.db"

# ── Updated schema — forks_count added ──────────────────────────────
COLUMNS = [
    ("repo_name", "TEXT NOT NULL"),
    ("description", "TEXT"),
    ("language", "TEXT"),
    ("star_count", "INTEGER"),
    ("last_updated", "TEXT"),
    ("forks_count", "INTEGER"),  # NEW FIELD — schema evolution
]


def fetch_repos(org: str) -> list[dict]:
    """Fetch public repositories for *org* from the GitHub REST API."""
    repos: list[dict] = []
    page = 1
    while True:
        url = f"{GITHUB_API}/orgs/{org}/repos?per_page=100&page={page}"
        req = urllib.request.Request(url, headers={"Accept": "application/vnd.github+json"})
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode())
        if not data:
            break
        repos.extend(data)
        page += 1
    return repos


def migrate_schema(conn: sqlite3.Connection) -> None:
    """Add forks_count column if it does not already exist (schema evolution)."""
    cursor = conn.execute("PRAGMA table_info(raw_repos)")
    existing_cols = {row[1] for row in cursor.fetchall()}

    if "forks_count" not in existing_cols:
        print("  [SCHEMA CHANGE] Adding 'forks_count' column to raw_repos...")
        conn.execute("ALTER TABLE raw_repos ADD COLUMN forks_count INTEGER")
        conn.commit()
        print("  [SCHEMA CHANGE] Column added successfully.")
    else:
        print("  [SCHEMA] 'forks_count' column already exists — no migration needed.")


def create_table(conn: sqlite3.Connection) -> None:
    """Create the raw_repos table if it does not exist (includes new column)."""
    col_defs = ", ".join(f"{name} {typ}" for name, typ in COLUMNS)
    conn.execute(f"CREATE TABLE IF NOT EXISTS raw_repos (id INTEGER PRIMARY KEY AUTOINCREMENT, {col_defs})")
    conn.commit()


def load_repos(conn: sqlite3.Connection, repos: list[dict]) -> int:
    """Insert repository records into raw_repos (full refresh). Returns row count."""
    # Clear existing data for a clean reload
    conn.execute("DELETE FROM raw_repos")

    rows = [
        (
            r["name"],
            r.get("description"),
            r.get("language"),
            r.get("stargazers_count", 0),
            r.get("updated_at"),
            r.get("forks_count", 0),  # NEW FIELD
        )
        for r in repos
    ]
    placeholders = ", ".join(["?"] * len(COLUMNS))
    col_names = ", ".join(name for name, _ in COLUMNS)
    conn.executemany(f"INSERT INTO raw_repos ({col_names}) VALUES ({placeholders})", rows)
    conn.commit()
    return len(rows)


def main() -> None:
    print(f"Fetching repositories for '{ORG}' from GitHub API...")
    repos = fetch_repos(ORG)
    print(f"  -> Retrieved {len(repos)} repositories.\n")

    conn = sqlite3.connect(DB_PATH)

    # Step 1: Ensure table exists
    create_table(conn)

    # Step 2: Migrate schema (add forks_count if missing)
    migrate_schema(conn)

    # Step 3: Reload data with new field
    count = load_repos(conn, repos)
    print(f"\n  -> Loaded {count} rows into raw_repos table (with forks_count).")

    # Verification — show top repos by stars with fork counts
    cursor = conn.execute(
        "SELECT repo_name, star_count, forks_count FROM raw_repos ORDER BY star_count DESC LIMIT 5"
    )
    print("\nTop 5 repos by stars (now including forks):")
    for name, stars, forks in cursor:
        print(f"  {name:30s} {stars:>6,} stars  |  {forks:>5,} forks")

    conn.close()
    print(f"\nDone. Database saved to {DB_PATH}")


if __name__ == "__main__":
    main()
