# Thnky — contexto del proyecto

Documento de referencia del repo. Si trabajas con Claude Code, dale este archivo
como primera lectura de la sesión (o renómbralo a `CLAUDE.md`, que lo lee solo).

---

## 1. El producto

Thnky es un entrenamiento cognitivo diario y gamificado para la era de la IA.
La gente practica las capacidades que usa menos cuando se apoya en la IA:
programar, razonar lógicamente y pensar críticamente.

Tagline: **"A little thinking every day."**

Tres skills en el MVP: **Coding**, **Logic**, **Critical thinking**.

**La regla que manda sobre todas las decisiones: Thnky entrena, no resuelve.**
Las pistas escalan (nudge → concept → approach) y se detienen antes de la
solución. El usuario tiene que terminar pensando "esto lo he hecho yo", nunca
"me lo ha resuelto la IA". Si una decisión técnica choca con esto, gana esto.

Hecho para HackBarna AI Summit 2026.

---

## 2. Prototipo de referencia

`design/prototype.html` es el contrato visual y de comportamiento. Es un
prototipo clicable autónomo, sin llamadas de red, con el flujo completo:
Home → reto → pistas → enviar → feedback → siguiente.

Se usa para tokens de color, tipografía, layout, copy y flujo. **No se porta su
código**: se reconstruye en condiciones. Pero cualquier diferencia de aspecto o
de texto tiene que ser una decisión, no un descuido.

Incluye tres paletas para elegir (Arcade, Citrus, Mint). El selector de paleta
es de demo y no va a la app final: cuando se decida una, se fija en los tokens.

---

## 3. Cómo trabajamos

- Nada se ejecuta en las máquinas del equipo sin preguntar antes.
- Mensajes de commit y descripciones de PR **en inglés**, sin trailer
  `Co-Authored-By`.
- Commits pequeños, una intención cada uno.
- El código, los identificadores y los comentarios, en inglés. Esta
  documentación de equipo, en castellano.

---

## 4. Principios de diseño de código

### SOLID

En un hackathon SOLID se aplica donde paga, no como ceremonia. Aquí paga en dos
sitios concretos, y no por gusto: es lo que hace que el plan B de la demo salga
gratis.

**Responsabilidad única.** El controller no orquesta ni llama a Nebius: recibe,
delega y responde. El servicio orquesta. El cliente HTTP solo habla HTTP.

**Abierto/cerrado e inversión de dependencias.** Dos abstracciones sostienen la
app entera:

```
ChallengeSource          -> de dónde sale un reto
  StaticChallengeSource     banco fijo, sin red
  NebiusChallengeSource     generado por el modelo
  CachingChallengeSource    decorador, evita repetir llamadas
  FallbackChallengeSource   intenta uno, cae al otro si falla

AnswerGrader             -> cómo se corrige una respuesta
  IndexGrader               choice y lines, comparación de índice, local
  ModelGrader               text y code, juicio por modelo con rúbrica
```

El servicio depende de las interfaces, nunca de las implementaciones. Añadir un
proveedor nuevo, o una forma nueva de corregir, es una clase más y una línea de
configuración. Y el requisito de "si Nebius falla, la demo sigue" deja de ser un
parche: es `FallbackChallengeSource`.

**Sustitución de Liskov.** Cualquier `ChallengeSource` devuelve un reto válido
contra el esquema o lanza. Ninguna implementación devuelve un objeto a medias.

**Segregación de interfaces.** Interfaces pequeñas. Si una clase implementa un
método vacío, la interfaz está mal partida.

### DRY

- **El esquema del reto se define una sola vez.** Es a la vez el modelo de
  dominio del backend, el JSON schema que se le pasa a Nebius y la forma que
  consume el frontend. Si hay tres definiciones distintas del mismo objeto,
  algo se va a desincronizar el domingo a las ocho de la mañana.
- Los prompts viven en archivos propios, no incrustados en la lógica.
- Los tokens visuales se definen una vez en CSS y se consumen por variable.
  Ningún hex suelto dentro de un componente.
- Si copias y pegas un bloque por tercera vez, extrae.

### Frontend por componentes

Un componente, una responsabilidad, un archivo. Componentes de presentación sin
estado siempre que se pueda; el estado sube al contenedor. Nada de componentes
que saben a la vez pintar, pedir datos y decidir la navegación.

