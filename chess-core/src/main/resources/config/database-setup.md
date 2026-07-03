# Base de datos Local usando PostgreSQL

Para ello nos tendremos que conectar a la bbdd genérica de postgres usando `psql`, crearemos la
bbdd de este proyecto llamada `generic` y nos conectaremos a ella, creamos un usuario llamado
`user` con ciertos permisos y creamos un esquema con el mismo nombre para ese usuario e indicamos
que lo utilice como default en vez de el esquema `public`.

La configuración de conexión de la aplicación (ver `application.yml`) espera:

- **Host:** `localhost`
- **Puerto:** `2345`
- **Base de datos:** `generic`
- **Usuario:** `user`
- **Password:** `pass`
- **Schema:** `generic`

---

## Opción 1 — Instalación nativa con instalador (Windows, PostgreSQL 17)

Instalar la última versión de PostgreSQL 17, poner o no contraseña al usuario `postgres`, y hacer
que el puerto sea `2345` durante la instalación.

### Añadir psql al PATH de Windows

1. Abre **Configuración del sistema → Variables de entorno**.
2. En "Variables del sistema", selecciona **Path** y haz clic en **Editar**.
3. Haz clic en **Nuevo** y añade la ruta al directorio `bin`:
   ```
   C:\Program Files\PostgreSQL\17\bin
   ```
4. Acepta y cierra las ventanas.

### Crear la base de datos, usuario y esquema

Abre una consola de PowerShell y ejecuta los siguientes comandos en orden:

```powershell
psql -U postgres -h localhost -p 2345
```

```sql
CREATE DATABASE generic;
\c generic
CREATE ROLE "user" WITH LOGIN PASSWORD 'pass' SUPERUSER CREATEDB CREATEROLE INHERIT REPLICATION BYPASSRLS CONNECTION LIMIT -1;
CREATE SCHEMA IF NOT EXISTS generic AUTHORIZATION "user";
ALTER ROLE "user" SET search_path TO generic;
CREATE EXTENSION IF NOT EXISTS unaccent;
ALTER EXTENSION unaccent SET SCHEMA generic;
```

### Conectarse con el nuevo usuario

```powershell
psql -U user -d generic -h localhost -p 2345
```

---

## Opción 2 — Docker (PostgreSQL 17.5 alpine)

```bash
docker pull postgres:17.5-alpine

docker run -d --name postgres_db \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=pass \
  -e POSTGRES_DB=generic \
  -p 2345:5432 \
  postgres:17.5-alpine

docker ps

docker exec -it postgres_db psql -U user -d generic
```

```sql
CREATE SCHEMA IF NOT EXISTS generic;
ALTER ROLE "user" SET search_path TO generic, public;
CREATE EXTENSION IF NOT EXISTS unaccent;
ALTER EXTENSION unaccent SET SCHEMA generic;
```

---

## Opción 3 — Apple `container` CLI (nativo de macOS, requiere Apple Silicon, PostgreSQL 18 alpine)

```bash
# Arrancar el runtime de container (una vez por sesión/reinicio del Mac)
container system start

# Descargar la imagen de postgres 18 (alpine)
container image pull docker.io/library/postgres:18-alpine

# Levantar el contenedor, publicando el puerto 2345 del host al 5432 del contenedor
# OJO: el flag -p/--publish debe ir ANTES del nombre de la imagen, si no se interpreta
# como argumento del propio postgres
container run -d --name postgres_db \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=pass \
  -e POSTGRES_DB=generic \
  -p 2345:5432 \
  postgres:18-alpine

# Comprobar que está corriendo (equivalente a docker ps)
container list

# Conectar al contenedor usando el nuevo usuario
container exec -it postgres_db psql -U user -d generic
```

```sql
CREATE SCHEMA IF NOT EXISTS generic;
ALTER ROLE "user" SET search_path TO generic, public;
CREATE EXTENSION IF NOT EXISTS unaccent;
ALTER EXTENSION unaccent SET SCHEMA generic;
```

### Notas sobre `container` CLI

- Comandos equivalentes a Docker: `docker pull` → `container image pull`,
  `docker ps` → `container list` (o `container list -a` para ver también los parados),
  `docker exec` → `container exec`, `docker stop`/`docker rm` → `container stop` / `container rm`.
- Cada contenedor recibe su propia IP enrutable; si `-p 2345:5432` diera problemas, se puede
  conectar directamente a la IP del contenedor (visible con `container list`) por el puerto
  nativo `5432`.
- **Evitar bind-mount** del directorio de datos para persistencia: hay un bug conocido en macOS
  ([apple/container#333](https://github.com/apple/container/issues/333)) que hace fallar el
  `chmod`/`chown` sobre `/var/lib/postgresql/data` cuando se monta un directorio del host. Usar en
  su lugar un volumen gestionado por `container`:
  ```bash
  container run -d --name postgres_db \
    -e POSTGRES_USER=user \
    -e POSTGRES_PASSWORD=pass \
    -e POSTGRES_DB=generic \
    -p 2345:5432 \
    --volume postgres_data:/var/lib/postgresql/data \
    postgres:18-alpine
  ```

### Limpieza / gestión del contenedor

```bash
container stop postgres_db
container rm postgres_db
container images rm postgres:18-alpine
```