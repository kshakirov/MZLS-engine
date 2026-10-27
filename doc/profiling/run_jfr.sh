#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "$script_dir/../.." && pwd)"
cd "$project_dir"

profile="${1:-}"
case "$profile" in
  get|fixed|chunked) ;;
  *) echo "Usage: $0 get|fixed|chunked" >&2; exit 2 ;;
esac

java_home="${MZLS_JAVA_HOME:-$HOME/opt/graalvm-jdk-21}"
jfr_bin="$java_home/bin/jfr"
[[ -x "$jfr_bin" ]] || { echo "JFR not found: $jfr_bin" >&2; exit 1; }

stamp="$(date -u +%Y%m%dT%H%M%SZ)"
output_dir="var/profiling/raw/$(date -u +%F)/$profile/$stamp"
recording="$(pwd)/$output_dir/recording.jfr"
mkdir -p "$output_dir"

export RUNS_OVERRIDE=1
export SERIES_ID="$stamp"
export MZLS_RESULTS_ROOT="$output_dir/load"
export MZLS_JAVA_OPTIONS="-XX:StartFlightRecording=filename=$recording,settings=profile,dumponexit=true"

doc/performance/run_series.sh "$profile"
[[ -s "$recording" ]] || { echo "JFR recording was not created" >&2; exit 1; }

"$jfr_bin" summary "$recording" > "$output_dir/jfr-summary.txt"
"$jfr_bin" view hot-methods "$recording" > "$output_dir/hot-methods.txt"
"$jfr_bin" view allocation-by-site "$recording" > "$output_dir/allocation-by-site.txt"
"$jfr_bin" view pinned-threads "$recording" > "$output_dir/pinned-threads.txt" || true
"$jfr_bin" view thread-cpu-load "$recording" > "$output_dir/thread-cpu-load.txt" || true

echo "Profiling complete: $output_dir"
