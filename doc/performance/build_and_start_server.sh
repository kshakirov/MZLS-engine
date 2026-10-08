#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir="$(cd "$script_dir/../.." && pwd)"
cd "$project_dir"

java_home="${MZLS_JAVA_HOME:-$HOME/opt/graalvm-jdk-21}"
maven_bin="${MZLS_MAVEN_BIN:-$HOME/opt/apache-maven-3.9.16/bin/mvn}"
socket_path="${SOCKET_PATH:-/tmp/.jbc_socket}"
runtime_dir="${RUNTIME_DIR:-var/runtime}"
pid_file="$runtime_dir/server.pid"
log_file="$runtime_dir/server.log"

[[ -x "$java_home/bin/java" ]] || { echo "Java not found: $java_home/bin/java" >&2; exit 1; }
[[ -x "$maven_bin" ]] || { echo "Maven not found: $maven_bin" >&2; exit 1; }

if [[ -f "$pid_file" ]]; then
  old_pid="$(<"$pid_file")"
  if [[ "$old_pid" =~ ^[1-9][0-9]*$ && -d "/proc/$old_pid" ]]; then
    echo "Server is already running: PID $old_pid" >&2
    exit 1
  fi
fi

mkdir -p "$runtime_dir"

echo "Building MZLS-engine with $java_home"
JAVA_HOME="$java_home" "$maven_bin" -q package

echo "Starting nano.engine.Main"
nohup "$java_home/bin/java" -cp target/classes nano.engine.Main \
  </dev/null >"$log_file" 2>&1 &
server_pid=$!
printf '%s\n' "$server_pid" > "$pid_file"

cleanup_failed_start() {
  if kill -0 "$server_pid" 2>/dev/null; then
    kill "$server_pid" 2>/dev/null || true
  fi
}
trap cleanup_failed_start EXIT

ready=false
for _ in {1..50}; do
  if ! kill -0 "$server_pid" 2>/dev/null; then
    echo "Server exited during startup; see $log_file" >&2
    exit 1
  fi
  if [[ -S "$socket_path" ]] && curl --unix-socket "$socket_path" \
      --fail --silent --show-error --max-time 1 --output /dev/null \
      http://localhost/index.html; then
    ready=true
    break
  fi
  sleep 0.1
done

if [[ "$ready" != true ]]; then
  echo "Server did not become ready at $socket_path; see $log_file" >&2
  exit 1
fi

trap - EXIT

echo "MZLS-engine is ready"
echo "PID: $server_pid"
echo "Socket: $socket_path"
echo "Log: $log_file"
