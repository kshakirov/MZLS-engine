# Profiling

JFR использует те же `get`, `fixed` и `chunked` профили, что baseline. Для
профайлинга выполняется один прогретый запуск. Его latency нельзя принимать за
performance baseline.

## 1. Подготовить PIMS

Выполнить шаг 1 из [performance README](../performance/README.md).

## 2. Запустить профиль

Запускать по одной команде:

```bash
doc/profiling/run_jfr.sh get
doc/profiling/run_jfr.sh fixed
doc/profiling/run_jfr.sh chunked
```

## 3. Прочитать результат

Каталог результата печатается в конце. В нём находятся:

```text
recording.jfr
jfr-summary.txt
hot-methods.txt
allocation-by-site.txt
pinned-threads.txt
thread-cpu-load.txt
load/...
```

Вывод и решение по нему кратко записать в [results.md](results.md). Сырые файлы
остаются в `var/profiling/` и в Git не входят.

> Chunked запускать только после проверки framing через Nginx, указанной в
> performance README.
