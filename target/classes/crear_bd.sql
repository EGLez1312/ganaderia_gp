/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  Elena González
 * Created: 3 ene 2026
 */

CREATE DATABASE IF NOT EXISTS ganaderia_gp;
USE ganaderia_gp;

-- Tabla usuario (obligatoria)
CREATE TABLE usuario (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    nombre VARCHAR(100),
    apellidos VARCHAR(100),
    ultima_conexion TIMESTAMP NULL,
    activo BOOLEAN DEFAULT TRUE
);

-- Tabla oveja
CREATE TABLE oveja (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numero_identificacion VARCHAR(20) UNIQUE NOT NULL,
    raza VARCHAR(50),
    fecha_nacimiento DATE,
    sexo CHAR(1),
    peso_actual DECIMAL(5,2),
    estado_salud VARCHAR(50)
);

-- Tabla evento
CREATE TABLE evento (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_oveja INT,
    tipo_evento VARCHAR(30),
    fecha DATE,
    detalles VARCHAR(200),
    FOREIGN KEY (id_oveja) REFERENCES oveja(id) ON DELETE CASCADE
);

-- Datos de prueba (usuario admin)
INSERT INTO usuario (username, password, email, nombre, apellidos) 
VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@ganaderia.com', 'Admin', 'Sistema');
-- Password en texto plano: "password" (encriptada con BCrypt)
