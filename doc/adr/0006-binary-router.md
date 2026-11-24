# ADR-0006 — Binary Route Tape and Frozen Symbols / Бинарная лента маршрутов и замороженные символы

Status: Accepted

## English

### Context

The router must register parameterised HTTP paths and later match raw request
bytes without trees, regular expressions, reflection, or an object per segment
in the lookup path. A shortened segment hash is useful as a fast filter but is
not sufficient proof of literal equality because collisions are possible.

Registration and lookup have different memory requirements. Registration may
use mutable builders; lookup should operate on compact, stable primitive
arrays.

### Decision

Parameterised route templates are appended to one binary `byte[]` tape by
linear allocation. `nextFreeOffset` identifies the first unused byte. Each
record contains a 12-byte header followed by five bytes per segment: one mask
byte and four bytes of hash storage.

Candidate lookup proceeds in two stages:

1. Filter records by segment count and compare segment hashes under the route
   mask.
2. Return every matching record offset for later exact literal verification,
   parameter extraction, and conflict resolution.

A literal segment uses mask `0xFF`; a parameter segment uses mask `0x00`.
Therefore the candidate predicate is:

```java
(routeMask & requestHash) == (routeMask & templateHash)
```

Original template symbols are built during registration and then frozen into
`byte[][][] compiledSymbols`. Its dimensions are route, segment, and byte. A
literal stores its original bytes; a parameter stores the one-byte marker
`'{'`. The route index in `compiledSymbols` must correspond to the record order
in the binary tape.

The shortened hash is only a candidate filter. A route cannot be accepted
until its literal bytes have been compared exactly with the corresponding
half-open ranges `[startInclusive, endExclusive)` in the original request path.

### Rationale

The binary tape provides sequential traversal and compact append-only
registration. The mask makes literal and parameter segments share one
comparison operation. Freezing the symbol builder removes boxed collections
from the lookup representation while retaining the source bytes required to
reject hash collisions.

### Consequences

- Lookup can operate on primitive arrays, indices, and offsets.
- Registration may allocate temporary boxed collections; they are not the
  runtime lookup representation.
- Hash matches produce candidates, not final matches.
- Candidate precedence and parameter delivery remain separate decisions.
- Registration must finish and symbols must be frozen before lookup.
- The current binary layout may require a superseding ADR if its header or hash
  width changes.

## Русский

### Контекст

Маршрутизатор должен регистрировать параметрические HTTP-пути и затем
сопоставлять сырые байты запроса без деревьев, регулярных выражений, рефлексии
и отдельного объекта на каждый сегмент в горячем пути. Сокращённый хэш сегмента
пригоден как быстрый фильтр, но не доказывает равенство литералов из-за
возможных коллизий.

Регистрация и поиск предъявляют разные требования к памяти. При регистрации
допустим изменяемый построитель, а поиск должен работать с компактными и
неизменяемыми массивами примитивов.

### Решение

Шаблоны параметрических маршрутов последовательно добавляются в одну бинарную
ленту `byte[]` посредством линейного выделения памяти. `nextFreeOffset`
указывает на первый свободный байт. Каждая запись содержит 12-байтовый заголовок
и пять байтов на сегмент: один байт маски и четыре байта поля хэша.

Поиск кандидатов выполняется в два этапа:

1. Записи фильтруются по количеству сегментов, затем хэши сегментов
   сравниваются под маской маршрута.
2. Возвращаются смещения всех совпавших записей для последующей точной проверки
   литералов, извлечения параметров и разрешения конфликтов.

Маска литерального сегмента равна `0xFF`, маска параметра — `0x00`. Поэтому
предикат отбора кандидата имеет вид:

```java
(routeMask & requestHash) == (routeMask & templateHash)
```

Исходные символы шаблонов строятся при регистрации, а затем замораживаются в
`byte[][][] compiledSymbols`. Измерения массива соответствуют маршруту,
сегменту и байту. Литерал хранит исходные байты, параметр — однобайтовый маркер
`'{'`. Индекс маршрута в `compiledSymbols` должен соответствовать порядку
записей в бинарной ленте.

Сокращённый хэш служит только фильтром кандидатов. Маршрут нельзя признать
совпавшим, пока его литералы не сравнены побайтово с соответствующими
полуинтервалами `[startInclusive, endExclusive)` исходного пути запроса.

### Обоснование

Бинарная лента даёт последовательный обход и компактную дописываемую
регистрацию. Маска позволяет проверять литералы и параметры одной операцией.
Заморозка построителя символов убирает коллекции с boxed-значениями из
представления для поиска, сохраняя исходные байты, необходимые для отбраковки
хэш-коллизий.

### Последствия

- Поиск может работать только с массивами примитивов, индексами и смещениями.
- При регистрации допустимы временные boxed-коллекции; они не входят в
  представление горячего пути.
- Совпадение хэшей даёт кандидата, а не окончательное совпадение.
- Приоритет кандидатов и передача параметров остаются отдельными решениями.
- До начала поиска регистрация должна завершиться, а символы — заморозиться.
- Изменение заголовка записи или ширины хэша может потребовать нового ADR,
  явно заменяющего это решение.
