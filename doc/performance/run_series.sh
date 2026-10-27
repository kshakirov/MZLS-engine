#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "$script_dir/../.." && pwd)"
cd "$project_dir"

profile_name="${1:-}"
case "$profile_name" in
  get|fixed|chunked) ;;
  *) echo "Usage: $0 get|fixed|chunked" >&2; exit 2 ;;
esac

profile_file="doc/performance/profiles/$profile_name.env"
# shellcheck disable=SC1090
source "$profile_file"

runs="${RUNS_OVERRIDE:-$RUNS}"
results_root="${MZLS_RESULTS_ROOT:-var/performance/raw}"
vegeta_bin="${VEGETA_BIN:-$HOME/.local/bin/vegeta}"
server_pid_file="${SERVER_PID_FILE:-var/runtime/server.pid}"
server_log="${SERVER_LOG:-var/runtime/server.log}"
nginx_config="${NGINX_CONFIG:-/etc/nginx/sites-enabled/mzls-engine-bench}"

[[ "$runs" =~ ^[1-9][0-9]*$ ]] || { echo "RUNS must be positive" >&2; exit 2; }
[[ -x "$vegeta_bin" ]] || { echo "Vegeta not found: $vegeta_bin" >&2; exit 1; }
command -v jq >/dev/null || { echo "jq not found" >&2; exit 1; }
command -v taskset >/dev/null || { echo "taskset not found" >&2; exit 1; }
[[ -f "$TARGET_FILE" ]] || { echo "Target not found: $TARGET_FILE" >&2; exit 1; }
[[ -z "$(git status --porcelain --untracked-files=normal)" ]] || {
  echo "Tracked worktree is dirty; commit or restore it before measuring" >&2
  exit 1
}

if [[ -n "$BODY_FILE" ]]; then
  [[ -f "$BODY_FILE" ]] || { echo "Body not found: $BODY_FILE" >&2; exit 1; }
  actual_body_bytes="$(wc -c < "$BODY_FILE")"
  [[ "$actual_body_bytes" -eq "$BODY_BYTES" ]] || {
    echo "Body size is $actual_body_bytes, expected $BODY_BYTES" >&2
    exit 1
  }
fi

series_stamp="${SERIES_ID:-$(date -u +%Y%m%dT%H%M%SZ)}"
date_utc="$(date -u +%F)"
series_dir="$results_root/$date_utc/$REQUEST_ID/$PROFILE_ID/$series_stamp"
[[ ! -e "$series_dir" ]] || { echo "Series exists: $series_dir" >&2; exit 1; }
mkdir -p "$series_dir"

active_run_dir=""
cleanup() {
  SERVER_PID_FILE="$server_pid_file" "$script_dir/stop_server.sh" >/dev/null 2>&1 || true
  if [[ -n "$active_run_dir" && -f "$server_log" && ! -f "$active_run_dir/server.log" ]]; then
    cp "$server_log" "$active_run_dir/server.log"
  fi
}
trap cleanup EXIT

profile_sha="$(sha256sum "$profile_file" | awk '{print $1}')"
target_sha="$(sha256sum "$TARGET_FILE" | awk '{print $1}')"
body_sha="none"
[[ -z "$BODY_FILE" ]] || body_sha="$(sha256sum "$BODY_FILE" | awk '{print $1}')"
nginx_sha="unreadable"
[[ ! -r "$nginx_config" ]] || nginx_sha="$(sha256sum "$nginx_config" | awk '{print $1}')"

for ((run_number = 1; run_number <= runs; run_number++)); do
  run_index="$(printf '%03d' "$run_number")"
  run_dir="$series_dir/run-$run_index"
  mkdir -p "$run_dir"
  active_run_dir="$run_dir"

  if ((run_number == 1)); then
    MZLS_SKIP_BUILD=false "$script_dir/build_and_start_server.sh"
  else
    MZLS_SKIP_BUILD=true "$script_dir/build_and_start_server.sh"
  fi

  server_pid="$(<"$server_pid_file")"
  [[ -r "/proc/$server_pid/cmdline" ]] || { echo "Server is not running" >&2; exit 1; }
  server_command="$(tr '\0' ' ' < "/proc/$server_pid/cmdline")"
  [[ "$server_command" == *nano.engine.Main* ]] || { echo "Wrong server PID" >&2; exit 1; }

  taskset -apc "$SERVER_CPUS" "$server_pid" >/dev/null
  unexpected_masks="$({ for task in "/proc/$server_pid"/task/*; do awk '/Cpus_allowed_list/ {print $2}' "$task/status"; done; } | sort -u | grep -v "^${SERVER_CPUS}$" || true)"
  [[ -z "$unexpected_masks" ]] || { echo "Not all Java threads use $SERVER_CPUS" >&2; exit 1; }

  smoke_status="$(curl --silent --show-error --max-time 3 --output /dev/null --write-out '%{http_code}' "$TARGET_URL")"
  [[ "$smoke_status" == "$EXPECTED_STATUS" ]] || { echo "Smoke HTTP $smoke_status" >&2; exit 1; }

  attack_common=(
    -targets="$TARGET_FILE"
    -rate="${RATE_RPS}/s"
    -workers="$WORKERS"
    -connections="$CONNECTIONS"
    -keepalive="$KEEPALIVE"
    -http2="$HTTP2"
  )
  [[ "$CHUNKED" != true ]] || attack_common+=(-chunked)

  started_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  java_executable="$(readlink -f "/proc/$server_pid/exe")"
  java_version="$($java_executable -version 2>&1 | head -n 1)"
  sockets_before="$(ss -s | tr '\n' ';')"

  cat > "$run_dir/metadata.env" <<EOF
