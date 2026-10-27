# Профили запросов

| Аргумент | Запрос | Тело | Передача |
|---|---|---:|---|
| `get` | `GET /index.html` | 0 B | без тела |
| `fixed` | `POST /index.html` | 16 B | `Content-Length: 16` |
| `chunked` | `POST /index.html` | 16 B | HTTP/1.1 chunked |

Fixed и chunked используют один файл
`requests/bodies/body-16b.txt`. Менять request, rate, duration, affinity,
keep-alive, Nginx или размеры тела — значит создать новый профиль.

Профиль chunked создаётся ключом Vegeta `-chunked`. До принятия baseline нужно
одиночным запросом доказать, что Nginx не нормализует framing перед передачей в
Unix socket.
