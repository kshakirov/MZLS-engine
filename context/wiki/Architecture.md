# Architecture / Архитектура

[English](#english) · [Русский](#русский)

## English

This section describes the system that can be traced to current source code.
It is not a roadmap and does not turn an experiment into an accepted design.

### Current components

- [HTTP Pipeline](Architecture-HTTP-Pipeline) — Unix-domain socket server,
  request dispatcher, header parser, and body automaton.
- [Binary Router](Architecture-Binary-Router) — an experimental compact route
  tape and candidate matcher. It is not integrated into the HTTP pipeline yet.

### Architectural boundary

`Main` starts `TestSocketServer` on `/tmp/.jbc_socket`. Each accepted connection
gets its own `HttpRequestParser`. That dispatcher feeds bytes first to
`WirthHttpParser` and, when a body is present, to
`HttpBodyParserAutomaton`. The router is currently exercised by an independent
manual test stand.

## Русский

Этот раздел описывает систему, которую можно проследить до текущего исходного
кода. Это не дорожная карта: эксперимент не становится принятым решением только
потому, что упомянут в документации.

### Текущие компоненты

- [HTTP Pipeline](Architecture-HTTP-Pipeline) — сервер на Unix domain socket,
  диспетчер запроса, парсер заголовков и автомат тела.
- [Binary Router](Architecture-Binary-Router) — экспериментальная плотная лента
  маршрутов и поиск кандидатов. В HTTP-конвейер пока не встроен.

### Архитектурная граница

`Main` запускает `TestSocketServer` на `/tmp/.jbc_socket`. Каждое принятое
соединение получает собственный `HttpRequestParser`. Диспетчер сначала передаёт
байты в `WirthHttpParser`, а при наличии тела — в
`HttpBodyParserAutomaton`. Маршрутизатор пока проверяется отдельным ручным
стендом.
