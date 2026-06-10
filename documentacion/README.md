# Helpdesk App

Aplicación de escritorio para la gestión de incidencias IT, desarrollada en Java con Swing, JDBC y patrón DAO.

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 26 | Lenguaje principal |
| Swing + FlatLaf | 3.4.1 | Interfaz gráfica con tema claro/oscuro |
| JDBC | — | Acceso a base de datos |
| MySQL Connector/J | 9.7.0 | Driver JDBC para MySQL |
| MySQL (Docker) | 8.x | Base de datos |

---

## Estructura del proyecto

```
src/
├── Main.java               # Punto de entrada
├── db/
│   └── ConexionDB.java     # Gestión de la conexión JDBC
├── model/                  # Entidades del dominio
│   ├── Usuario.java
│   ├── Empleado.java
│   ├── Tecnico.java
│   ├── Incidencia.java
│   ├── Asignacion.java
│   ├── Rol.java
│   ├── Prioridad.java
│   └── Estado.java
├── dao/                    # Interfaces e implementaciones DAO
│   ├── UsuarioDAO.java / UsuarioDAOImpl.java
│   ├── EmpleadoDAO.java / EmpleadoDAOImpl.java
│   ├── TecnicoDAO.java / TecnicoDAOImpl.java
│   ├── IncidenciaDAO.java / IncidenciaDAOImpl.java
│   └── AsignacionDAO.java / AsignacionDAOImpl.java
├── dto/                    # DTOs para consultas con JOIN
│   ├── IncidenciaDTO.java
│   └── AsignacionDTO.java
├── view/                   # Ventanas Swing
│   ├── Login.java
│   ├── Register.java
│   └── Dashboard.java
└── util/
    └── Validador.java      # Validación centralizada de datos
lib/
└── mysql-connector-j-9.7.0.jar
documentacion/
├── README.md
├── documentacion.pdf
└── documentacion.docx
database.sql                # Script de creación e inserción de datos
```

---

## Arquitectura

La aplicación sigue el patrón **MVC** con **DAO**:

- **Model** — clases Java que representan las tablas de la BD.
- **DAO** — interfaces que definen las operaciones CRUD; las implementaciones (`*DAOImpl`) contienen el SQL.
- **DTO** — objetos de transferencia usados cuando un JOIN devuelve columnas de varias tablas (p.ej. nombre del empleado junto a su incidencia).
- **View** — ventanas Swing que llaman a los DAOs directamente.

### Herencia de tablas (Joined Table Inheritance)

`usuarios` es la tabla base. `empleados` y `tecnicos` extienden `usuarios` mediante una FK `usuario_id` con `ON DELETE CASCADE`. En Java, `Empleado` y `Tecnico` extienden `Usuario`.

---

## Instalación y ejecución

### 1. Levantar MySQL con Docker

```bash
docker run --name helpdesk-mysql \
  -e MYSQL_ROOT_PASSWORD=<tu_password> \
  -e MYSQL_DATABASE=helpdesk \
  -p 3306:3306 \
  -d mysql:8
```

### 2. Crear tablas e insertar datos de prueba

```bash
mysql -u <tu_usuario> -p helpdesk < database.sql
```

O ejecutar el script `database.sql` directamente desde MySQL Workbench.

### 3. Configurar credenciales

Crear el fichero `src/config.properties` (no incluido en el repositorio) con el siguiente formato:

```properties
db.url=jdbc:mysql://localhost:3306/helpdesk
db.user=<tu_usuario>
db.password=<tu_password>
```

> Este fichero está en `.gitignore`.

### 4. Compilar

```bash
# Linux / Mac
javac -encoding UTF-8 -cp "lib/mysql-connector-j-9.7.0.jar" -d bin $(find src -name "*.java")

# Windows (PowerShell)
$files = Get-ChildItem -Recurse -Filter "*.java" src | % { $_.FullName }
javac -encoding UTF-8 -cp "lib/mysql-connector-j-9.7.0.jar" -d bin $files
```

### 5. Ejecutar

```bash
# Linux / Mac
java -cp "bin:lib/mysql-connector-j-9.7.0.jar" Main

# Windows
java -cp "bin;lib/mysql-connector-j-9.7.0.jar" Main
```

---

## Funcionalidades

### Autenticación
- **Login** con validación de credenciales contra la BD.
- **Registro** de nuevos usuarios con validación de formato (email, DNI, teléfono, contraseña).
- Transacción al registrar: si falla el insert en la tabla hija (`empleados`/`tecnicos`), se hace rollback del insert en `usuarios`.

### Control de acceso por rol

| Funcionalidad | EMPLEADO | TECNICO |
|---|---|---|
| Ver sus propias incidencias | ✓ | ✓ |
| Ver todas las incidencias | — | ✓ |
| Crear incidencia | ✓ (en su nombre) | ✓ |
| Editar / Eliminar incidencia | — | ✓ |
| Gestionar Asignaciones | — | ✓ |
| Gestionar Empleados | — | ✓ |
| Gestionar Técnicos | — | ✓ |

### Dashboard (TECNICO)
- **4 pestañas**: Incidencias, Asignaciones, Empleados, Técnicos.
- **CRUD completo** en cada pestaña: Add, Edit, Delete, Details.
- **Botón Refresh** para recargar todas las tablas.
- **Botón de tema** para alternar entre modo oscuro y claro sin reiniciar la ventana.
- La pestaña Asignaciones muestra **todas las incidencias**, incluyendo las que aún no tienen técnico asignado ("Sin asignar").

### Validación de datos
Centralizada en `util/Validador.java`. Se aplica en todos los formularios de alta y edición:
- Campos obligatorios no vacíos.
- Username mínimo 3 caracteres.
- Contraseña mínimo 4 caracteres.
- Email con formato válido (`texto@dominio.ext`).
- DNI: 8 dígitos + 1 letra (`12345678A`).
- Teléfono: exactamente 9 dígitos.

---

## Datos de prueba

El script `database.sql` incluye los siguientes usuarios de prueba:

| Username | Password | Rol |
|---|---|---|
| jgarcia | 1234 | EMPLEADO |
| mlopez | 1234 | EMPLEADO |
| crodriguez | 1234 | TECNICO |
| amoreno | 1234 | TECNICO |
