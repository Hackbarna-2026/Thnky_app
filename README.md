# Thnky

Entrenamiento cognitivo diario y gamificado para la era de la IA.
*A little thinking every day.* Hecho para HackBarna AI Summit 2026.

El contexto completo del proyecto, las reglas de diseño y el plan de trabajo
están en [CLAUDE.md](CLAUDE.md).

## Estructura

```
backend/     Spring Boot 3 (Java 25): API, generación con Nebius, corrección
frontend/    SPA con Vite + React
design/      prototype.html, el contrato visual
supabase/    schema.sql de la base de datos
```

## Requisitos

- JDK 25 (comprueba con `mvn -v` que Maven usa ese JDK)
- Maven 3.6.3+
- Node 20.19+ y npm

## Puesta en marcha

1. Copia las variables de entorno:

   ```
   cp .env.example .env
   ```

   El `.env` no se commitea nunca. Sin él la app arranca igualmente, solo con el
   banco estático de retos y sin base de datos.

2. Backend, en `http://localhost:8080`:

   ```
   cd backend
   mvn spring-boot:run
   ```

3. Frontend, en `http://localhost:5173`, con `/api` redirigido al backend:

   ```
   cd frontend
   npm install
   npm run dev
   ```

## Variables de entorno

| Variable | Para qué | Obligatoria |
|---|---|---|
| `NEBIUS_API_KEY`, `NEBIUS_BASE_URL`, `NEBIUS_MODEL` | Generación y corrección con el modelo | No, cae al banco estático |
| `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD` | Base de datos | No, sin ella no se persiste nada |

## Conectar Supabase

1. Ejecuta `supabase/schema.sql` en el SQL Editor de tu proyecto.
2. En **Connect** elige **Session pooler**. La conexión directa usa IPv6 y falla
   en muchas redes.
3. Rellena el `.env`:

   ```
   SUPABASE_DB_URL=jdbc:postgresql://<host-del-pooler>:5432/postgres
   SUPABASE_DB_USER=postgres.<project-ref>
   SUPABASE_DB_PASSWORD=<contraseña de la base de datos>
   ```

   Supabase solo muestra una URI `postgresql://usuario:contraseña@host:5432/postgres`.
   Para el JDBC: usa solo la parte `host:5432/postgres` con `jdbc:postgresql://`
   delante, y pon usuario y contraseña en sus variables. Nada de `@` en la URL.
