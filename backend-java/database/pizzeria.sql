-- =========================================
-- Base de datos del Sistema Pizzería
-- Ejecutar completo: borra y recrea las tablas con los datos iniciales.
-- =========================================
CREATE DATABASE IF NOT EXISTS pizzeria
  CHARACTER SET utf8mb4            -- permite tildes y ñ en nombres y descripciones
  COLLATE utf8mb4_unicode_ci;

USE pizzeria;

-- Se borran primero para poder ejecutar el script varias veces
-- (por ejemplo, antes de cada ensayo) y empezar siempre con los mismos datos.
DROP TABLE IF EXISTS Pizza;
DROP TABLE IF EXISTS Usuario;

-- -----------------------------------------
-- Tabla Pizza: el catálogo del menú
-- -----------------------------------------
CREATE TABLE Pizza (
  Id INT AUTO_INCREMENT PRIMARY KEY,   -- MySQL genera el id solo y nunca lo repite
  Nombre VARCHAR(50) NOT NULL,         -- una pizza sin nombre no se puede mostrar
  Precio INT NOT NULL,                 -- pesos sin centavos; evita tarjetas sin precio
  Imagen VARCHAR(100),                 -- solo el nombre del archivo, ej. 'hawaiana.jpg'
  Descripcion VARCHAR(200)             -- opcional
) ENGINE=InnoDB;

-- -----------------------------------------
-- Tabla Usuario: los administradores
-- -----------------------------------------
CREATE TABLE Usuario (
  Id INT AUTO_INCREMENT PRIMARY KEY,
  Nombre VARCHAR(50) NOT NULL,
  Apellido VARCHAR(50) NOT NULL,
  Correo VARCHAR(100) NOT NULL UNIQUE, -- el login se hace con el correo: no puede repetirse
  Clave VARCHAR(100) NOT NULL          -- texto plano por alcance del proyecto; en producción iría con hash (ej. BCrypt)
) ENGINE=InnoDB;

-- -----------------------------------------
-- Datos iniciales: las pizzas del menú
-- No se escribe el Id porque es AUTO_INCREMENT: lo asigna MySQL.
-- Imagen debe coincidir con los archivos de frontend-vue/public/images/pizzas/
-- -----------------------------------------
INSERT INTO Pizza (Nombre, Precio, Imagen, Descripcion) VALUES
  ('Hawaiana', 32000, 'hawaiana.jpg', 'Queso mozzarella, jamón y piña'),
  ('Pepperoni', 30000, 'pepperoni.jpg', 'Salsa de tomate, mozzarella y pepperoni'),
  ('Mexicana', 34000, 'mexicana.jpg', 'Salsa de tomate, mozzarella, carne molida, fríjol, jalapeño, maíz y cilantro'),
  ('Carnes', 36000, 'carnes.jpg', 'Salsa de tomate, mozzarella, carne desmechada, chorizo y tocineta'),
  ('Costillas BBQ', 38000, 'costillas.jpg', 'Salsa BBQ, mozzarella, costilla de cerdo desmechada y cebolla morada'),
  ('Pollo y Champiñón', 40000, 'pollo-champinon.jpg', 'Salsa de tomate, mozzarella, pollo desmechado y champiñones');

-- -----------------------------------------
-- Primer administrador
-- No hay pantalla de registro, así que se crea directamente aquí.
-- -----------------------------------------
INSERT INTO Usuario (Nombre, Apellido, Correo, Clave) VALUES
  ('Jhon', 'Cardenas', 'admin@prueba.com', 'admin123');