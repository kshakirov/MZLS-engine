# Development / Разработка

[English](#english) · [Русский](#русский)

## English

MZLS-engine uses small executable research stands in `src/test/java`. They are
ordinary classes with `main` methods and Java assertions, not a conventional
unit-test suite.

Current stands:

- `TestWirthHttpParser` — header-parser experiments;
- `TestHttpBodyParserAutomaton` — fixed-length and chunked body transitions;
- `TestHttpRequestParser` — dispatcher and complete logical request flow;
- `TestBitRoute` — binary route tape and candidate matching.

Run a stand with assertions enabled:

```bash
MAVEN_OPTS='-ea' mvn -q test-compile exec:java \
  -Dexec.mainClass='nano.engine.kernel.TestBitRoute' \
  -Dexec.classpathScope='test'
```

The live socket stand starts from `nano.engine.Main` and binds
`/tmp/.jbc_socket`. Performance and profiling have separate canonical
instructions linked from [Performance](Performance).

## Русский

MZLS-engine использует небольшие исполняемые исследовательские стенды в
`src/test/java`. Это обычные классы с методами `main` и Java assertions, а не
традиционный набор unit-тестов.

Текущие стенды:

- `TestWirthHttpParser` — эксперименты с парсером заголовков;
- `TestHttpBodyParserAutomaton` — переходы fixed-length и chunked тела;
- `TestHttpRequestParser` — диспетчер и полный логический запрос;
- `TestBitRoute` — бинарная лента маршрутов и поиск кандидатов.

Запуск стенда с включёнными assertions:

```bash
MAVEN_OPTS='-ea' mvn -q test-compile exec:java \
  -Dexec.mainClass='nano.engine.kernel.TestBitRoute' \
  -Dexec.classpathScope='test'
```

Живой сокетный стенд запускается из `nano.engine.Main` и привязывается к
`/tmp/.jbc_socket`. Для performance и profiling действуют отдельные
канонические инструкции со страницы [Performance](Performance).
