# Engineering Decisions / Инженерные решения

[English](#english) · [Русский](#русский)

## Registry / Реестр

| ADR | Title / Название | Status | Canonical source |
|---|---|---|---|
| 0001 | Network I/O Core / Ядро сетевого ввода-вывода | Accepted | [`adr_journal.tex`](https://github.com/kshakirov/MZLS-engine/blob/master/doc/adr/adr_journal.tex) |
| 0002 | Incremental Latency Monitoring / Инкрементальный мониторинг латентности | Proposed | [`adr_journal.tex`](https://github.com/kshakirov/MZLS-engine/blob/master/doc/adr/adr_journal.tex) |
| 0003 | HTTP Parser Automaton / Автомат HTTP-парсера | Proposed | [`adr_journal.tex`](https://github.com/kshakirov/MZLS-engine/blob/master/doc/adr/adr_journal.tex) |
| 0004 | Token Pointer Isolation / Изоляция указателей токенов | Accepted | [`0004-token-pointer-isolation.md`](https://github.com/kshakirov/MZLS-engine/blob/master/doc/adr/0004-token-pointer-isolation.md) |
| 0005 | Lookahead-1 Alignment and NAM Roadmap / Согласование Lookahead-1 и план НАМ | Accepted | [`0005-version-alignment-and-nam-roadmap.md`](https://github.com/kshakirov/MZLS-engine/blob/master/doc/adr/0005-version-alignment-and-nam-roadmap.md) |
| 0006 | Binary Route Tape and Frozen Symbols / Бинарная лента маршрутов и замороженные символы | Accepted | [`0006-binary-router.md`](https://github.com/kshakirov/MZLS-engine/blob/master/doc/adr/0006-binary-router.md) |

## English

An ADR records a decision, its context, rationale, consequences, and status. It
does not describe every current implementation detail and is not silently
rewritten when the code evolves. A later decision may explicitly supersede an
earlier one.

The registry above preserves the existing numbering and statuses. ADR-0001 to
ADR-0003 currently share a LaTeX journal; ADR-0004 to ADR-0006 are Markdown
files. They may be migrated to a uniform bilingual Markdown format without
renumbering or changing their historical meaning.

New ADR format:

```markdown
# ADR-NNNN — English title / Русское название

Status: Proposed | Accepted | Implemented | Superseded | Rejected

## English
### Context
### Decision
### Rationale
### Consequences

## Русский
### Контекст
### Решение
### Обоснование
### Последствия
```

## Русский

ADR фиксирует решение, его контекст, обоснование, последствия и статус. Он не
описывает каждую деталь текущей реализации и не переписывается молча при
изменении кода. Новое решение может явно заменить старое.

Реестр сохраняет существующую нумерацию и статусы. ADR-0001–ADR-0003 сейчас
находятся в общем LaTeX-журнале, ADR-0004–ADR-0006 — в Markdown. Их можно
постепенно перенести в единый двуязычный формат без перенумерации и изменения
исторического смысла.
