# HTTP Pipeline / HTTP-конвейер

Status: **Experimental**  
Last reviewed: `2026-11-24`, branch `issue-router`, commit `611a0f3`

[English](#english) · [Русский](#русский)

## English

### Live path

```text
client or load generator
        ↓
reverse proxy
        ↓
/tmp/.jbc_socket
        ↓
TestSocketServer
        ↓
HttpRequestParser
        ├── WirthHttpParser
        └── HttpBodyParserAutomaton
```

`TestSocketServer` accepts blocking Unix-domain socket connections and runs one
connection handler per virtual thread. A handler reads fragments into a
1,024-byte `ByteBuffer` and owns one `HttpRequestParser` instance.

### Request phases

1. `HttpRequestParser` feeds header bytes one by one to `WirthHttpParser`.
2. The header parser classifies the request body as `NONE`, `FIXED_CONTENT`, or
   `CHUNK_CONTENT`.
3. With no body, the dispatcher returns `FINISH`.
4. With a body, the dispatcher creates `HttpBodyParserAutomaton` in either
   `READ_FIXED_BODY` or `READ_CHUNK_SIZE`.
5. The body automaton consumes one offered byte per `runEngine(byte)` call and
   reaches `SUCCESS` or `ERROR`.
6. The server writes a complete HTTP response and closes the channel.

### Current contracts

- Dispatcher result: `FINISH`, `NEEDS_MORE_DATA`, or `ERROR`.
- One parser instance holds the state of one logical request.
- Fixed-length and chunked bodies share the same body automaton.
- The current arena is allocated when the body automaton is created.
- The Java chunked-body state machine has a corresponding nuXmv model in
  `nuxmv/body_parse_automaton_ver_5.smv`.

### Known experimental constraints

- The socket stand allocates a new byte array for every read fragment.
- It also accumulates received bytes in a `ByteArrayOutputStream` that is not
  yet part of the parsing contract.
- Request pipelining and multiple logical requests per connection are not yet
  implemented.
- Lifecycle, capacity checks, error visibility, and arena policy still require
  dedicated decisions.
- `SimpleHttpReqParser` is legacy experimental code and does not define the
  architecture.

## Русский

### Живой тракт

```text
клиент или генератор нагрузки
        ↓
обратный прокси
        ↓
/tmp/.jbc_socket
        ↓
TestSocketServer
        ↓
HttpRequestParser
        ├── WirthHttpParser
        └── HttpBodyParserAutomaton
```

`TestSocketServer` принимает блокирующие соединения через Unix domain socket и
запускает обработчик каждого соединения в отдельном virtual thread. Обработчик
читает фрагменты через `ByteBuffer` размером 1 024 байта и владеет одним
экземпляром `HttpRequestParser`.

### Фазы запроса

1. `HttpRequestParser` побайтово передаёт заголовок в `WirthHttpParser`.
2. Парсер заголовка классифицирует тело как `NONE`, `FIXED_CONTENT` или
   `CHUNK_CONTENT`.
3. Если тела нет, диспетчер возвращает `FINISH`.
4. При наличии тела создаётся `HttpBodyParserAutomaton` в состоянии
   `READ_FIXED_BODY` или `READ_CHUNK_SIZE`.
5. Один вызов `runEngine(byte)` принимает один предложенный байт и переводит
   автомат к `SUCCESS` либо `ERROR`.
6. Сервер полностью записывает HTTP-ответ и закрывает канал.

### Текущие контракты

- Результат диспетчера: `FINISH`, `NEEDS_MORE_DATA` или `ERROR`.
- Один экземпляр парсера хранит состояние одного логического запроса.
- Тела fixed-length и chunked обрабатывает общий автомат тела.
- Текущая арена выделяется при создании автомата тела.
- Автомату chunked-тела соответствует модель
  `nuxmv/body_parse_automaton_ver_5.smv`.

### Известные ограничения полигона

- Сокетный стенд создаёт новый массив байт для каждого прочитанного фрагмента.
- Принятые байты дополнительно накапливаются в `ByteArrayOutputStream`, который
  пока не участвует в контракте разбора.
- HTTP pipelining и несколько логических запросов в одном соединении ещё не
  реализованы.
- Жизненный цикл, проверки ёмкости, видимость ошибок и политика арены требуют
  отдельных решений.
- `SimpleHttpReqParser` — старый экспериментальный код и архитектуру не
  определяет.