---

## 5. Arquitectura

```
navegador (SPA por componentes)
        │  JSON
        ▼
backend Java (Spring Boot)  ──►  Nebius (API compatible con OpenAI)
        │                        guarda la API key
        ▼
Supabase (opcional: perfil, intentos, caché)
```

**El navegador no habla nunca con Nebius.** La key vive en el backend, en
variable de entorno, cargada de `.env`, que está en `.gitignore`. Si la app
llamara a Nebius desde el cliente, la key viajaría al navegador y cualquiera la
saca del inspector en diez segundos.

---

## 6. Backend (Java)

Spring Boot 3 con Java 21. Records para DTOs y modelo de dominio, enums para
skill, dificultad y tipo. IntelliJ va de serie con esto.

### Estructura de paquetes

```
com.thnky
├── api/          controllers y DTOs de entrada/salida
├── domain/       Challenge, Skill, Difficulty, ChallengeType, Answer, Verdict
├── challenge/    ChallengeSource y sus implementaciones
├── grading/      AnswerGrader y sus implementaciones
├── ai/           NebiusClient, prompts, definición del JSON schema
├── profile/      LearnerProfile y su repositorio
└── config/       configuración, beans, propiedades
```

### Endpoints

Dos, y ninguno más para el MVP.

```
GET  /api/challenges/next?skill=&diff=&lang=
     -> un Challenge completo, con las tres pistas dentro

POST /api/answers
     body: { challengeId, answer, hintsUsed, seconds }
     -> { correct, xp, good, improve, insight }
```

### Cliente de Nebius

`RestClient` de Spring contra el endpoint de chat completions, con records para
petición y respuesta. No hace falta traerse un SDK: la API es HTTP y JSON, y un
par de records os dan control total sobre el `response_format`.

Spring AI también soporta endpoints compatibles con OpenAI cambiando la
`base-url`, pero añade configuración que en un hackathon se come tiempo. Si el
`RestClient` os funciona en veinte minutos, no lo toquéis.

Timeout corto y explícito en el cliente. Un reto lento es un reto fallido.

---

## 7. Nebius

API compatible con OpenAI. Solo cambia la URL base y la key.

- Base URL: `https://api.tokenfactory.nebius.com/v1/`
  En su documentación también aparece el host antiguo de AI Studio,
  `https://api.studio.nebius.com/v1/`. **Confirmad cuál corresponde a las keys
  del hackathon antes de perder media hora con errores de auth.**
- Auth: bearer con la key de entorno.
- **Structured outputs**: `response_format` con JSON schema y modo estricto.
  Sin eso el modelo devuelve markdown con backticks y os toca parsear prosa.
- Un modelo instruct sólido, fijado en configuración. Nada de nombres de modelo
  desperdigados por el código.

### Dos trabajos, dos prompts. No se mezclan.

**Generador.** Recibe skill, dificultad, lenguaje (si es de código), tipo de
reto y un resumen del perfil del usuario. Devuelve un reto completo **con las
tres pistas ya dentro, en una sola llamada**. Generar pista a pista es más
lento, más caro, y el modelo no sabe que la tercera tiene que ser más fuerte que
la primera.

