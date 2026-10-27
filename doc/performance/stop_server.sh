#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "$script_dir/../.." && pwd)"
cd "$project_dir"

pid_file="${SERVER_PID_FILE:-var/runtime/server.pid}"
[[ -f "$pid_file" ]] || { echo "Server is not running"; exit 0; }

pid="$(<"$pid_file")"
[[ "$pid" =~ ^[1-9][0-9]*$ ]] || { echo "Invalid PID file: $pid_file" >&2; exit 1; }
if [[ ! -r "/proc/$pid/cmdline" ]]; then
  rm -f "$pid_file"
  echo "Removed stale PID file"
  exit 0
fi

command_line="$(tr '\0' ' ' < "/proc/$pid/cmdline")"
[[ "$command_line" == *nano.engine.Main* ]] || {
  echo "PID $pid is not nano.engine.Main; refusing to stop it" >&2
  exit 1
}

kill "$pid"
for _ in {1..50}; do
  if ! kill -0 "$pid" 2>/dev/null; then
    rm -f "$pid_file"
    echo "Stopped MZLS-engine PID $pid"
    exit 0
  fi
  sleep 0.1
done

kill -KILL "$pid"
rm -f "$pid_file"
echo "Killed unresponsive MZLS-engine PID $pid"
