#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "$script_dir/../.." && pwd)"
cd "$project_dir"

profile_dir="${1:-}"
[[ -n "$profile_dir" && -d "$profile_dir" ]] || {
  echo "Usage: $0 path/to/profile-directory" >&2
  exit 2
}

summary="$profile_dir/runs-summary.csv"
printf '%s\n' 'run,sample_n,valid,success_ratio,mean_ns,stddev_ns,min_ns,p50_ns,p90_ns,p95_ns,p99_ns,max_ns,throughput_rps,status_codes' > "$summary"

found=false
for run_dir in "$profile_dir"/run-[0-9][0-9][0-9]; do
  [[ -d "$run_dir" ]] || continue
  found=true
  samples="$run_dir/samples.csv"
  report="$run_dir/report.json"
  metadata="$run_dir/metadata.env"
  [[ -s "$samples" && -s "$report" && -s "$metadata" ]] || {
    echo "Incomplete run: $run_dir" >&2
    exit 1
  }

  read -r sample_n mean_ns stddev_ns min_ns max_ns < <(
    awk -F, '
    NR == 1 { min = $3; max = $3 }
    {
      x = $3
      n += 1
      delta = x - mean
      mean += delta / n
      m2 += delta * (x - mean)
      if (x < min) min = x
      if (x > max) max = x
    }
    END {
      stddev = n > 1 ? sqrt(m2 / (n - 1)) : 0
      printf "%d %.6f %.6f %.0f %.0f\n", n, mean, stddev, min, max
    }' "$samples"
  )

  run="${run_dir##*/run-}"
  valid="$(awk -F= '$1 == "VALID" {value = $2} END {print value}' "$metadata")"
  success="$(jq -r '.success' "$report")"
  throughput="$(jq -r '.throughput' "$report")"
  p50="$(jq -r '.latencies["50th"]' "$report")"
  p90="$(jq -r '.latencies["90th"]' "$report")"
  p95="$(jq -r '.latencies["95th"]' "$report")"
  p99="$(jq -r '.latencies["99th"]' "$report")"
  statuses="$(jq -c '.status_codes' "$report" | sed 's/"/""/g')"

  printf '%s,%s,%s,%s,%.6f,%.6f,%s,%s,%s,%s,%s,%s,%s,"%s"\n' \
    "$run" "$sample_n" "$valid" "$success" "$mean_ns" "$stddev_ns" \
    "$min_ns" "$p50" "$p90" "$p95" "$p99" "$max_ns" "$throughput" "$statuses" \
    >> "$summary"
done

[[ "$found" == true ]] || { echo "No run-NNN directories in $profile_dir" >&2; exit 1; }
echo "$summary"
