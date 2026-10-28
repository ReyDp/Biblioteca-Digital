# Biblioteca Digital

Sistema de gestión bibliotecaria desarrollado con Java y Spring Boot, orientado a la administración de libros, préstamos, devoluciones y multas. El proyecto utiliza PostgreSQL para la persistencia de datos y pruebas automatizadas para verificar el comportamiento de sus servicios.

> **Estado del proyecto:** En desarrollo.

## Tabla de contenido

- [Descripción](#descripción)
- [Objetivos](#objetivos)
- [Funcionalidades](#funcionalidades)
- [Tecnologías](#tecnologías)
- [Arquitectura y estructura](#arquitectura-y-estructura)
- [Requisitos previos](#requisitos-previos)
- [Instalación y configuración](#instalación-y-configuración)
- [Ejecución de la aplicación](#ejecución-de-la-aplicación)
- [Pruebas automatizadas](#pruebas-automatizadas)
- [Configuración de seguridad](#configuración-de-seguridad)
- [Control de versiones](#control-de-versiones)
- [Autor](#autor)

## Descripción

Biblioteca Digital es una aplicación desarrollada como proyecto de software para gestionar procesos habituales de una biblioteca. Su implementación utiliza el ecosistema Spring para organizar la lógica de negocio, acceder a los datos y mantener una separación entre los componentes de la aplicación.

El proyecto contempla la gestión de préstamos y devoluciones, así como el cálculo y registro de multas por retraso en la devolución de libros.

La aplicación utiliza PostgreSQL como sistema gestor de bases de datos y JUnit y Mockito para las pruebas automatizadas de los servicios.

## Objetivos

- Implementar una solución de software para apoyar la gestión de una biblioteca.
- Organizar el código mediante una arquitectura por capas y responsabilidades.
- Gestionar los préstamos y las devoluciones de libros.
- Calcular las multas de acuerdo con los días de retraso.
- Aplicar pruebas automatizadas para verificar las reglas de negocio.
- Utilizar buenas prácticas de desarrollo, control de versiones y gestión de credenciales.

## Funcionalidades

Las siguientes funcionalidades corresponden a los componentes y servicios identificados en la implementación actual:

- **Gestión de préstamos:** registro y consulta de préstamos.
- **Registro de devoluciones:** actualización del estado del préstamo y registro de la fecha de devolución.
- **Cálculo de multas:** determinación de los días de retraso y del importe correspondiente.
- **Registro de multas definitivas:** cálculo de la multa al registrar la devolución, evitando duplicar una multa existente para el mismo préstamo.
- **Consulta de multas:** consulta de multas registradas y cálculo estimado para préstamos que todavía no se han devuelto.
- **Gestión del estado de las multas:** manejo de estados como pendiente y pagada.
- **Pruebas automatizadas:** pruebas unitarias y de integración para verificar los servicios y su interacción con la base de datos.

La cobertura funcional completa de la aplicación, incluidas las interfaces y los módulos adicionales, se documentará a medida que se verifiquen sus implementaciones.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje de programación |
| Spring Boot | Configuración y ejecución de la aplicación |
| Spring Data JPA | Acceso y persistencia de datos |
| Hibernate | Mapeo objeto-relacional |
| PostgreSQL | Sistema gestor de bases de datos |
| Thymeleaf | Motor de plantillas para las vistas web |
| Maven | Gestión de dependencias y construcción |
| JUnit 5 | Pruebas automatizadas |
| Mockito | Simulación de dependencias en pruebas |
| Git | Control de versiones |
| GitHub | Alojamiento del repositorio |

## Arquitectura y estructura

El proyecto organiza sus responsabilidades mediante componentes de la aplicación, servicios de negocio y repositorios de persistencia.

La estructura general esperada del proyecto es la siguiente:

```text
Biblioteca-Digital/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ingsoft/biblioteca/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── model/
│   │   │       └── ...
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── templates/
│   │       └── static/
│   └── test/
│       ├── java/
│       └── resources/
│           └── application.properties
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

> Nota: el árbol anterior es orientativo. Los paquetes y directorios deben ajustarse a la estructura real del código fuente.

### Responsabilidades principales

- **Controladores:** reciben las solicitudes y coordinan las respuestas de la aplicación web.
- **Servicios:** implementan las reglas de negocio de préstamos, devoluciones y multas.
- **Repositorios:** gestionan el acceso a los datos mediante Spring Data JPA.
- **Modelos y entidades:** representan los objetos de dominio y las entidades persistidas.
- **Pruebas:** verifican el comportamiento de los servicios y la integración con PostgreSQL.

## Requisitos previos

Antes de ejecutar el proyecto, debes tener instalado:

1. **Java Development Kit (JDK) 21.**
2. **PostgreSQL**, con el servidor en ejecución.
3. **Git**, para obtener el código fuente y gestionar sus cambios.
4. Un entorno de desarrollo como IntelliJ IDEA, recomendado para trabajar con el proyecto.

Maven está disponible mediante el *Maven Wrapper* incluido en el repositorio, por lo que no necesitas instalar Maven globalmente.

Puedes verificar Java y Git con estos comandos:

```bash
java -version
git --version
```

## Instalación y configuración

### 1. Clonar el repositorio

Abre una terminal y ejecuta:

```bash
git clone https://github.com/ReyDp/Biblioteca-Digital.git
```

Entra en el directorio:

```bash
cd Biblioteca-Digital
```

### 2. Crear las bases de datos

En PostgreSQL, crea las bases de datos utilizadas por el entorno de desarrollo y las pruebas:

```sql
CREATE DATABASE biblioteca_digital;
CREATE DATABASE biblioteca_digital_test;
```

La primera se utiliza para la ejecución normal de la aplicación. La segunda está destinada a las pruebas de integración.

Asegúrate de que el usuario de PostgreSQL tenga los permisos necesarios para conectarse y operar sobre ambas bases de datos.

### 3. Configurar las credenciales

La aplicación utiliza la variable de entorno `DB_PASSWORD` para obtener la contraseña del usuario configurado en PostgreSQL.

No escribas la contraseña directamente en `application.properties` ni la publiques en el repositorio.

En Windows PowerShell, puedes definir la variable para la sesión actual:

```powershell
$env:DB_PASSWORD = "TU_CONTRASENA_LOCAL"
```

Sustituye el texto de ejemplo por la contraseña de tu entorno local. No la compartas ni la incluyas en los commits.

Si ejecutas la aplicación desde IntelliJ IDEA, configura también `DB_PASSWORD` en las variables de entorno de la configuración de ejecución correspondiente.

### 4. Verificar la configuración de Spring Boot

El archivo `src/main/resources/application.properties` contiene la configuración de la aplicación principal.

La configuración de conexión debe tener esta forma:

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

Estos valores corresponden a la configuración local de desarrollo. Ajusta el usuario, el puerto y la dirección de PostgreSQL si tu instalación es diferente.

Para las pruebas, el archivo `src/test/resources/application.properties` debe apuntar exclusivamente a `biblioteca_digital_test`:

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

**Importante:** `create-drop` puede eliminar las tablas administradas por Hibernate al finalizar las pruebas. Verifica que la configuración de pruebas nunca apunte a la base de datos principal.

## Ejecución de la aplicación

Con PostgreSQL en ejecución y `DB_PASSWORD` configurada, inicia la aplicación desde la raíz del proyecto.

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Cuando Spring Boot termine de iniciar correctamente, abre en el navegador:

```text
http://localhost:8080
```

La ruta inicial y las páginas disponibles dependerán de los controladores implementados en el proyecto.

Para detener la aplicación, utiliza `Ctrl + C` en la terminal donde se está ejecutando.

## Pruebas automatizadas

El proyecto incluye pruebas unitarias y de integración para comprobar la lógica de negocio.

Para ejecutar todas las pruebas con Maven Wrapper en Windows:

```powershell
.\mvnw.cmd test
```

Para ejecutar una clase de prueba concreta:

```powershell
.\mvnw.cmd -Dtest=MultaServiceTest test
```

Las pruebas unitarias permiten comprobar reglas de negocio utilizando dependencias simuladas cuando corresponde. Las pruebas de integración verifican el comportamiento de los servicios junto con Spring Boot y PostgreSQL.

Para que las pruebas de integración funcionen, debes tener disponible PostgreSQL, creada la base `biblioteca_digital_test` y configurada la variable `DB_PASSWORD`.

## Configuración de seguridad

Para mantener el repositorio seguro:

- No publiques contraseñas, tokens ni claves privadas.
- No incluyas archivos `.env` con credenciales reales.
- Mantén las contraseñas fuera de los archivos versionados.
- Verifica que las configuraciones de desarrollo y pruebas apunten a sus respectivas bases de datos.
- Revisa los archivos antes de ejecutar `git add .` y antes de publicar un commit.

Si alguna credencial real se publica accidentalmente, cambiarla o revocarla es necesario; eliminarla del archivo en un commit posterior no la elimina del historial de Git.

## Control de versiones

El código fuente está alojado en GitHub:

**Repositorio:** [ReyDp/Biblioteca-Digital](https://github.com/ReyDp/Biblioteca-Digital)

Para consultar el estado de los cambios:

```bash
git status
```

Para registrar modificaciones:

```bash
git add .
git commit -m "feat: describe el cambio realizado"
git push origin main
```

Utiliza mensajes de commit descriptivos que expliquen los cambios realizados.

## Autor

**Reynaldo José Durán Pertuz**

Desarrollador en formación, enfocado en el desarrollo backend con Java y Spring Boot.

- GitHub: [@ReyDp](https://github.com/ReyDp)
- Proyecto: [Biblioteca Digital](https://github.com/ReyDp/Biblioteca-Digital)

---

Este proyecto continúa en desarrollo. La documentación se actualizará conforme se incorporen y verifiquen nuevas funcionalidades.
