"""
Demo 1 — Data Ingestion + Schema Change
=========================================
Connects to the public GitHub API, pulls the list of repositories for
the organisation 'dbt-labs', and loads them into a local SQLite table
called `raw_repos`.

Usage:
    python ingest_repos.py
"""

import json
import sqlite3
import urllib.request

GITHUB_API = "https://api.github.com"
ORG = "dbt-labs"
DB_PATH = "raw_repos.db"

# ── Schema definition (single source of truth) ──────────────────────
COLUMNS = [
    ("repo_name", "TEXT NOT NULL"),
    ("description", "TEXT"),
    ("language", "TEXT"),
    ("star_count", "INTEGER"),
    ("last_updated", "TEXT"),
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


def create_table(conn: sqlite3.Connection) -> None:
    """Create the raw_repos table if it does not exist."""
    col_defs = ", ".join(f"{name} {typ}" for name, typ in COLUMNS)
    conn.execute(f"CREATE TABLE IF NOT EXISTS raw_repos (id INTEGER PRIMARY KEY AUTOINCREMENT, {col_defs})")
    conn.commit()


def load_repos(conn: sqlite3.Connection, repos: list[dict]) -> int:
    """Insert repository records into raw_repos. Returns row count."""
    rows = [
        (
            r["name"],
            r.get("description"),
            r.get("language"),
            r.get("stargazers_count", 0),
            r.get("updated_at"),
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
    print(f"  -> Retrieved {len(repos)} repositories.")

    conn = sqlite3.connect(DB_PATH)
    create_table(conn)
    count = load_repos(conn, repos)
    print(f"  -> Loaded {count} rows into raw_repos table.")

    # Quick verification
    cursor = conn.execute("SELECT repo_name, star_count FROM raw_repos ORDER BY star_count DESC LIMIT 5")
    print("\nTop 5 repos by stars:")
    for name, stars in cursor:
        print(f"  {name:30s} {stars:>6,} stars")

    conn.close()
    print(f"\nDone. Database saved to {DB_PATH}")


if __name__ == "__main__":
    main()
