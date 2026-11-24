# Performance / Производительность

[English](#english) · [Русский](#русский)

## English

Performance claims are accepted only with a reproducible profile, raw results,
machine context, and an explicit validity check.

The repository currently defines three independent HTTP profiles:

- GET without a request body;
- fixed-length request body, 16 bytes;
- chunked request body carrying the same 16 bytes.

The canonical procedures live with the code:

- [Performance workflow](https://github.com/kshakirov/MZLS-engine/blob/master/doc/performance/README.md)
- [Request definitions](https://github.com/kshakirov/MZLS-engine/blob/master/doc/performance/requests.md)
- [Result schema](https://github.com/kshakirov/MZLS-engine/blob/master/doc/performance/schema.md)
- [JFR profiling](https://github.com/kshakirov/MZLS-engine/blob/master/doc/profiling/README.md)

Latency from a JFR profiling run is diagnostic data and is not a performance
baseline. Chunked results are valid only after verifying that the proxy
preserves chunked framing.

## Русский

Утверждение о производительности принимается только вместе с воспроизводимым
профилем, сырыми результатами, описанием машины и явной проверкой валидности.

В репозитории определены три независимых HTTP-профиля:

- GET без тела запроса;
- fixed-length с телом 16 байт;
- chunked с теми же 16 байтами данных.

Канонические процедуры хранятся рядом с кодом:

- [Запуск замеров](https://github.com/kshakirov/MZLS-engine/blob/master/doc/performance/README.md)
- [Определения запросов](https://github.com/kshakirov/MZLS-engine/blob/master/doc/performance/requests.md)
- [Схема результатов](https://github.com/kshakirov/MZLS-engine/blob/master/doc/performance/schema.md)
- [JFR-профилирование](https://github.com/kshakirov/MZLS-engine/blob/master/doc/profiling/README.md)

Latency из JFR-прогона служит диагностикой и не является performance baseline.
Результаты chunked валидны только после проверки, что прокси сохраняет chunked
framing.
