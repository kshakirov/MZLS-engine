# ADR-0004: Token Pointer Isolation and Global Barrier Deconstruction

## Status
Accepted

## Context (RU)
В ходе эмпирического анализа структуры ДКА парсера `WirthHttpParser` на ветке `dev` были выявлены две критические уязвимости производительности и логической связности:
1. **Глобальный барьер триггера пробела**: Размещение стейт-машины `switch(status)` внутри глобального условия `if (payload[i] == 0x20)` лишало автомат непрерывности, делая невозможным посимвольный анализ рабочих ASCII-символов.
2. **Ловушка единого скользящего указателя**: Использование одной переменной `start` для фиксации левой границы бегущего токена приводило к потере контекста на этапе парсинга пар "Имя Заголовка : Значение", так как перезапись переменной для старта значения уничтожала координаты начала имени.

## Decision (RU)
1. **Инверсия цикла**: Переключатель `switch(status)` поднят на самый верхний уровень цикла `for`, превращая проверку пробела в локальный изолированный триггер переключения состояний.
2. **Изоляция указателей**: Вместо одной скользящей переменной внедряются изолированные примитивные указатели `int methodStart`, `int uriStart`, `int headerNameStart`, занимающие всего 12 байт в L1-кэше процессора HP, но гарантирующие стабильность поиска O(1).
3. **Исправление префикс-матчера**: Исправлена инвертированная логика в методе `fullMatch`, возвращающая корректные булевы флаги при побайтовом сравнении шаблонов без аллокаций.

## Context & Decision (EN)
To maintain a strict Markovian invariant within the `WirthHttpParser` on the `dev` branch, the architecture has been refactored:
* The global whitespace constraint `if (payload[i] == 0x20)` surrounding the FSM has been deprecated. The `switch(status)` block is relocated to the top level of the execution loop, transforming character validation into local state-scoped triggers.
* The single sliding pointer `start` is replaced with isolated primitive fields (`methodStart`, `uriStart`, `headerNameStart`). This layout prevents token boundary corruption during HTTP header key-value evaluation and fits perfectly into a single CPU L1 cache line (12 bytes total overhead).
* Fixed inversion bug in `fullMatch` utility to provide accurate short-circuiting byte matching.

## Next Steps / План на завтра
1. **Рубеж 1 (REQ_VERSION -> HEADER_START)**: Реализовать переключение стейта при обнаружении пары CRLF (`\r\n`) на конце строки версии протокола.
2. **Рубеж 2 (Великая развилка HEADER_START)**: Проверка на пустую строку (`\r\n\r\n`) для перевода автомата в терминальный стейт `FINISHED`, либо фиксация `headerNameStart` при обнаружении имени нового заголовка.
3. **Рубеж 3 (HEADER_NAME -> HEADER_VALUE)**: Сканирование имени до двоеточия `0x3A`, запись координат в `offsets[]` и переключение на чтение значения.
