#!/usr/bin/env python3
import csv
import json
import math
import statistics
import sys
from pathlib import Path


def metadata(path: Path) -> dict[str, str]:
    result = {}
    for line in path.read_text().splitlines():
        if "=" in line:
            key, value = line.split("=", 1)
            result[key] = value
    return result


def latencies(path: Path) -> list[int]:
    with path.open(newline="") as stream:
        return [int(row[2]) for row in csv.reader(stream)]


def stats(values: list[int]) -> dict[str, int | float]:
    ordered = sorted(values)

    def ordered_percentile(quantile: float) -> int:
        index = max(0, math.ceil(quantile * len(ordered)) - 1)
        return ordered[index]

    return {
        "sample_n": len(values),
        "mean_ns": statistics.fmean(values),
        "stddev_ns": statistics.stdev(values) if len(values) > 1 else 0.0,
        "min_ns": ordered[0],
        "p50_ns": ordered_percentile(0.50),
        "p90_ns": ordered_percentile(0.90),
        "p95_ns": ordered_percentile(0.95),
        "p99_ns": ordered_percentile(0.99),
        "max_ns": ordered[-1],
    }


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit(f"Usage: {sys.argv[0]} path/to/series-directory")
    series = Path(sys.argv[1])
    run_dirs = sorted(path for path in series.glob("run-[0-9][0-9][0-9]") if path.is_dir())
    if not run_dirs:
        raise SystemExit(f"No runs in {series}")

    rows = []
    combined = []
    identity = None
    identity_keys = ("PROFILE_ID", "PROFILE_SHA256", "REQUEST_ID", "GIT_COMMIT")
    for run_dir in run_dirs:
        meta = metadata(run_dir / "metadata.env")
        current_identity = {key: meta.get(key) for key in identity_keys}
        if identity is None:
            identity = current_identity
        elif current_identity != identity:
            raise SystemExit(f"Profile or commit mismatch in {run_dir}")
        values = latencies(run_dir / "samples.csv")
        run_stats = stats(values)
        report = json.loads((run_dir / "report.json").read_text())
        row = {
            "run": run_dir.name,
            "valid": meta.get("VALID") == "true",
            "success_ratio": report["success"],
            "throughput_rps": report["throughput"],
            "status_codes": json.dumps(report["status_codes"], sort_keys=True),
            **run_stats,
        }
        rows.append(row)
        if row["valid"]:
            combined.extend(values)

    if not combined:
        raise SystemExit("No valid runs")

    fieldnames = list(rows[0])
    with (series / "runs-summary.csv").open("w", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    first_meta = metadata(run_dirs[0] / "metadata.env")
    result = {
        "schema_version": 1,
        "git_commit": first_meta["GIT_COMMIT"],
        "profile": {
            key.lower(): first_meta[key]
            for key in (
                "PROFILE_ID", "PROFILE_SHA256", "REQUEST_ID",
                "TARGET_URL", "TARGET_SHA256", "BODY_BYTES", "BODY_SHA256",
                "CHUNKED", "EXPECTED_STATUS", "RATE_RPS", "DURATION",
                "WARMUP_DURATION", "WORKERS", "CONNECTIONS", "KEEPALIVE",
                "HTTP2", "HOST_ID", "JAVA_VERSION", "VEGETA_VERSION",
                "SERVER_CPUS", "VEGETA_CPUS", "NGINX_CONFIG_SHA256",
            )
        },
        "valid_runs": sum(row["valid"] for row in rows),
        "total_runs": len(rows),
        "latency": stats(combined),
    }
    (series / "combined-summary.json").write_text(json.dumps(result, indent=2) + "\n")
    print(series / "combined-summary.json")


if __name__ == "__main__":
    main()
