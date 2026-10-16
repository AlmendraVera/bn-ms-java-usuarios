# bn-ms-java-usuarios

Microservicio de **autenticación y permisos** (Capa 3 de la arquitectura). Administra usuarios y roles y valida el login. El API Gateway lo consulta para autorizar por rol.

> Proyecto de la **Evaluación Parcial 2 (JVY0101)**. Forma parte de la arquitectura de 4 microservicios: usuarios, inventario, órdenes y picking-despacho.

## Modelo de datos

```
Rol 1 ───── N Usuario        (@OneToMany en Rol / @ManyToOne en Usuario)
```

- **Rol**: `id`, `nombre` (único), `permisos` (texto separado por comas)
- **Usuario**: `id`, `nombre`, `email` (único), `password` (se guarda con hash PBKDF2 y nunca se devuelve en el JSON), `activo`, `rol`

## Datos de ejemplo (se cargan al iniciar)

| Email | Password | Rol |
|---|---|---|
| admin@bn.cl | admin123 | ADMIN |
| operario@bn.cl | operario123 | OPERARIO |
| comercio@bn.cl | comercio123 | COMERCIO |

## Endpoints

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/auth/login` | Valida credenciales; devuelve rol y permisos | 200, 400, 401, 403 |
| GET | `/api/usuarios` | Lista usuarios (`?rol=OPERARIO` para filtrar) | 200 |
| GET | `/api/usuarios/{id}` | Busca un usuario | 200, 404 |
| POST | `/api/usuarios` | Crea un usuario | 201, 400, 404, 409 |
| PUT | `/api/usuarios/{id}` | Actualiza un usuario (la password es opcional) | 200, 400, 404, 409 |
| DELETE | `/api/usuarios/{id}` | Elimina un usuario | 204, 404 |
| GET | `/api/roles` | Lista roles | 200 |
| GET | `/api/roles/{id}` | Busca un rol | 200, 404 |
| POST | `/api/roles` | Crea un rol | 201, 400, 409 |
| PUT | `/api/roles/{id}` | Actualiza un rol | 200, 400, 404, 409 |
| DELETE | `/api/roles/{id}` | Elimina un rol (409 si tiene usuarios) | 204, 404, 409 |

### Ejemplos

```bash
# Login
curl -X POST localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"operario@bn.cl","password":"operario123"}'

# Crear usuario (el rol se indica por id)
curl -X POST localhost:8081/api/usuarios -H "Content-Type: application/json" \
  -d '{"nombre":"Ana Pérez","email":"ana@bn.cl","password":"clave123","rol":{"id":2}}'
```

## Requisitos

- JDK 17 o superior (`java -version`)
- Git
- No hace falta instalar Maven: el proyecto incluye el *Maven Wrapper* (`mvnw` / `mvnw.cmd`)

## Cómo clonar, instalar dependencias y ejecutar

```bash
git clone https://github.com/AlmendraVera/bn-ms-java-usuarios.git
cd bn-ms-java-usuarios

# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

Maven descarga las dependencias automáticamente la primera vez. Cuando termine verás
`Started Application` y el servicio quedará escuchando en **http://localhost:8081**.

Este servicio **no depende de otros microservicios**.

## Base de datos

H2 en memoria (se crea al iniciar y se borra al detener). Consola web: **http://localhost:8081/h2-console**

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:usuariosdb;DB_CLOSE_DELAY=-1` |
| User Name | `sa` |
| Password | *(vacía)* |

La configuración está en `src/main/resources/application.properties`.

## Compilar y empaquetar con Maven

```bash
./mvnw clean      # borra la carpeta target/
./mvnw install    # compila, ejecuta los tests e instala el jar en el repositorio local
./mvnw package    # genera target/bn-ms-java-usuarios-0.0.1-SNAPSHOT.jar

java -jar target/bn-ms-java-usuarios-0.0.1-SNAPSHOT.jar
```

## Estructura (arquitectura en capas)

```
src/main/java/cl/duoc/bn/usuarios/
├── controller/   endpoints REST (reciben la petición y devuelven la respuesta HTTP)
├── service/      lógica de negocio y validaciones
├── repository/   acceso a datos (Spring Data JPA)
├── model/        entidades JPA
├── exception/    ApiException + ManejadorExcepciones (respuestas de error en JSON)
├── config/       DatosIniciales: carga roles y usuarios de ejemplo
└── Application.java
```

## Formato de los errores

Todos los errores devuelven el mismo JSON:

```json
{
  "timestamp": "2026-10-15T21:03:08",
  "status": 404,
  "error": "Not Found",
  "mensaje": "No existe un recurso con id 99",
  "ruta": "/api/..."
}
```

| Código | Cuándo ocurre |
|---|---|
| 400 | Datos faltantes o inválidos, JSON mal formado, parámetro con tipo incorrecto |
| 404 | El recurso no existe |
| 409 | Conflicto: dato duplicado o acción no permitida en el estado actual |
| 401 | Credenciales inválidas en el login |
| 403 | Usuario desactivado |

## Integrantes

- *(Nombre y apellido del integrante 1)*
- *(Nombre y apellido del integrante 2)*
