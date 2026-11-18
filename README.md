# Practica de Laboratorio

Aplicación web para la gestión de organizaciones, usuarios, clientes y proyectos. El backend expone una API REST construida con **Spring Boot** y seguridad basada en **JWT**, y el frontend es una **SPA en Angular** que consume dicha API.

## Tabla de contenidos

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos previos](#requisitos-previos)
- [Puesta en marcha](#puesta-en-marcha)
  - [1. Clonar el repositorio](#1-clonar-el-repositorio)
  - [2. Configurar la base de datos](#2-configurar-la-base-de-datos)
  - [3. Ejecutar el backend](#3-ejecutar-el-backend)
  - [4. Ejecutar el frontend](#4-ejecutar-el-frontend)
- [Endpoints de la API](#endpoints-de-la-api)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Testing](#testing)
- [Build de producción](#build-de-producción)

## Características

- Registro y login de usuarios con autenticación basada en JWT.
- Administración de usuarios de una organización (roles, alta y cambio de estado).
- Gestión de clientes (alta, edición, baja/reactivación, listado paginado).
- Gestión de proyectos: creación, edición, cambio de etapa con historial, y administración de miembros.

## Arquitectura

```
.
├── src/                # Backend (Spring Boot)
└── frontend/           # Frontend (Angular)
```

El frontend consume la API del backend a través de un proxy configurado en desarrollo (`frontend/proxy.conf.json`), por lo que las peticiones a `/api/**` se redirigen a `http://localhost:8080`.

## Tecnologías

**Backend**
- Java 21
- Spring Boot 4.1.2 (Web, Data JPA, Validation, Security)
- PostgreSQL (driver JDBC)
- JWT (`io.jsonwebtoken` / jjwt)
- Lombok
- Maven

**Frontend**
- Angular 20
- TypeScript 5.9
- Bootstrap 5
- RxJS

## Requisitos previos

- [JDK 21](https://adoptium.net/) o superior
- [Node.js](https://nodejs.org/) 18+ y npm
- [Angular CLI](https://angular.dev/tools/cli) (`npm install -g @angular/cli`) — opcional, el proyecto incluye sus propios scripts npm
- Una instancia de [PostgreSQL](https://www.postgresql.org/) accesible (local o en contenedor)
- Maven no es necesario instalarlo: el proyecto incluye el *wrapper* (`mvnw` / `mvnw.cmd`)

## Puesta en marcha

### 1. Clonar el repositorio

```bash
git clone https://github.com/<usuario>/practicalaboratorio.git
cd practicalaboratorio
```

### 2. Configurar la base de datos

El proyecto usa PostgreSQL. Crea una base de datos vacía y defínela mediante variables de entorno (Spring Boot las mapea automáticamente a las propiedades `spring.datasource.*`):

```bash
# Linux / macOS
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/practicalaboratorio
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
```

```powershell
# Windows PowerShell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/practicalaboratorio"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
```

Alternativamente, puedes definir estas propiedades directamente en `src/main/resources/application.properties` o crear un `application-local.properties` (no versionado) con tus credenciales.

> El esquema de la base de datos se crea/actualiza automáticamente al arrancar la aplicación (`spring.jpa.hibernate.ddl-auto=update`), no se requieren migraciones manuales.

También puedes levantar una instancia rápida de PostgreSQL con Docker:

```bash
docker run --name practicalaboratorio-db \
  -e POSTGRES_DB=practicalaboratorio \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:16
```

### 3. Ejecutar el backend

Desde la raíz del proyecto:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

El backend quedará disponible en **http://localhost:8080**.

Para generar el artefacto empaquetado:

```bash
./mvnw clean package
java -jar target/practicalaboratorio-0.0.1-SNAPSHOT.jar
```

### 4. Ejecutar el frontend

En otra terminal:

```bash
cd frontend
npm install
npm start
```

El frontend quedará disponible en **http://localhost:4200** y redirigirá automáticamente las llamadas a `/api/**` hacia el backend en el puerto 8080.

## Endpoints de la API

| Recurso      | Método | Ruta                                   | Descripción                              |
|--------------|--------|-----------------------------------------|-------------------------------------------|
| Auth         | POST   | `/api/auth/registro`                    | Registra una organización y su usuario admin |
| Auth         | POST   | `/api/auth/login`                       | Inicia sesión y devuelve un JWT          |
| Usuarios     | GET    | `/api/usuarios/me`                      | Usuario autenticado actual               |
| Usuarios     | GET    | `/api/usuarios`                         | Lista de usuarios de la organización     |
| Usuarios     | POST   | `/api/usuarios`                         | Crea un usuario                          |
| Usuarios     | PATCH  | `/api/usuarios/{id}/estado`             | Cambia el estado de un usuario           |
| Clientes     | GET    | `/api/clientes`                         | Lista paginada de clientes               |
| Clientes     | GET    | `/api/clientes/{id}`                    | Detalle de un cliente                    |
| Clientes     | POST   | `/api/clientes`                         | Crea un cliente                          |
| Clientes     | PUT    | `/api/clientes/{id}`                    | Actualiza un cliente                     |
| Clientes     | DELETE | `/api/clientes/{id}`                    | Da de baja un cliente                    |
| Clientes     | PATCH  | `/api/clientes/{id}/reactivar`          | Reactiva un cliente                      |
| Proyectos    | GET    | `/api/proyectos`                        | Lista de proyectos                       |
| Proyectos    | GET    | `/api/proyectos/{id}`                   | Detalle de un proyecto                   |
| Proyectos    | POST   | `/api/proyectos`                        | Crea un proyecto                         |
| Proyectos    | PUT    | `/api/proyectos/{id}`                   | Actualiza un proyecto                    |
| Proyectos    | PATCH  | `/api/proyectos/{id}/etapa`             | Cambia la etapa del proyecto             |
| Proyectos    | POST   | `/api/proyectos/{id}/miembros`          | Agrega un miembro al proyecto            |
| Proyectos    | DELETE | `/api/proyectos/{id}/miembros/{usuarioId}` | Elimina un miembro del proyecto      |

Salvo `/api/auth/**`, todos los endpoints requieren el header `Authorization: Bearer <token>`.

## Estructura del proyecto

```
src/main/java/com/example/practicalaboratorio/
├── auth/            # Login, registro, JWT y configuración de seguridad
├── usuario/         # Gestión de usuarios y roles
├── organizacion/    # Entidad y repositorio de organización
├── cliente/         # Gestión de clientes
├── proyecto/        # Gestión de proyectos, etapas, historial y miembros
└── common/          # DTOs y manejo global de excepciones

frontend/src/app/
├── core/            # Servicios, modelos, guards e interceptores
└── features/        # Módulos de funcionalidad (auth, clientes, proyectos, usuarios, layout)
```

## Testing

```bash
# Backend
./mvnw test

# Frontend
cd frontend
npm test
```

## Build de producción

```bash
# Backend
./mvnw clean package

# Frontend
cd frontend
npm run build
```

Los artefactos del frontend se generan en `frontend/dist/`.
