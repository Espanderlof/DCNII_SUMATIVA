-- --------------------------------------------------------
-- Script SQL para Oracle - Sistema de Gestión de Usuarios y Roles
-- Prefijo de tablas: sum
-- --------------------------------------------------------

-- Crear secuencias para IDs
CREATE SEQUENCE sum_seq_usuario
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE sum_seq_rol
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- Crear tabla de roles
CREATE TABLE sum_roles (
    id_rol          NUMBER PRIMARY KEY,
    nombre          VARCHAR2(50) NOT NULL UNIQUE,
    descripcion     VARCHAR2(200),
    fecha_creacion  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    activo          NUMBER(1) DEFAULT 1 NOT NULL CHECK (activo IN (0, 1))
);

-- Crear tabla de usuarios
CREATE TABLE sum_usuarios (
    id_usuario      NUMBER PRIMARY KEY,
    username        VARCHAR2(50) NOT NULL UNIQUE,
    email           VARCHAR2(100) NOT NULL UNIQUE,
    nombre          VARCHAR2(100),
    apellido        VARCHAR2(100),
    password_hash   VARCHAR2(100) NOT NULL,
    fecha_creacion  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    ultimo_login    TIMESTAMP,
    activo          NUMBER(1) DEFAULT 1 NOT NULL CHECK (activo IN (0, 1))
);

-- Crear tabla de relación entre usuarios y roles (muchos a muchos)
CREATE TABLE sum_usuario_rol (
    id_usuario      NUMBER,
    id_rol          NUMBER,
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (id_usuario) 
        REFERENCES sum_usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (id_rol) 
        REFERENCES sum_roles (id_rol) ON DELETE CASCADE
);

-- Comentarios en tablas y columnas para documentación
COMMENT ON TABLE sum_roles IS 'Tabla que almacena los roles del sistema';
COMMENT ON COLUMN sum_roles.id_rol IS 'Identificador único del rol';
COMMENT ON COLUMN sum_roles.nombre IS 'Nombre único del rol';
COMMENT ON COLUMN sum_roles.descripcion IS 'Descripción del rol y sus permisos';
COMMENT ON COLUMN sum_roles.fecha_creacion IS 'Fecha de creación del rol';
COMMENT ON COLUMN sum_roles.fecha_modificacion IS 'Fecha de última modificación del rol';
COMMENT ON COLUMN sum_roles.activo IS 'Indica si el rol está activo (1) o inactivo (0)';

COMMENT ON TABLE sum_usuarios IS 'Tabla que almacena los usuarios del sistema';
COMMENT ON COLUMN sum_usuarios.id_usuario IS 'Identificador único del usuario';
COMMENT ON COLUMN sum_usuarios.username IS 'Nombre de usuario único para login';
COMMENT ON COLUMN sum_usuarios.email IS 'Correo electrónico único del usuario';
COMMENT ON COLUMN sum_usuarios.nombre IS 'Nombre del usuario';
COMMENT ON COLUMN sum_usuarios.apellido IS 'Apellido del usuario';
COMMENT ON COLUMN sum_usuarios.password_hash IS 'Hash de la contraseña del usuario';
COMMENT ON COLUMN sum_usuarios.fecha_creacion IS 'Fecha de creación del usuario';
COMMENT ON COLUMN sum_usuarios.fecha_modificacion IS 'Fecha de última modificación del usuario';
COMMENT ON COLUMN sum_usuarios.ultimo_login IS 'Fecha y hora del último inicio de sesión';
COMMENT ON COLUMN sum_usuarios.activo IS 'Indica si el usuario está activo (1) o inactivo (0)';

COMMENT ON TABLE sum_usuario_rol IS 'Tabla de relación entre usuarios y roles';
COMMENT ON COLUMN sum_usuario_rol.id_usuario IS 'ID del usuario relacionado';
COMMENT ON COLUMN sum_usuario_rol.id_rol IS 'ID del rol asignado';
COMMENT ON COLUMN sum_usuario_rol.fecha_asignacion IS 'Fecha de asignación del rol al usuario';

-- Crear triggers para actualizar automáticamente fecha_modificacion
CREATE OR REPLACE TRIGGER trg_sum_usuarios_update
BEFORE UPDATE ON sum_usuarios
FOR EACH ROW
BEGIN
    :NEW.fecha_modificacion := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_sum_roles_update
