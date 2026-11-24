# Binary Router / Бинарный маршрутизатор

Status: **Experimental**  
Last reviewed: `2026-11-24`, branch `issue-router`

[English](#english) · [Русский](#русский)

## English

### Purpose

`BitRoute` explores route registration and matching without trees, regular
expressions, or one object per path segment. It is currently independent from
the HTTP pipeline.

### Route tape

Routes are appended to one `byte[]` by linear allocation. `nextFreeOffset`
points to the first unused byte. A record starts at that offset and advances the
cursor by its complete size.

Current record layout:

| Relative range | Size | Meaning |
|---|---:|---|
| `[0, 2)` | 2 | record size, currently written big-endian |
| `[2, 3)` | 1 | segment count |
| `[3, 12)` | 9 | reserved header bytes |
| `[12 + 5i, 13 + 5i)` | 1 | segment mask |
| `[13 + 5i, 17 + 5i)` | 4 | segment hash storage |

The complete record size is:

```text
12 + 5 × segmentCount
```

A literal mask is `0xFF`, which Java sign-extends to `-1`; a parameter mask is
`0x00`. The current hash function computes FNV-1a and retains its low 16 bits,
although the value is stored in a four-byte field.

### Candidate matching

The request URL is encoded into the same segment representation. Compiled route
records are first filtered by segment count. Corresponding hashes are then
masked and compared:

```java
(routeMask & requestHash) == (routeMask & templateHash)
```

For a literal, `routeMask == -1` preserves both hashes. For a parameter,
`routeMask == 0` maps both sides to zero. The decoder collects record offsets of
all candidates instead of selecting a handler immediately. Conflict resolution
and parameter extraction are deliberately later stages.

### Frozen symbols

Registration also builds the original template symbols. Before lookup they are
frozen into `byte[][][] compiledSymbols`, indexed by route, segment, and byte.
A literal segment retains its original bytes; a parameter segment is represented
by the one-byte marker `'{'`. Record order in the route tape and route order in
`compiledSymbols` form one shared index space.

This representation is intended for the exact pass after hash filtering. It
rejects hash collisions without materialising request segments: literals are
compared directly with half-open ranges in the original request path, while a
parameter marker records or skips the corresponding range.

### Verified by the current manual stand

- Multiple records are appended without overwriting earlier records.
- Records ending with a parameter are closed correctly.
- A request can match a parameterised record.
- Non-matching records remain marked with offset `-1`.

### Open work

- Preserve request segment boundaries and values for parameter extraction.
- Complete and test the exact literal pass over `compiledSymbols`.
- Resolve overlapping parameterised candidates.
- Store and return the selected handler identifier.
- Read the full unsigned record size and scan only the used tape region.
- Add capacity growth and define decoder reuse.

## Русский

### Назначение

`BitRoute` исследует регистрацию и поиск маршрутов без деревьев, регулярных
выражений и отдельного объекта на каждый сегмент пути. В HTTP-конвейер он пока
не встроен.

### Лента маршрутов

Маршруты последовательно записываются в один `byte[]` посредством линейного
выделения памяти. `nextFreeOffset` указывает на первый свободный байт. Новая
запись начинается с этого смещения и после завершения сдвигает курсор на свой
полный размер.

Текущая раскладка записи:

| Относительный полуинтервал | Размер | Значение |
|---|---:|---|
| `[0, 2)` | 2 | размер записи, сейчас big-endian |
| `[2, 3)` | 1 | количество сегментов |
| `[3, 12)` | 9 | зарезервированные байты заголовка |
| `[12 + 5i, 13 + 5i)` | 1 | маска сегмента |
| `[13 + 5i, 17 + 5i)` | 4 | поле хэша сегмента |

Полный размер записи:

```text
12 + 5 × segmentCount
```

Маска литерала `0xFF` при знаковом расширении Java становится `-1`; маска
параметра равна `0x00`. Текущая хэш-функция вычисляет FNV-1a и оставляет младшие
16 бит, хотя значение хранится в четырёхбайтовом поле.

### Поиск кандидатов

URL запроса кодируется в такое же представление сегментов. Записи сначала
фильтруются по количеству сегментов, затем соответствующие хэши маскируются и
сравниваются:

```java
(routeMask & requestHash) == (routeMask & templateHash)
```

Для литерала `routeMask == -1` сохраняет оба хэша. Для параметра
`routeMask == 0` превращает обе стороны в ноль. Декодер собирает смещения всех
записей-кандидатов, а не выбирает обработчик немедленно. Разрешение конфликтов
и извлечение параметров намеренно оставлены следующими стадиями.

### Замороженные символы

При регистрации также строятся исходные символы шаблонов. Перед поиском они
замораживаются в `byte[][][] compiledSymbols` с индексами маршрута, сегмента и
байта. Литеральный сегмент сохраняет исходные байты, а параметр представляется
однобайтовым маркером `'{'`. Порядок записей в ленте маршрутов и порядок
маршрутов в `compiledSymbols` образуют единое пространство индексов.

Это представление предназначено для точного прохода после фильтрации по хэшам.
Оно позволяет отбраковывать коллизии без материализации сегментов запроса:
литералы сравниваются непосредственно с полуинтервалами исходного пути, а для
маркера параметра соответствующий полуинтервал сохраняется либо пропускается.

### Что проверяет текущий ручной стенд

- Несколько записей добавляются без перезаписи предыдущих.
- Маршрут, заканчивающийся параметром, закрывается корректно.
- Запрос совпадает с параметрическим маршрутом.
- Несовпавшие записи сохраняют отметку `-1`.

### Открытая работа

- Сохранить границы и значения сегментов запроса для извлечения параметров.
- Завершить и проверить точный проход по литералам из `compiledSymbols`.
- Разрешать пересекающиеся параметрические кандидаты.
- Сохранять и возвращать идентификатор выбранного обработчика.
- Читать полный беззнаковый размер записи и обходить только занятую часть ленты.
- Добавить рост ёмкости и определить повторное использование декодера.
