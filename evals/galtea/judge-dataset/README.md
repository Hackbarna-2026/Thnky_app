# Galtea evaluation — Judge (ModelGrader) accuracy

Independent check, via [Galtea](https://galtea.ai) (hackathon sponsor tool), that the
backend's judge (`ModelGrader`, section 7 of `CLAUDE.md`) grades text/code answers
correctly. Latest run: **12/12 test cases, score 1.0 ("JSON Field Match")**.

## What's in `dataset.csv`

12 cases across 4 static challenges (`index`, `claim` — text; `dupes`, `reverse` —
code): a genuinely correct answer, a genuine but wrong attempt, and an off-topic
placeholder (e.g. "hello world") per challenge. `input` is `{challengeId, answer}`;
`expected_output` is `{correct}`. Regenerate with `python3 build_dataset.py`.

## Galtea setup

- Product: **Thnky Judge** (`product_lgds7x75thdd9i5gzamr0oon`) → Version **v1**
  (`version_vedsx20833jm8io7gaaztj94`)
- Dataset: uploaded as a custom test file via the CLI's presigned-URL flow (no
  generation credits spent) — `galtea storage generate-put-url --file-type testFile`,
  `curl -X PUT`, then `galtea datasets create` with the returned download URL as `uri`
- Metric: Galtea's built-in **JSON Field Match** (`cd83153d-27a0-41bd-8ba0-3bec1d9600d2`)
  — deterministic, compares `actualOutput` against the test case's `expectedOutput`
  field by field

## Re-running it

1. Start the backend locally (`./mvnw spring-boot:run`).
2. For each test case, `POST /api/answers` with `{challengeId, answer, hintsUsed: 0,
   seconds: 30}` from `input`, and take `{"correct": <response.correct>}` as
   `actualOutput`.
3. Submit each result with `galtea evaluations create-single-turn`, passing
   `testCaseId`, `actualOutput`, `metrics: [{"id": "cd83153d-..."}]`, and `productId`.
4. Poll `galtea evaluations list --ids <ids>` until none are `PENDING`; read `score`
   and `reason` off the terminal rows.

Results (evaluation IDs, scores) live in the Galtea dashboard, not in this repo — the
dataset and script here are what make the run reproducible.