El prompt del generador necesita ejemplos: se le pasan dos o tres retos del
banco estático como few-shot. El modelo imita el tono ("Eight coins. Two
weighings.") mucho mejor de lo que lo sigue si solo se lo describes.

**Juez.** Solo para `text` y `code`. Recibe el reto, la respuesta, las pistas
gastadas y el tiempo, y devuelve veredicto más los tres campos de feedback.

El juez **no repite la solución** en su feedback. Habla del razonamiento del
usuario, no de la respuesta correcta.

Los retos `choice` y `lines` se corrigen en local comparando índices: es
instantáneo, gratis y no falla nunca. Mandarlos al modelo sería tirar latencia y
dinero.

---

## 8. Esquema del reto

Todo reto, estático o generado, tiene esta forma. El prototipo ya la consume.

```json
{
  "id": "string",
  "skill": "code | logic | critical",
  "lang": "js | py | sql",
  "diff": "Easy | Medium | Hard",
  "type": "choice | text | code | lines",
  "hook": "frase corta y potente, va grande sobre la banda de color",
  "title": "string",
  "desc": "una o dos frases",

  "options":    ["..."],
  "answer":     0,
  "optionsSvg": ["<svg…>"],
  "figure":     "<svg…>",

  "starter": "esqueleto de código",
  "lines":   ["línea", "línea"],
  "file":    "average.js",

  "hints":   ["nudge", "concept", "approach"],
  "good":    "qué hizo bien",
  "improve": "qué trabajar",
  "insight": "una línea de 'think about this'"
}
```

Campos por tipo: `options` y `answer` para `choice`; `optionsSvg` para `choice`
con respuestas visuales; `starter` para `code`; `lines`, `file` y `answer` para
`lines`; `figure` es opcional en cualquiera.

---

## 9. Supabase

Se usa cuando haga falta persistir algo entre sesiones. Es preferible a montar
Postgres a mano: da la tabla, la API y el panel sin instalar nada.

Tablas mínimas:

```
learner_profile   user_id, skill, level, last_results (jsonb)
attempt           id, user_id, challenge_id, correct, hints_used, seconds, created_at
challenge_cache   key (skill+diff+lang+perfil), challenge (jsonb), created_at
```

**La service key de Supabase vive en el backend, igual que la de Nebius.** Si en
algún momento el frontend habla directo con Supabase, que sea con la anon key y
con RLS activo. Para el hackathon es más simple que todo pase por el backend.

Si al final no hace falta persistir nada, no la metáis. `localStorage` en el
cliente cubre racha y XP para la demo.

---

## 10. Personalización

Perfil ligero por usuario: para cada skill, los últimos resultados (acertado o
no, pistas usadas, segundos). Un resumen de eso entra en el prompt del generador
para que dificultad y tema se adapten.

Esta es la parte que mira el jurado: es lo que separa Thnky de un prompt con una
interfaz bonita. Conviene poder enseñarlo en la demo.

---

## 11. Seguridad

- Ninguna key, token o endpoint privado en código de cliente. Nada de secretos
  commiteados, nunca.
- **No se ejecuta código enviado por el usuario.** Ni en el navegador ni en el
  servidor. Los retos de código se corrigen por modelo o por inspección.
- La salida del modelo es dato no confiable. El texto generado se pinta como
  texto, jamás se inyecta como HTML. La excepción son `figure` y `optionsSvg`,
  que se sanean o, mejor, se generan con constructores de SVG en el servidor a
  partir de parámetros, en vez de dejar que el modelo escriba SVG libre.
- Nada de datos personales. Sin cuentas reales en el hackathon.
- Ningún texto de la app afirma medir inteligencia ni CI. Thnky mide progreso
  dentro de Thnky, y lo dice.

---

## 12. Resistencia de la demo

La demo es el domingo por la tarde con el wifi de un congreso. Se diseña
asumiendo que falla.

1. El banco estático del prototipo va incluido como fallback sembrado. Toda ruta
   de generación cae a él ante error, timeout o esquema que no valida.
2. Ningún botón espera a una generación en vivo. El siguiente reto se genera en
   segundo plano mientras el usuario resuelve el actual.
3. Los retos generados se cachean. El mismo perfil y los mismos parámetros no
   pagan dos llamadas.
4. **El flujo completo tiene que funcionar con el cable desenchufado**, tirando
   del banco de fallback. Probadlo antes del domingo, no el domingo.

---

## 13. Orden de trabajo sugerido

1. Backend con los dos endpoints devolviendo siempre el banco estático, sin
   Nebius. Con esto la app entera ya funciona de punta a punta.
2. Frontend por componentes consumiendo esos endpoints, con el prototipo
   delante.
3. `NebiusChallengeSource` detrás del mismo endpoint, con el fallback puesto
   desde el primer commit.
4. `ModelGrader` para texto libre y código.
5. Perfil y personalización.
6. Supabase, solo si algo necesita sobrevivir a un refresco.

Si a las tres de la mañana Nebius da guerra, los pasos 1 y 2 ya son una demo.

---

## 14. Fuera de alcance del MVP

Sin ranking, sin amigos, sin funciones sociales, sin logros.
Sin versión para niños: se cuenta como roadmap, no se construye.
