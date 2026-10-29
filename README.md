# Biblioteca Digital

Sistema de gestión bibliotecaria desarrollado con **Java 21** y **Spring Boot**, orientado a la administración de ejemplares, préstamos, devoluciones, multas y pagos. Incluye un panel administrativo con indicadores de resumen y un catálogo público consultable sin registro.

> **Estado del proyecto:** Versión estable de desarrollo.

## Tabla de contenido

- [Descripción](#descripción)
- [Objetivo y problema que resuelve](#objetivo-y-problema-que-resuelve)
- [Funcionalidades implementadas](#funcionalidades-implementadas)
- [Roles y permisos](#roles-y-permisos)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Arquitectura y estructura](#arquitectura-y-estructura)
- [Requisitos previos](#requisitos-previos)
- [Instalación y configuración local](#instalación-y-configuración-local)
- [Configuración segura de `DB_PASSWORD`](#configuración-segura-de-db_password)
- [Creación y configuración de la base de datos](#creación-y-configuración-de-la-base-de-datos)
- [Ejecución de la aplicación](#ejecución-de-la-aplicación)
- [Pruebas automatizadas](#pruebas-automatizadas)
- [Rutas principales](#rutas-principales)
- [Capturas de pantalla](#capturas-de-pantalla)
- [Estado actual y limitaciones conocidas](#estado-actual-y-limitaciones-conocidas)
- [Autor](#autor)

## Descripción

Biblioteca Digital es una aplicación web desarrollada como proyecto académico para gestionar los procesos habituales de una biblioteca: catálogo de ejemplares, registro de usuarios, préstamos, devoluciones, cálculo de multas por retraso y pagos parciales.

Cada registro de la entidad `Libro` representa un **ejemplar físico individual**, lo que permite controlar la disponibilidad real de cada copia. El plazo de préstamo es de **15 días** y la multa por retraso es de **$1.000 COP por día**.

La aplicación utiliza **PostgreSQL** como base de datos, **Spring Security** para la autenticación y autorización, y **Thymeleaf** para las vistas. Incluye **97 pruebas automatizadas** (94 unitarias/web y 3 de integración con PostgreSQL) que verifican el correcto funcionamiento de los servicios, los controladores y la seguridad.

## Objetivo y problema que resuelve

El proyecto busca automatizar la gestión bibliotecaria que normalmente se realiza de forma manual o con hojas de cálculo, resolviendo problemas como:

- Pérdida del historial de préstamos y devoluciones.
- Dificultad para calcular multas de forma consistente.
- Falta de visibilidad sobre el estado general de la biblioteca (ejemplares disponibles, préstamos vencidos, saldos pendientes).
- Riesgo de errores al registrar pagos parciales o devoluciones duplicadas.

Biblioteca Digital centraliza estos procesos en una aplicación web con roles diferenciados, protegiendo los datos y manteniendo un historial completo de todas las operaciones.

## Funcionalidades implementadas

### Catálogo público

- Listado de todos los ejemplares con filtros por título, autor y categoría.
- Detalle de cada ejemplar mostrando estado de disponibilidad.
- Consulta de categorías disponibles.

### Registro e inicio de sesión de usuarios

- Registro público de nuevos usuarios con asignación automática del rol `CLIENTE`.
- Inicio de sesión con autenticación basada en Spring Security.
- Cifrado de contraseñas con **BCrypt**.
- Deshabilitación de usuarios inactivos.

### Préstamos y devoluciones

- Solicitud de préstamo desde el detalle del ejemplar (rol `CLIENTE`).
- Prevención de préstamos duplicados del mismo ejemplar mediante bloqueo pesimista (`PESSIMISTIC_WRITE`).
- Registro de devolución física por parte del administrador.
- Prevención de devoluciones repetidas.
- Cálculo automático de multa al registrar la devolución.

### Multas y pagos

- Cálculo de multa estimada para préstamos activos con retraso.
- Multa definitiva al registrar la devolución, con estados `PENDIENTE`, `PARCIAL` y `PAGADA`.
- Registro de pagos parciales con referencia única.
- Consulta de saldo pendiente de cada multa.
- Historial completo de multas y pagos sin eliminación.

### Perfil del cliente

- Consulta y edición del perfil personal.
- Visualización de préstamos propios (activos y devueltos).
- Visualización de multas propias con detalle de pagos.

### Administración de libros

- Listado, búsqueda, registro y edición de ejemplares.
- Control de estado activo/inactivo para ejemplares dañados o retirados.

### Administración de categorías

- Listado, creación y edición de categorías.
- Detección de duplicados sin distinguir mayúsculas y minúsculas.

### Panel administrativo (Dashboard)

Indicadores de resumen en tiempo real:

1. Total de ejemplares registrados.
2. Ejemplares disponibles (activos y sin préstamo activo).
3. Ejemplares inactivos.
4. Préstamos activos.
5. Préstamos vencidos (activos con fecha límite pasada).
6. Préstamos devueltos.
7. Multas pendientes de pago.
8. Saldo total pendiente de las multas.

### Seguridad

- Protección CSRF en todos los formularios POST.
- Aislamiento de datos: cada cliente solo accede a sus propios préstamos y multas.
- Rutas administrativas protegidas exclusivamente para `ROLE_ADMIN`.
- Contraseñas hasheadas con BCrypt.
- Contraseña de base de datos gestionada mediante variable de entorno.

## Roles y permisos

| Rol | Descripción | Acceso |
|---|---|---|
| **CLIENTE** | Usuario registrado que consulta y solicita préstamos | Catálogo público, perfil, propios préstamos, propias multas y pagos |
| **ADMIN** | Administrador de la biblioteca | Todo lo anterior más: panel general, administración de libros y categorías, registro de devoluciones, consulta de todas las multas |

### Rutas protegidas por rol

| Ruta | Rol requerido |
|---|---|
| `/`, `/catalogo`, `/libros/**`, `/css/**`, `/images/**`, `/login`, `/registro` | Público (sin autenticación) |
| `/prestamos/**` | `ROLE_CLIENTE` |
| `/multas/**` | `ROLE_CLIENTE` |
| `/perfil/**` | `ROLE_CLIENTE` |
| `/admin/**` | `ROLE_ADMIN` |

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 4.1.2 | Marco de trabajo principal |
| **Spring Data JPA** | — | Acceso y persistencia de datos |
| **Hibernate** | — | Mapeo objeto-relacional |
| **PostgreSQL** | — | Sistema gestor de bases de datos |
| **Spring Security** | — | Autenticación y autorización |
| **Thymeleaf** | — | Motor de plantillas para las vistas web |
| **Spring Validation** | — | Validación de formularios |
| **Maven** | — | Gestión de dependencias y construcción |
| **JUnit 5** | — | Pruebas automatizadas |
| **Mockito** | — | Simulación de dependencias en pruebas |
| **Spring Security Test** | — | Pruebas de seguridad con MockMvc |
| **Git** | — | Control de versiones |
| **GitHub** | — | Alojamiento del repositorio |

## Arquitectura y estructura

El proyecto sigue una **arquitectura por capas** con separación clara de responsabilidades:

```text
biblioteca-digital/
├── src/
│   ├── main/
│   │   ├── java/com/ingsoft/biblioteca/
│   │   │   ├── config/           # Configuración de Spring Security
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/       # Controladores web
│   │   │   │   ├── CatalogoController.java
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── PrestamoController.java
│   │   │   │   ├── MultaController.java
│   │   │   │   ├── UsuarioController.java
│   │   │   │   ├── AdminLibroController.java
│   │   │   │   ├── AdminCategoriaController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── NavegacionControllerAdvice.java
│   │   │   ├── dto/              # Objetos de transferencia de datos
│   │   │   │   ├── LibroForm.java
│   │   │   │   ├── CategoriaForm.java
│   │   │   │   ├── PagoMultaForm.java
│   │   │   │   └── RegistroUsuarioForm.java
│   │   │   ├── entity/           # Entidades JPA
│   │   │   │   ├── Libro.java
│   │   │   │   ├── Categoria.java
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Prestamo.java
│   │   │   │   ├── Multa.java
│   │   │   │   ├── PagoMulta.java
│   │   │   │   ├── EstadoPrestamo.java
│   │   │   │   ├── EstadoMulta.java
│   │   │   │   └── RolUsuario.java
│   │   │   ├── repository/       # Repositorios Spring Data JPA
│   │   │   │   ├── LibroRepository.java
│   │   │   │   ├── CategoriaRepository.java
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   ├── PrestamoRepository.java
│   │   │   │   ├── MultaRepository.java
│   │   │   │   └── PagoMultaRepository.java
│   │   │   ├── security/         # Seguridad personalizada
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── service/          # Lógica de negocio
│   │   │   │   ├── LibroService.java
│   │   │   │   ├── CategoriaService.java
│   │   │   │   ├── UsuarioService.java
│   │   │   │   ├── PrestamoService.java
│   │   │   │   ├── MultaService.java
│   │   │   │   ├── PagoMultaService.java
│   │   │   │   └── DashboardService.java
│   │   │   └── BibliotecaDigitalApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── templates/        # Plantillas Thymeleaf
│   │       │   ├── fragments/
│   │       │   │   └── navegacion.html
│   │       │   ├── catalogo.html
│   │       │   ├── detalle-libro.html
│   │       │   ├── login.html
│   │       │   ├── registro.html
│   │       │   ├── prestamos.html
│   │       │   ├── detalle-prestamo.html
│   │       │   ├── multas.html
│   │       │   ├── detalle-multa.html
│   │       │   ├── perfil.html
│   │       │   ├── admin-dashboard.html
│   │       │   ├── admin-libros.html
│   │       │   ├── admin-libro-form.html
│   │       │   ├── admin-categorias.html
│   │       │   ├── admin-categoria-form.html
│   │       │   ├── admin-prestamos.html
│   │       │   └── admin-multas.html
│   │       └── static/
│   │           └── css/
│   │               ├── navegacion.css
│   │               ├── login.css
│   │               └── administracion.css
│   └── test/
│       ├── java/com/ingsoft/biblioteca/
│       │   ├── controller/       # Pruebas web con MockMvc
│       │   ├── security/         # Pruebas de seguridad
│       │   └── service/          # Pruebas unitarias con Mockito
│       └── resources/
│           └── application-test.properties
├── pom.xml
├── mvnw / mvnw.cmd               # Maven Wrapper
└── README.md
```

### Responsabilidades por capa

- **Controladores (`controller/`):** Reciben las solicitudes HTTP, validan los parámetros y coordinan las respuestas. No contienen lógica de negocio.
- **Servicios (`service/`):** Implementan las reglas de negocio, incluyendo transacciones, bloqueo pesimista y validaciones.
- **Repositorios (`repository/`):** Gestionan el acceso a los datos mediante Spring Data JPA, con consultas JPQL personalizadas.
- **Entidades (`entity/`):** Representan las tablas de la base de datos y sus relaciones.
- **DTOs (`dto/`):** Objetos de transferencia para formularios y validación de entrada.
- **Seguridad (`security/`, `config/`):** Configuración de Spring Security y carga personalizada de usuarios.
- **Plantillas (`templates/`):** Vistas Thymeleaf con fragmentos reutilizables.
- **Estilos (`static/css/`):** Hojas de estilo para la interfaz de usuario.

## Requisitos previos

Antes de ejecutar el proyecto, debes tener instalado:

1. **Java Development Kit (JDK) 21** — Verifica con `java -version`.
2. **PostgreSQL** — El servidor debe estar en ejecución y accesible en `localhost:5432`.
3. **Git** — Para obtener el código fuente y gestionar cambios.
4. **IntelliJ IDEA** (recomendado) — Entorno de desarrollo integrado compatible con Spring Boot.

Maven está disponible mediante el **Maven Wrapper** incluido en el repositorio (`mvnw.cmd`), por lo que no necesitas instalarlo globalmente.

## Instalación y configuración local

### 1. Clonar el repositorio

```powershell
git clone https://github.com/ReyDp/Biblioteca-Digital.git
cd Biblioteca-Digital
```

### 2. Crear las bases de datos

Conecta a PostgreSQL y ejecuta:

```sql
CREATE DATABASE biblioteca_digital;
CREATE DATABASE biblioteca_digital_test;
```

- `biblioteca_digital` se usa para la ejecución normal de la aplicación.
- `biblioteca_digital_test` se usa exclusivamente para las pruebas de integración.

Asegúrate de que el usuario `postgres` tenga permisos sobre ambas bases de datos.

### 3. Configurar `DB_PASSWORD`

La aplicación obtiene la contraseña de PostgreSQL desde la variable de entorno `DB_PASSWORD`. **Nunca escribas la contraseña directamente en los archivos de configuración.**

En Windows PowerShell (válida para la sesión actual):

```powershell
$env:DB_PASSWORD = "TU_CONTRASENA_LOCAL"
```

Si usas **IntelliJ IDEA**, configura la variable en la configuración de ejecución:

1. *Run* → *Edit Configurations*.
2. Selecciona la configuración de `BibliotecaDigitalApplication`.
3. En *Environment variables*, agrega: `DB_PASSWORD=tu_contraseña`.

### 4. Verificar la configuración

El archivo `src/main/resources/application.properties` contiene:

```properties
spring.application.name=biblioteca-digital

spring.datasource.url=jdbc:postgresql://localhost:5432/biblioteca_digital
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

server.port=8080

spring.thymeleaf.cache=false
```

Para las pruebas de integración, `src/test/resources/application-test.properties` apunta a:

```properties
spring.application.name=biblioteca-digital-test

spring.datasource.url=jdbc:postgresql://localhost:5432/biblioteca_digital_test
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

**Importante:** `ddl-auto=create-drop` elimina las tablas administradas por Hibernate al finalizar las pruebas. Esta configuración **nunca** debe apuntar a `biblioteca_digital`.

## Configuración segura de `DB_PASSWORD`

La contraseña de PostgreSQL se gestiona exclusivamente mediante la variable de entorno `DB_PASSWORD`. Esta práctica evita que credenciales reales queden registradas en el historial de Git.

### Reglas de seguridad

- **No** escribas la contraseña en `application.properties` ni en ningún archivo del repositorio.
- **No** incluyas archivos `.env` con credenciales reales.
- **No** compartas el valor de `DB_PASSWORD` en mensajes, capturas de pantalla ni commits.
- Si una credencial se publica accidentalmente, **cámbiala inmediatamente** en PostgreSQL; eliminarla del código en un commit posterior no la elimina del historial de Git.

### Verificar la variable

Antes de ejecutar la aplicación o las pruebas, confirma que la variable está definida:

```powershell
echo $env:DB_PASSWORD
```

Si el resultado está vacío, establécela antes de continuar.

## Creación y configuración de la base de datos

### Base de datos principal: `biblioteca_digital`

Esta base de datos almacena todos los datos de la aplicación en ejecución. Con `ddl-auto=update`, Hibernate crea y modifica automáticamente las tablas según las entidades JPA.

```sql
CREATE DATABASE biblioteca_digital;
```

### Base de datos de pruebas: `biblioteca_digital_test`

Esta base de datos se usa exclusivamente durante las pruebas de integración. Con `ddl-auto=create-drop`, Hibernate crea el esquema al inicio y lo elimina al finalizar, garantizando un estado limpio en cada ejecución.

```sql
CREATE DATABASE biblioteca_digital_test;
```

### Entidades y relaciones

El esquema incluye las siguientes entidades principales:

| Entidad | Tabla | Descripción |
|---|---|---|
| `Libro` | `libros` | Ejemplar físico individual |
| `Categoria` | `categorias` | Categoría del libro |
| `Usuario` | `usuarios` | Cuenta de usuario |
| `Cliente` | `clientes` | Perfil de cliente vinculado a un usuario |
| `Prestamo` | `prestamos` | Préstamo de un ejemplar a un cliente |
| `Multa` | `multas` | Multa asociada a un préstamo |
| `PagoMulta` | `pagos_multas` | Pago registrado contra una multa |

Las relaciones principales son:

- `Libro` → `Categoria` (muchos a uno).
- `Cliente` → `Usuario` (uno a uno).
- `Prestamo` → `Cliente` (muchos a uno).
- `Prestamo` → `Libro` (muchos a uno).
- `Multa` → `Prestamo` (uno a uno).
- `PagoMulta` → `Multa` (muchos a uno).

## Ejecución de la aplicación

### Desde la terminal

Con PostgreSQL en ejecución y `DB_PASSWORD` configurada:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación se inicia en `http://localhost:8080`. Para detenerla, usa `Ctrl + C`.

### Desde IntelliJ IDEA

1. Abre el proyecto en IntelliJ IDEA.
2. Configura `DB_PASSWORD` en las variables de entorno de la configuración de ejecución (*Run* → *Edit Configurations* → *Environment variables*).
3. Ejecuta `BibliotecaDigitalApplication` desde el menú *Run* → *Run*.
4. Abre el navegador en `http://localhost:8080`.

## Pruebas automatizadas

El proyecto incluye **97 pruebas automatizadas** organizadas en tres categorías:

### Pruebas unitarias y web (94 pruebas)

Ejecutan sin necesidad de PostgreSQL, usando Mockito para simular las dependencias:

```powershell
.\mvnw.cmd test
```

Este comando ejecuta las pruebas unitarias de servicios, las pruebas web de controladores con MockMvc y las pruebas de seguridad. Las pruebas etiquetadas con `@Tag("postgres")` se excluyen por defecto.

### Pruebas de integración con PostgreSQL (3 pruebas)

Verifican el comportamiento real de la aplicación con la base de datos. Usan exclusivamente `biblioteca_digital_test` con `ddl-auto=create-drop`:

```powershell
.\mvnw.cmd test -Ppostgres-integration
```

Las clases de integración son:

| Clase | Propósito |
|---|---|
| `BibliotecaDigitalApplicationTests` | Verifica que el ApplicationContext de Spring Boot se carga correctamente |
| `ConexionPostgresTest` | Confirma la conexión a `biblioteca_digital_test` |
| `PrestamoServiceIntegrationTest` | Prueba el flujo completo de devolución tardía con multa en PostgreSQL |

**Advertencia:** Estas pruebas usan `ddl-auto=create-drop`, lo que elimina el esquema al finalizar. **Nunca** deben apuntar a `biblioteca_digital`.

### Ejecutar una prueba concreta

```powershell
.\mvnw.cmd -Dtest=MultaServiceTest test
```

### Cobertura de pruebas

| Categoría | Pruebas |
|---|---|
| Servicios (Mockito) | ~40 |
| Controladores web (MockMvc) | ~35 |
| Seguridad | ~10 |
| Integración PostgreSQL | 3 |
| **Total** | **97** |

## Rutas principales

### Rutas públicas

| Ruta | Método | Descripción |
|---|---|---|
| `/` | GET | Redirección al catálogo |
| `/catalogo` | GET | Catálogo público de ejemplares |
| `/libros/{id}` | GET | Detalle de un ejemplar |
| `/login` | GET/POST | Inicio de sesión |
| `/registro` | GET/POST | Registro de nuevo usuario |

### Rutas de cliente (`ROLE_CLIENTE`)

| Ruta | Método | Descripción |
|---|---|---|
| `/prestamos` | GET | Listado de préstamos propios |
| `/prestamos/{id}` | GET | Detalle de un préstamo propio |
| `/prestamos` | POST | Solicitar préstamo de un ejemplar |
| `/multas` | GET | Listado de multas propias |
| `/multas/{id}` | GET | Detalle de una multa propia |
| `/multas/{id}/pagos` | POST | Registrar un pago contra una multa |
| `/perfil` | GET | Ver perfil personal |
| `/perfil` | POST | Actualizar perfil personal |

### Rutas de administrador (`ROLE_ADMIN`)

| Ruta | Método | Descripción |
|---|---|---|
| `/admin/dashboard` | GET | Panel general con 8 indicadores |
| `/admin/libros` | GET | Listado de todos los ejemplares |
| `/admin/libros/nuevo` | GET | Formulario de registro de ejemplar |
| `/admin/libros` | POST | Registrar un nuevo ejemplar |
| `/admin/libros/{id}/editar` | GET/POST | Editar un ejemplar existente |
| `/admin/categorias` | GET | Listado de todas las categorías |
| `/admin/categorias/nuevo` | GET | Formulario de creación de categoría |
| `/admin/categorias` | POST | Crear una nueva categoría |
| `/admin/categorias/{id}/editar` | GET/POST | Editar una categoría existente |
| `/admin/prestamos` | GET | Listado de préstamos activos |
| `/admin/prestamos/{id}/devolucion` | POST | Registrar devolución de un préstamo |
| `/admin/multas` | GET | Listado de todas las multas |

## Capturas de pantalla

Las capturas de pantalla se agregarán próximamente. A continuación se muestran los marcadores organizados por módulo:

### Catálogo público

- **Catálogo de ejemplares:** `[Agregar captura del catálogo principal]`
- **Detalle de ejemplar:** `[Agregar captura del detalle de un ejemplar]`

### Autenticación

- **Inicio de sesión:** `[Agregar captura del formulario de login]`
- **Registro de usuario:** `[Agregar captura del formulario de registro]`

### Préstamos y devoluciones

- **Listado de préstamos del cliente:** `[Agregar captura]`
- **Detalle de préstamo con multa estimada:** `[Agregar captura]`
- **Administración de préstamos:** `[Agregar captura del listado admin]`

### Multas y pagos

- **Listado de multas del cliente:** `[Agregar captura]`
- **Detalle de multa con pagos:** `[Agregar captura]`

### Panel administrativo

- **Dashboard con indicadores:** `[Agregar captura del panel general]`
- **Administración de libros:** `[Agregar captura]`
- **Administración de categorías:** `[Agregar captura]`

### Perfil

- **Perfil del cliente:** `[Agregar captura]`

## Estado actual y limitaciones conocidas

### Funcionalidades completadas

- ✅ Catálogo público con búsqueda y filtros.
- ✅ Registro e inicio de sesión de usuarios.
- ✅ Roles `CLIENTE` y `ADMIN` con rutas protegidas.
- ✅ Préstamos con prevención de duplicados (bloqueo pesimista).
- ✅ Devoluciones registradas por el administrador.
- ✅ Cálculo de multas estimadas y definitivas ($1.000 COP/día).
- ✅ Pagos parciales con referencia única.
- ✅ Panel administrativo con 8 indicadores de resumen.
- ✅ Administración de libros y categorías.
- ✅ Protección CSRF en todos los formularios POST.
- ✅ Aislamiento de datos entre clientes.
- ✅ 97 pruebas automatizadas (94 unitarias/web + 3 de integración).

### Limitaciones conocidas

- **Sin rate limiting:** Los endpoints `/login` y `/registro` no incluyen protección contra fuerza bruta.
- **Configuración de desarrollo en producción:** Las propiedades `show-sql=true`, `ddl-auto=update` y `thymeleaf.cache=false` están optimizadas para desarrollo. Para producción, se recomienda usar perfiles separados de Spring Boot.
- **Sin usuarios de demostración:** La aplicación no incluye cuentas preconfiguradas; el administrador debe crear los usuarios manualmente o mediante el registro público.
- **Capturas de pantalla pendientes:** La sección de capturas contiene marcadores que deben completarse con imágenes reales.

## Autor

**Reynaldo José Durán Pertuz**

Desarrollador en formación, enfocado en el desarrollo backend con Java y Spring Boot.

- GitHub: [@ReyDp](https://github.com/ReyDp)
- Proyecto: [Biblioteca Digital](https://github.com/ReyDp/Biblioteca-Digital)

---

Este proyecto continúa en desarrollo. La documentación se actualizará conforme se incorporen y verifiquen nuevas funcionalidades.