STARTED_AT_UTC=$started_at
GIT_COMMIT=$(git rev-parse HEAD)
GIT_TAG=$(git tag --points-at HEAD | paste -sd, -)
GIT_DIRTY=false
HOST_ID=$(hostname)
KERNEL=$(uname -srmo)
JAVA_EXECUTABLE=$java_executable
JAVA_VERSION=$java_version
VEGETA_VERSION=$($vegeta_bin --version | head -n 1)
PROFILE_NAME=$profile_name
PROFILE_ID=$PROFILE_ID
PROFILE_SHA256=$profile_sha
REQUEST_ID=$REQUEST_ID
TARGET_URL=$TARGET_URL
TARGET_SHA256=$target_sha
BODY_BYTES=$BODY_BYTES
BODY_SHA256=$body_sha
CHUNKED=$CHUNKED
EXPECTED_STATUS=$EXPECTED_STATUS
RATE_RPS=$RATE_RPS
DURATION=$DURATION
WARMUP_DURATION=$WARMUP_DURATION
WORKERS=$WORKERS
CONNECTIONS=$CONNECTIONS
KEEPALIVE=$KEEPALIVE
HTTP2=$HTTP2
SERVER_PID=$server_pid
SERVER_CPUS=$SERVER_CPUS
VEGETA_CPUS=$VEGETA_CPUS
NGINX_CONFIG=$nginx_config
NGINX_CONFIG_SHA256=$nginx_sha
NGINX_CPUS=shared
ULIMIT_NOFILE=$(ulimit -n)
SOCKETS_BEFORE=$sockets_before
VALID=pending
EOF

  echo "[$run_index/$runs] warm-up $PROFILE_ID"
  taskset -c "$VEGETA_CPUS" "$vegeta_bin" attack \
    -name="${REQUEST_ID}_warmup" -duration="$WARMUP_DURATION" \
    -output="$run_dir/warmup.gob" "${attack_common[@]}"
  "$vegeta_bin" report -type=json -output="$run_dir/warmup-report.json" "$run_dir/warmup.gob"
  warmup_unexpected="$(jq -r --arg expected "$EXPECTED_STATUS" \
    '.status_codes | to_entries | map(select(.key != $expected) | .value) | add // 0' \
    "$run_dir/warmup-report.json")"
  if ! jq -e '.success == 1' "$run_dir/warmup-report.json" >/dev/null || [[ "$warmup_unexpected" -ne 0 ]]; then
    echo "Warm-up failed; artifacts saved at $run_dir" >&2
    exit 1
  fi

  echo "[$run_index/$runs] measured $PROFILE_ID"
  taskset -c "$VEGETA_CPUS" "$vegeta_bin" attack \
    -name="$REQUEST_ID" -duration="$DURATION" \
    -output="$run_dir/results.gob" "${attack_common[@]}"

  "$vegeta_bin" encode -to=csv -output="$run_dir/samples.csv" "$run_dir/results.gob"
  "$vegeta_bin" report -type=json -output="$run_dir/report.json" "$run_dir/results.gob"

  sample_n="$(wc -l < "$run_dir/samples.csv")"
  success_ratio="$(jq -r '.success' "$run_dir/report.json")"
  unexpected_statuses="$(jq -r --arg expected "$EXPECTED_STATUS" \
    '.status_codes | to_entries | map(select(.key != $expected) | .value) | add // 0' \
    "$run_dir/report.json")"
  valid=false
  if jq -e '.success == 1' "$run_dir/report.json" >/dev/null && [[ "$unexpected_statuses" -eq 0 ]]; then
    valid=true
  fi

  cat >> "$run_dir/metadata.env" <<EOF
SOCKETS_AFTER=$(ss -s | tr '\n' ';')
SAMPLE_N=$sample_n
SUCCESS_RATIO=$success_ratio
VALID=$valid
EOF

  "$script_dir/stop_server.sh"
  cp "$server_log" "$run_dir/server.log"
  active_run_dir=""
  [[ "$valid" == true ]] || { echo "Invalid run saved at $run_dir" >&2; exit 1; }
done

trap - EXIT
"$script_dir/summarize_series.py" "$series_dir"
echo "Series complete: $series_dir"
