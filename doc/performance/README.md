# MZLS-engine performance protocol

Этот каталог фиксирует воспроизводимый протокол нагрузочных измерений. Сначала
снимается baseline, затем меняется ровно один исследуемый фактор и повторяется та
же серия.

Документы и скрипты хранятся в Git. Сырые результаты сохраняются локально в
`var/performance/` и не должны молча удаляться, включая неудачные прогоны.

## Текущий GET-профиль

```text
request_id:     get_index_empty
target:         GET http://127.0.0.1:18080/index.html
transport:      Vegeta -> Nginx -> Unix socket -> MZLS-engine
rate:           20000 requests/s
duration:       10 s
warm-up:        5 s
expected:       HTTP 200
MZLS CPUs:      4,10 (одно физическое ядро с SMT)
Vegeta CPUs:    5,11 (другое физическое ядро с SMT)
Nginx CPUs:     общая маска машины
keep-alive:     true между Vegeta и Nginx
HTTP/2:         false
```

Профиль измеряет весь локальный тракт, а не только Java-парсер. Изменение rate,
duration, affinity, keep-alive, Nginx или request template создаёт другой профиль.

## Один независимый прогон

Сервер должен быть уже запущен, а его PID записан в `var/runtime/server.pid`.

```bash
doc/performance/run_get.sh 20000 10s 001
```

Параметры: `rate_rps`, `duration`, `run_index`. Значения можно переопределять
переменными окружения, перечисленными в начале скрипта.

Результат:

```text
var/performance/raw/YYYY-MM-DD/get_index_empty/
└── affinity-allthreads-20000rps-10s/
    └── run-001/
        ├── metadata.env
        ├── results.gob
        ├── samples.csv
        └── report.json
```

`results.gob` — исходный результат Vegeta, `samples.csv` — одна строка на запрос,
`report.json` — быстрые агрегаты, `metadata.env` — условия прогона. Существующий
каталог скрипт не перезаписывает.

Перед сравнительной серией Storm workers останавливаются. Служебные процессы и
общий Nginx остаются частью среды и фиксируются в metadata. `taskset` ограничивает
MZLS и Vegeta, но не делает CPU эксклюзивными для них.

## Сводка серии

После нескольких прогонов одного профиля:

```bash
doc/performance/summarize_get.sh \
  var/performance/raw/2026-10-08/get_index_empty/affinity-allthreads-20000rps-10s
```

Скрипт создаёт `runs-summary.csv`. Строка соответствует одному прогону. Latency
и выборочное стандартное отклонение считаются по `samples.csv`; throughput,
success ratio и коды ответов берутся из `report.json`.

Прогон с ошибками сохраняется и получает `valid=false`. Нельзя объединять такие
прогоны с чистыми как измерения одной операции.
