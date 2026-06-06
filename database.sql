-- ============================================================
-- Helpdesk App - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS helpdesk;
USE helpdesk;

-- ============================================================
-- BASE TABLE: usuarios
-- ============================================================
CREATE TABLE usuarios (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email    VARCHAR(100) NOT NULL UNIQUE,
    nombre   VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni      VARCHAR(9)   NOT NULL UNIQUE,
    rol      ENUM('EMPLEADO', 'TECNICO') NOT NULL
);

-- ============================================================
-- CHILD TABLE: empleados  (Joined Table Inheritance)
-- ============================================================
CREATE TABLE empleados (
    usuario_id   INT         PRIMARY KEY,
    departamento VARCHAR(100) NOT NULL,
    telefono     VARCHAR(15)  NOT NULL,
    CONSTRAINT fk_empleados_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE CASCADE
);

-- ============================================================
-- CHILD TABLE: tecnicos  (Joined Table Inheritance)
-- ============================================================
CREATE TABLE tecnicos (
    usuario_id   INT         PRIMARY KEY,
    especialidad VARCHAR(100) NOT NULL,
    CONSTRAINT fk_tecnicos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE CASCADE
);

-- ============================================================
-- MAIN ENTITY: incidencias
-- ============================================================
CREATE TABLE incidencias (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    empleado_id    INT          NOT NULL,
    titulo         VARCHAR(150) NOT NULL,
    descripcion    TEXT         NOT NULL,
    prioridad      ENUM('BAJA', 'MEDIA', 'ALTA') NOT NULL DEFAULT 'MEDIA',
    estado         ENUM('ABIERTA', 'EN_PROGRESO', 'RESUELTA') NOT NULL DEFAULT 'ABIERTA',
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incidencias_empleado
        FOREIGN KEY (empleado_id) REFERENCES empleados(usuario_id)
        ON DELETE CASCADE
);

-- ============================================================
-- N:M RELATION: asignaciones (incidencias <-> tecnicos)
-- ============================================================
CREATE TABLE asignaciones (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    incidencia_id    INT          NOT NULL,
    tecnico_id       INT          NOT NULL,
    fecha_asignacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comentario       TEXT,
    CONSTRAINT fk_asignaciones_incidencia
        FOREIGN KEY (incidencia_id) REFERENCES incidencias(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_asignaciones_tecnico
        FOREIGN KEY (tecnico_id) REFERENCES tecnicos(usuario_id)
        ON DELETE CASCADE
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- usuarios (passwords are plain text for now)
INSERT INTO usuarios (username, password, email, nombre, apellidos, dni, rol) VALUES
('jgarcia',   '1234', 'jgarcia@empresa.com',   'Juan',  'García López',    '12345678A', 'EMPLEADO'),
('mlopez',    '1234', 'mlopez@empresa.com',    'María', 'López Martínez',  '87654321B', 'EMPLEADO'),
('crodriguez','1234', 'crodriguez@empresa.com','Carlos','Rodríguez Pérez', '11223344C', 'TECNICO'),
('amoreno',   '1234', 'amoreno@empresa.com',   'Ana',   'Moreno Sánchez',  '44332211D', 'TECNICO');

-- empleados
INSERT INTO empleados (usuario_id, departamento, telefono) VALUES
(1, 'Contabilidad', '600111222'),
(2, 'Recursos Humanos', '600333444');

-- tecnicos
INSERT INTO tecnicos (usuario_id, especialidad) VALUES
(3, 'Redes y Sistemas'),
(4, 'Hardware y Soporte');

-- incidencias
INSERT INTO incidencias (empleado_id, titulo, descripcion, prioridad, estado) VALUES
(1, 'PC no enciende', 'El ordenador del puesto 3 no arranca desde esta mañana.', 'ALTA', 'ABIERTA'),
(1, 'Sin acceso a internet', 'No hay conexión a internet en toda la planta 2.', 'ALTA', 'EN_PROGRESO'),
(2, 'Impresora atascada', 'La impresora de RRHH tiene un atasco de papel recurrente.', 'MEDIA', 'ABIERTA'),
(2, 'Error en aplicación nóminas', 'La aplicación de nóminas da error al exportar a PDF.', 'BAJA', 'RESUELTA');

-- asignaciones
INSERT INTO asignaciones (incidencia_id, tecnico_id, comentario) VALUES
(1, 3, 'Revisado fuente de alimentación, pendiente de repuesto.'),
(2, 3, 'Problema en el switch de planta 2, en proceso.'),
(2, 4, 'Apoyo en diagnóstico de red.'),
(3, 4, 'Limpieza del rodillo programada para mañana.');
