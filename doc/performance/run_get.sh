#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "$script_dir/../.." && pwd)"
cd "$project_dir"

rate_rps="${1:-20000}"
duration="${2:-10s}"
run_index="${3:-001}"

request_id="get_index_empty"
target_file="${TARGET_FILE:-doc/performance/requests/get_index_empty.targets}"
target_url="${TARGET_URL:-http://127.0.0.1:18080/index.html}"
expected_status="${EXPECTED_STATUS:-200}"
warmup_duration="${WARMUP_DURATION:-5s}"
server_pid_file="${SERVER_PID_FILE:-var/runtime/server.pid}"
server_cpus="${SERVER_CPUS:-4,10}"
vegeta_cpus="${VEGETA_CPUS:-5,11}"
vegeta_bin="${VEGETA_BIN:-$HOME/.local/bin/vegeta}"
workers="${WORKERS:-10}"
connections="${CONNECTIONS:-10000}"
keepalive="${KEEPALIVE:-true}"
http2="${HTTP2:-false}"

[[ "$rate_rps" =~ ^[1-9][0-9]*$ ]] || { echo "rate_rps must be a positive integer" >&2; exit 2; }
[[ "$duration" =~ ^[1-9][0-9]*(ms|s|m|h)$ ]] || { echo "duration must look like 10s" >&2; exit 2; }
[[ "$run_index" =~ ^[0-9]{3}$ ]] || { echo "run_index must contain three digits" >&2; exit 2; }
[[ -x "$vegeta_bin" ]] || { echo "Vegeta not found: $vegeta_bin" >&2; exit 1; }
[[ -f "$target_file" ]] || { echo "Target file not found: $target_file" >&2; exit 1; }
[[ -f "$server_pid_file" ]] || { echo "Server PID file not found: $server_pid_file" >&2; exit 1; }

server_pid="$(<"$server_pid_file")"
[[ "$server_pid" =~ ^[1-9][0-9]*$ ]] || { echo "Invalid server PID" >&2; exit 1; }
[[ -r "/proc/$server_pid/cmdline" ]] || { echo "MZLS process is not running" >&2; exit 1; }
server_command="$(tr '\0' ' ' < "/proc/$server_pid/cmdline")"
[[ "$server_command" == *nano.engine.Main* ]] || { echo "PID $server_pid is not MZLS-engine" >&2; exit 1; }

smoke_status="$(curl --silent --show-error --max-time 3 --output /dev/null --write-out '%{http_code}' "$target_url")"
[[ "$smoke_status" == "$expected_status" ]] || { echo "Smoke test returned HTTP $smoke_status" >&2; exit 1; }

taskset -apc "$server_cpus" "$server_pid" >/dev/null
unexpected_masks="$({ for task in "/proc/$server_pid"/task/*; do awk '/Cpus_allowed_list/ {print $2}' "$task/status"; done; } | sort -u | grep -v "^${server_cpus}$" || true)"
[[ -z "$unexpected_masks" ]] || { echo "Not all Java threads use CPU mask $server_cpus" >&2; exit 1; }

date_utc="$(date -u +%F)"
profile="${PROFILE:-affinity-allthreads-${rate_rps}rps-${duration}}"
run_dir="var/performance/raw/$date_utc/$request_id/$profile/run-$run_index"
[[ ! -e "$run_dir" ]] || { echo "Run already exists: $run_dir" >&2; exit 1; }
mkdir -p "$run_dir"

git_dirty=false
[[ -z "$(git status --porcelain --untracked-files=no)" ]] || git_dirty=true
java_executable="$(readlink -f "/proc/$server_pid/exe")"
vegeta_version="$($vegeta_bin --version | head -n 1)"
started_at_utc="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

cat > "$run_dir/metadata.env" <<EOF
STARTED_AT_UTC=$started_at_utc
GIT_COMMIT=$(git rev-parse HEAD)
GIT_TAG=$(git tag --points-at HEAD | paste -sd, -)
GIT_DIRTY=$git_dirty
HOST_ID=$(hostname)
KERNEL=$(uname -srmo)
JAVA_EXECUTABLE=$java_executable
VEGETA_VERSION=$vegeta_version
REQUEST_ID=$request_id
TARGET_URL=$target_url
EXPECTED_STATUS=$expected_status
RATE_RPS=$rate_rps
DURATION=$duration
WARMUP_DURATION=$warmup_duration
WORKERS=$workers
CONNECTIONS=$connections
KEEPALIVE=$keepalive
HTTP2=$http2
SERVER_PID=$server_pid
SERVER_CPUS=$server_cpus
VEGETA_CPUS=$vegeta_cpus
NGINX_CPUS=shared
ULIMIT_NOFILE=$(ulimit -n)
SOCKETS_BEFORE=$(ss -s | tr '\n' ';')
VALID=pending
EOF

echo "Warm-up: $warmup_duration at $rate_rps requests/s"
taskset -c "$vegeta_cpus" "$vegeta_bin" attack \
  -name="${request_id}_warmup" \
  -targets="$target_file" \
  -rate="${rate_rps}/s" \
  -duration="$warmup_duration" \
  -workers="$workers" \
  -connections="$connections" \
  -keepalive="$keepalive" \
  -http2="$http2" \
  -output=/dev/null

echo "Measured run: $run_dir"
taskset -c "$vegeta_cpus" "$vegeta_bin" attack \
  -name="$request_id" \
  -targets="$target_file" \
  -rate="${rate_rps}/s" \
  -duration="$duration" \
  -workers="$workers" \
  -connections="$connections" \
  -keepalive="$keepalive" \
  -http2="$http2" \
  -output="$run_dir/results.gob"

"$vegeta_bin" encode -to=csv -output="$run_dir/samples.csv" "$run_dir/results.gob"
"$vegeta_bin" report -type=json -output="$run_dir/report.json" "$run_dir/results.gob"

sample_n="$(wc -l < "$run_dir/samples.csv")"
success_ratio="$(jq -r '.success' "$run_dir/report.json")"
unexpected_statuses="$(jq -r --arg expected "$expected_status" '.status_codes | to_entries | map(select(.key != $expected)) | map(.value) | add // 0' "$run_dir/report.json")"
valid=true
[[ "$unexpected_statuses" == 0 ]] || valid=false

cat >> "$run_dir/metadata.env" <<EOF
SAMPLE_N=$sample_n
SUCCESS_RATIO=$success_ratio
SOCKETS_AFTER=$(ss -s | tr '\n' ';')
VALID=$valid
EOF

jq . "$run_dir/report.json"
echo "sample_n=$sample_n valid=$valid"
[[ "$valid" == true ]]
