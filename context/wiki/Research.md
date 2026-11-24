# Research / Исследования

[English](#english) · [Русский](#русский)

## English

This section contains questions for which the project has not accepted a final
engineering decision.

### Active directions

- **Formal models and nuXmv.** Keep Java body-parser transitions aligned with
  the checked finite model and verify safety and progress properties.
- **Router conflict resolution.** Define priority between overlapping
  parameterised templates without hiding the rule in iteration order.
- **Parameter binding.** Preserve request segment boundaries and expose values
  without materialising intermediate strings.
- **Streaming HTTP.** Define buffer ownership, capacity limits, pipelining, and
  the lifecycle of multiple logical requests on one connection.
- **Glossary and model classification.** Identify each component by a precise
  computational model only after its semantics stabilise.

When a question receives an accepted answer, the decision moves to an ADR and
the resulting mechanism is documented under Architecture.

## Русский

Здесь находятся вопросы, по которым проект ещё не принял окончательного
инженерного решения.

### Активные направления

- **Формальные модели и nuXmv.** Сохранять соответствие переходов Java-автомата
  тела проверяемой конечной модели и доказывать свойства безопасности и
  прогресса.
- **Разрешение конфликтов маршрутов.** Определить приоритет пересекающихся
  параметрических шаблонов, не пряча правило в порядке обхода.
- **Связывание параметров.** Сохранить границы сегментов запроса и выдавать
  значения без материализации промежуточных строк.
- **Потоковый HTTP.** Определить владение буферами, ограничения ёмкости,
  pipelining и жизненный цикл нескольких логических запросов в соединении.
- **Глоссарий и классификация моделей.** Давать компонентам точные научные
  названия после стабилизации их семантики.

После принятия ответа решение фиксируется в ADR, а получившийся механизм
описывается в Architecture.