BEFORE UPDATE ON sum_roles
FOR EACH ROW
BEGIN
    :NEW.fecha_modificacion := CURRENT_TIMESTAMP;
END;
/

-- Insertar datos iniciales para pruebas (roles predefinidos)
INSERT INTO sum_roles (id_rol, nombre, descripcion) 
VALUES (sum_seq_rol.NEXTVAL, 'ADMIN', 'Administrador con acceso completo al sistema');

INSERT INTO sum_roles (id_rol, nombre, descripcion) 
VALUES (sum_seq_rol.NEXTVAL, 'USER', 'Usuario estándar con acceso limitado');

INSERT INTO sum_roles (id_rol, nombre, descripcion) 
VALUES (sum_seq_rol.NEXTVAL, 'GUEST', 'Usuario invitado con acceso de solo lectura');

-- Insertar un usuario administrador para pruebas
-- Nota: En producción, usar un algoritmo seguro para el hash de contraseñas.
-- Este es solo un ejemplo con un hash simulado (admin123)
INSERT INTO sum_usuarios (id_usuario, username, email, nombre, apellido, password_hash) 
VALUES (sum_seq_usuario.NEXTVAL, 'admin', 'admin@example.com', 'Administrador', 'Sistema', 
        'e8c7ffb067e36be0a70e9f411ed12a3e76c2e3288df20a3fa44e85b9d34bd12a');

-- Asignar rol administrador al usuario administrador
INSERT INTO sum_usuario_rol (id_usuario, id_rol) 
VALUES (1, 1);

-- Confirmar la transacción
COMMIT;

-- Crear o reemplazar procedimientos almacenados para operaciones comunes

-- Procedimiento para crear un nuevo usuario
CREATE OR REPLACE PROCEDURE sum_crear_usuario(
    p_username IN VARCHAR2,
    p_email IN VARCHAR2,
    p_nombre IN VARCHAR2,
    p_apellido IN VARCHAR2,
    p_password_hash IN VARCHAR2,
    p_id_usuario OUT NUMBER
)
AS
BEGIN
    SELECT sum_seq_usuario.NEXTVAL INTO p_id_usuario FROM DUAL;
    
    INSERT INTO sum_usuarios (
        id_usuario,
        username,
        email,
        nombre,
        apellido,
        password_hash
    ) VALUES (
        p_id_usuario,
        p_username,
        p_email,
        p_nombre,
        p_apellido,
        p_password_hash
    );
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sum_crear_usuario;
/

-- Procedimiento para asignar un rol a un usuario
CREATE OR REPLACE PROCEDURE sum_asignar_rol(
    p_id_usuario IN NUMBER,
    p_id_rol IN NUMBER
)
AS
    v_count NUMBER;
BEGIN
    -- Verificar si el usuario existe
    SELECT COUNT(*) INTO v_count FROM sum_usuarios WHERE id_usuario = p_id_usuario;
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'El usuario no existe');
    END IF;
    
    -- Verificar si el rol existe
    SELECT COUNT(*) INTO v_count FROM sum_roles WHERE id_rol = p_id_rol;
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'El rol no existe');
    END IF;
    
    -- Verificar si ya tiene asignado ese rol
    SELECT COUNT(*) INTO v_count FROM sum_usuario_rol 
    WHERE id_usuario = p_id_usuario AND id_rol = p_id_rol;
    
    IF v_count = 0 THEN
        INSERT INTO sum_usuario_rol (id_usuario, id_rol) 
        VALUES (p_id_usuario, p_id_rol);
        
        COMMIT;
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sum_asignar_rol;
/

-- Procedimiento para quitar un rol a un usuario
CREATE OR REPLACE PROCEDURE sum_quitar_rol(
    p_id_usuario IN NUMBER,
    p_id_rol IN NUMBER
)
AS
BEGIN
    DELETE FROM sum_usuario_rol 
    WHERE id_usuario = p_id_usuario AND id_rol = p_id_rol;
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END sum_quitar_rol;
/

-- Función para verificar si un usuario tiene un rol específico
CREATE OR REPLACE FUNCTION sum_usuario_tiene_rol(
    p_id_usuario IN NUMBER,
    p_id_rol IN NUMBER
) RETURN NUMBER
AS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM sum_usuario_rol 
    WHERE id_usuario = p_id_usuario AND id_rol = p_id_rol;
    
    RETURN v_count;
END sum_usuario_tiene_rol;
/
