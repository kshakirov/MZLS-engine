# Performance

Три независимых профиля: GET без тела, fixed-length с телом 16 B и chunked с
тем же телом 16 B. Каждый профиль: прогрев 5 s, затем 5 запусков по 10 s при
20 000 req/s. Между запусками JVM перезапускается.

## 1. Подготовить PIMS

```bash
cd ~/repos/MZLS-engine
git switch master
git pull --ff-only origin master
storm list
systemctl is-active nginx
```

В `storm list` не должно быть активных топологий. Демоны Storm и Nginx не
останавливать.

## 2. Запустить серию

Запускать по одной команде:

```bash
doc/performance/run_series.sh get
doc/performance/run_series.sh fixed
doc/performance/run_series.sh chunked
```

Сырые данные и сводка появятся здесь:

```text
var/performance/raw/YYYY-MM-DD/<request-id>/<profile-id>/<timestamp>/
├── run-001/ ... run-005/
├── runs-summary.csv
└── combined-summary.json
```

Если серия оборвалась, её каталог сохраняется. Не смешивать его с новым
прогоном.

## 3. Принять baseline

После проверки `combined-summary.json` скопировать его в Git:

```bash
cp var/performance/raw/.../combined-summary.json \
  doc/performance/baselines/<profile-id>.json
```

## 4. Сравнить новый прогон с baseline

```bash
doc/performance/compare_baseline.py \
  doc/performance/baselines/<profile-id>.json \
  var/performance/raw/.../combined-summary.json
```

Профили должны совпасть. Различия описаны в [requests.md](requests.md), поля
результатов — в [schema.md](schema.md), машина — в
[machines/pmd-pims-aio.md](machines/pmd-pims-aio.md).

## 5. Остановить сервак вручную

Обычно раннер делает это сам. После ручного запуска:

```bash
doc/performance/stop_server.sh
```

Профайлинг запускается отдельно по [инструкции](../profiling/README.md).

> Chunked baseline принимать только после проверки, что Nginx передаёт в Unix
> socket именно chunked framing, а не заменяет его на `Content-Length`.
