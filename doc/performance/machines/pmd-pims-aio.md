# pmd-pims-aio

- сервер: `pims.pmdistributing.com`;
- Java: `~/opt/graalvm-jdk-21`;
- Maven: `~/opt/apache-maven-3.9.16/bin/mvn`;
- Vegeta: `~/.local/bin/vegeta`;
- тракт: Vegeta → Nginx `127.0.0.1:18080` → `/tmp/.jbc_socket` → MZLS-engine;
- MZLS affinity: CPU `4,10` — одна SMT-пара;
- Vegeta affinity: CPU `5,11` — другая SMT-пара;
- Nginx и демоны Storm остаются в общей маске;
- активных Storm-топологий во время измерения быть не должно.

Конкретные версии, ядро, Nginx config hash и состояние сокетов пишутся в
`metadata.env` каждого запуска.
