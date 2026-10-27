#!/usr/bin/env python3
import json
import sys
from pathlib import Path


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit(f"Usage: {sys.argv[0]} baseline.json current.json")
    baseline = json.loads(Path(sys.argv[1]).read_text())
    current = json.loads(Path(sys.argv[2]).read_text())
    if baseline["profile"] != current["profile"]:
        raise SystemExit("Profiles differ; comparison refused")

    print(f"profile: {current['profile']['profile_id']}")
    for key in ("mean_ns", "stddev_ns", "min_ns", "p50_ns", "p90_ns", "p95_ns", "p99_ns", "max_ns"):
        old = baseline["latency"][key]
        new = current["latency"][key]
        delta = ((new / old) - 1) * 100 if old else float("nan")
        print(f"{key:12} {old:12.2f} -> {new:12.2f}  {delta:+8.2f}%")
    print(f"sample_n     {baseline['latency']['sample_n']} -> {current['latency']['sample_n']}")


if __name__ == "__main__":
    main()
