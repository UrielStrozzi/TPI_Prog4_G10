-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: subastas_db
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `slug` varchar(120) DEFAULT NULL,
  `padre_id` int(11) DEFAULT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `updated_at` datetime(3) NOT NULL DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_categorias_nombre` (`nombre`),
  UNIQUE KEY `uq_categorias_slug` (`slug`),
  KEY `idx_categorias_padre_id` (`padre_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Categorías jerárquicas. padre_id NULL = raíz. Extensible a árbol N niveles.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
INSERT INTO `categorias` VALUES (1,'Electrónica','Dispositivos electrónicos y tecnología','electronica',NULL,1,'2026-06-17 21:23:35.101','2026-06-17 21:23:35.101'),(2,'Ropa y Moda','Indumentaria, calzado y accesorios','ropa-moda',NULL,1,'2026-06-17 21:23:35.101','2026-06-17 21:23:35.101'),(3,'Hogar','Muebles, decoración y artículos del hogar','hogar',NULL,1,'2026-06-17 21:23:35.101','2026-06-17 21:23:35.101'),(4,'Deportes','Equipamiento deportivo y ropa deportiva','deportes',NULL,1,'2026-06-17 21:23:35.101','2026-06-17 21:23:35.101'),(5,'Automotores','Vehículos, repuestos y accesorios','automotores',NULL,1,'2026-06-17 21:23:35.101','2026-06-17 21:23:35.101'),(6,'Arte y Antigüedades','Obras de arte, coleccionables y antigüedades','arte-antiguedades',NULL,1,'2026-06-17 21:23:35.101','2026-06-17 21:23:35.101'),(7,'Otros','Artículos varios no categorizados','otros',NULL,1,'2026-06-17 21:23:35.101','2026-06-17 21:23:35.101'),(8,'Instrumentos Musicales','Guitarras, pianos, vientos y más',NULL,NULL,1,'2026-06-30 19:33:03.281','2026-06-30 19:33:03.281');
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `disputas`
--

DROP TABLE IF EXISTS `disputas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `disputas` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `subasta_id` bigint(20) unsigned NOT NULL,
  `iniciador_id` bigint(20) unsigned NOT NULL,
  `motivo` varchar(300) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `estado` enum('ABIERTA','RESUELTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
  `resuelto_por_id` bigint(20) unsigned DEFAULT NULL,
  `resolucion` text DEFAULT NULL,
  `estado_final_subasta` enum('ADJUDICADA','FINALIZADA','CANCELADA') DEFAULT NULL,
  `fecha_apertura` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `fecha_resolucion` datetime(3) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `updated_at` datetime(3) NOT NULL DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3),
  PRIMARY KEY (`id`),
  KEY `fk_disputas_resuelto_por` (`resuelto_por_id`),
  KEY `idx_disputas_subasta_id` (`subasta_id`),
  KEY `idx_disputas_estado` (`estado`),
  KEY `idx_disputas_iniciador_id` (`iniciador_id`),
  CONSTRAINT `fk_disputas_iniciador` FOREIGN KEY (`iniciador_id`) REFERENCES `usuarios` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_disputas_resuelto_por` FOREIGN KEY (`resuelto_por_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_disputas_subasta` FOREIGN KEY (`subasta_id`) REFERENCES `subastas` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Disputas sobre subastas adjudicadas. Solo un ADMIN puede resolver.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `disputas`
--

LOCK TABLES `disputas` WRITE;
/*!40000 ALTER TABLE `disputas` DISABLE KEYS */;
/*!40000 ALTER TABLE `disputas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historial_estados_subasta`
--

DROP TABLE IF EXISTS `historial_estados_subasta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_estados_subasta` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `subasta_id` bigint(20) unsigned NOT NULL,
  `estado_anterior` enum('BORRADOR','PUBLICADA','ACTIVA','FINALIZADA','CANCELADA','ADJUDICADA','EN_DISPUTA') DEFAULT NULL,
  `estado_nuevo` enum('BORRADOR','PUBLICADA','ACTIVA','FINALIZADA','CANCELADA','ADJUDICADA','EN_DISPUTA') NOT NULL,
  `usuario_id` bigint(20) unsigned DEFAULT NULL,
  `motivo` text DEFAULT NULL,
  `fecha` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  PRIMARY KEY (`id`),
  KEY `fk_he_usuario` (`usuario_id`),
  KEY `idx_he_subasta_id` (`subasta_id`),
  KEY `idx_he_fecha` (`fecha`),
  CONSTRAINT `fk_he_subasta` FOREIGN KEY (`subasta_id`) REFERENCES `subastas` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_he_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Historial inmutable de transiciones de estado por subasta. Nunca se borra.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historial_estados_subasta`
--

LOCK TABLES `historial_estados_subasta` WRITE;
/*!40000 ALTER TABLE `historial_estados_subasta` DISABLE KEYS */;
/*!40000 ALTER TABLE `historial_estados_subasta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notificaciones`
--

DROP TABLE IF EXISTS `notificaciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificaciones` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint(20) unsigned NOT NULL,
  `subasta_id` bigint(20) unsigned DEFAULT NULL,
  `tipo` enum('SUBASTA_GANADA','SUBASTA_ADJUDICADA','PUJA_SUPERADA','SUBASTA_CANCELADA','DISPUTA_ABIERTA','DISPUTA_RESUELTA','SUBASTA_POR_CERRAR','SISTEMA') NOT NULL,
  `titulo` varchar(200) NOT NULL,
  `cuerpo` text DEFAULT NULL,
  `estado` enum('PENDIENTE','LEIDA','ARCHIVADA') NOT NULL DEFAULT 'PENDIENTE',
  `leida_at` datetime(3) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  PRIMARY KEY (`id`),
  KEY `idx_notif_usuario_leida` (`usuario_id`),
  KEY `idx_notif_usuario_fecha` (`usuario_id`,`created_at`),
  KEY `idx_notif_subasta_id` (`subasta_id`),
  CONSTRAINT `fk_notif_subasta` FOREIGN KEY (`subasta_id`) REFERENCES `subastas` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_notif_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notificaciones en BD. Escalar a canales externos sin modificar esta tabla.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notificaciones`
--

LOCK TABLES `notificaciones` WRITE;
/*!40000 ALTER TABLE `notificaciones` DISABLE KEYS */;
/*!40000 ALTER TABLE `notificaciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `vendedor_id` bigint(20) unsigned NOT NULL,
  `categoria_id` smallint(5) unsigned NOT NULL,
  `titulo` varchar(200) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `condicion` enum('NUEVO','USADO','REACONDICIONADO') NOT NULL DEFAULT 'USADO',
  `deleted_at` datetime(3) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `updated_at` datetime(3) NOT NULL DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3),
  PRIMARY KEY (`id`),
  KEY `idx_productos_vendedor_id` (`vendedor_id`),
  KEY `idx_productos_categoria_id` (`categoria_id`),
  KEY `idx_productos_deleted_at` (`deleted_at`),
  CONSTRAINT `fk_productos_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_productos_vendedor` FOREIGN KEY (`vendedor_id`) REFERENCES `usuarios` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Productos publicables en subastas. Escalar con imágenes, atributos, etc.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,2,1,'MacBook Pro 14 M3 — Precio actualizado','Batería al 95%. Incluye cargador original.','USADO','2026-06-30 23:02:09.000','2026-06-30 22:49:28.000','2026-06-30 23:02:09.000'),(2,2,1,'Ipgone Pro 14 Max','Excelente estado. 4GB RAM, 512GB SSD. Caja original.','USADO',NULL,'2026-07-02 01:09:02.000','2026-07-02 01:09:02.000');
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pujas`
--

DROP TABLE IF EXISTS `pujas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pujas` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `subasta_id` bigint(20) unsigned NOT NULL,
  `usuario_id` bigint(20) unsigned NOT NULL,
  `monto` decimal(15,2) NOT NULL,
  `fecha_hora` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `estado` enum('CONFIRMADA','ANULADA') NOT NULL DEFAULT 'CONFIRMADA',
  `ip_origen` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pujas_subasta_estado_monto` (`subasta_id`,`estado`,`monto`),
  KEY `idx_pujas_subasta_fecha` (`subasta_id`,`fecha_hora`),
  KEY `idx_pujas_usuario_id` (`usuario_id`),
  CONSTRAINT `fk_pujas_subasta` FOREIGN KEY (`subasta_id`) REFERENCES `subastas` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_pujas_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_pujas_monto` CHECK (`monto` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Pujas registradas. Las operaciones de INSERT son siempre transaccionales con bloqueo.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pujas`
--

LOCK TABLES `pujas` WRITE;
/*!40000 ALTER TABLE `pujas` DISABLE KEYS */;
INSERT INTO `pujas` VALUES (1,2,3,155000.00,'2026-07-02 01:39:01.000','CONFIRMADA',NULL),(2,2,3,158000.00,'2026-07-02 01:41:04.000','CONFIRMADA',NULL);
/*!40000 ALTER TABLE `pujas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(30) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `updated_at` datetime(3) NOT NULL DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_roles_nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Roles del sistema. Agregar nuevos roles con INSERT sin tocar código.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'USER','Usuario estándar: puede ver subastas y realizar pujas','2026-06-17 21:23:35.094','2026-06-17 21:23:35.094'),(2,'SELLER','Vendedor: puede publicar productos y crear subastas','2026-06-17 21:23:35.094','2026-06-17 21:23:35.094'),(3,'ADMIN','Administrador: puede moderar, suspender usuarios y resolver disputas','2026-06-17 21:23:35.094','2026-06-17 21:23:35.094');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subastas`
--

DROP TABLE IF EXISTS `subastas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subastas` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `producto_id` bigint(20) unsigned NOT NULL,
  `vendedor_id` bigint(20) unsigned NOT NULL,
  `precio_base` decimal(15,2) NOT NULL,
  `incremento_minimo` decimal(15,2) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `fecha_inicio` datetime(3) NOT NULL,
  `fecha_cierre` datetime(3) NOT NULL,
  `estado` enum('BORRADOR','PUBLICADA','ACTIVA','FINALIZADA','CANCELADA','ADJUDICADA','EN_DISPUTA') NOT NULL DEFAULT 'BORRADOR',
  `monto_actual` decimal(15,2) DEFAULT NULL,
  `ganador_id` bigint(20) unsigned DEFAULT NULL,
  `fecha_adjudicacion` datetime(3) DEFAULT NULL,
  `precio_final` decimal(15,2) DEFAULT NULL,
  `cancelado_por_id` bigint(20) unsigned DEFAULT NULL,
  `motivo_cancelacion` text DEFAULT NULL,
  `fecha_cancelacion` datetime(3) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `updated_at` datetime(3) NOT NULL DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3),
  PRIMARY KEY (`id`),
  KEY `fk_subastas_cancelado_por` (`cancelado_por_id`),
  KEY `idx_subastas_estado` (`estado`),
  KEY `idx_subastas_estado_inicio` (`estado`,`fecha_inicio`),
  KEY `idx_subastas_estado_cierre` (`estado`,`fecha_cierre`),
  KEY `idx_subastas_vendedor_id` (`vendedor_id`),
  KEY `idx_subastas_ganador_id` (`ganador_id`),
  KEY `idx_subastas_producto_id` (`producto_id`),
  CONSTRAINT `fk_subastas_cancelado_por` FOREIGN KEY (`cancelado_por_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_subastas_ganador` FOREIGN KEY (`ganador_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_subastas_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_subastas_vendedor` FOREIGN KEY (`vendedor_id`) REFERENCES `usuarios` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_subastas_fechas` CHECK (`fecha_cierre` > `fecha_inicio`),
  CONSTRAINT `chk_subastas_precio_base` CHECK (`precio_base` > 0),
  CONSTRAINT `chk_subastas_incremento` CHECK (`incremento_minimo` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Subastas. monto_actual y ganador_id se actualizan en cada puja transaccional.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subastas`
--

LOCK TABLES `subastas` WRITE;
/*!40000 ALTER TABLE `subastas` DISABLE KEYS */;
INSERT INTO `subastas` VALUES (1,1,2,150000.00,5000.00,NULL,'2025-06-28 21:00:00.000','2025-06-28 21:05:00.000','BORRADOR',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-07-01 01:20:48.000','2026-07-01 01:20:48.000'),(2,2,2,100000.00,3000.00,NULL,'2026-07-02 01:33:00.000','2026-07-02 01:48:37.000','ADJUDICADA',158000.00,3,'2026-07-02 01:48:53.000',158000.00,NULL,NULL,NULL,'2026-07-02 01:10:59.000','2026-07-02 01:48:53.000');
/*!40000 ALTER TABLE `subastas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_roles`
--

DROP TABLE IF EXISTS `usuario_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_roles` (
  `usuario_id` bigint(20) unsigned NOT NULL,
  `rol_id` tinyint(3) unsigned NOT NULL,
  `asignado_at` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `asignado_por` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `fk_ur_rol` (`rol_id`),
  KEY `fk_ur_asignado_por` (`asignado_por`),
  CONSTRAINT `fk_ur_asignado_por` FOREIGN KEY (`asignado_por`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_ur_rol` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_ur_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabla pivote usuario ↔ rol (N:M). Soporta multi-rol por usuario.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_roles`
--

LOCK TABLES `usuario_roles` WRITE;
/*!40000 ALTER TABLE `usuario_roles` DISABLE KEYS */;
INSERT INTO `usuario_roles` VALUES (1,1,'2026-06-29 14:49:57.513',NULL),(1,3,'2026-06-29 22:26:05.350',NULL),(2,1,'2026-06-30 19:23:48.066',NULL),(2,2,'2026-06-30 19:23:48.065',NULL),(3,1,'2026-06-29 14:55:08.576',NULL),(4,1,'2026-07-01 20:36:59.143',NULL);
/*!40000 ALTER TABLE `usuario_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `apellido` varchar(100) DEFAULT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `bloqueado` tinyint(1) NOT NULL DEFAULT 0,
  `motivo_bloqueo` varchar(500) DEFAULT NULL,
  `bloqueado_por` bigint(20) unsigned DEFAULT NULL,
  `bloqueado_at` datetime(3) DEFAULT NULL,
  `deleted_at` datetime(3) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT current_timestamp(3),
  `updated_at` datetime(3) NOT NULL DEFAULT current_timestamp(3) ON UPDATE current_timestamp(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_usuarios_username` (`username`),
  UNIQUE KEY `uq_usuarios_email` (`email`),
  KEY `fk_usuarios_bloqueado_por` (`bloqueado_por`),
  KEY `idx_usuarios_email` (`email`),
  KEY `idx_usuarios_username` (`username`),
  KEY `idx_usuarios_bloqueado` (`bloqueado`),
  KEY `idx_usuarios_deleted_at` (`deleted_at`),
  CONSTRAINT `fk_usuarios_bloqueado_por` FOREIGN KEY (`bloqueado_por`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Usuarios del sistema. Escalar con columnas extra sin romper estructura.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'admin_root','admin@subastas.com','$2a$10$tV1x8Gj0i/QpYeKIUaHPr.ejm1/fUaPNLxeK4xoRAqr8mzQ.dvUz2','Admin','Sistema',NULL,0,NULL,NULL,NULL,NULL,'2026-06-29 17:49:57.000','2026-06-29 17:49:57.000'),(2,'seller_root','seller@subastas.com','$2a$10$t3cG2aUl35Gw8bP0ROdJmuuhJWYUmJ1i8mbEluPpbjRRsVQ7djQdm','Seller','Sistema',NULL,0,NULL,NULL,NULL,NULL,'2026-06-29 17:54:55.000','2026-06-30 22:23:48.000'),(3,'user_root','user@subastas.com','$2a$10$eNJx/acK3QFxEZ04lAz6h./JpvQRP1dYm5XFii2mX0d2zeveLI.66','User','Sistema',NULL,0,NULL,NULL,NULL,NULL,'2026-06-29 17:55:08.000','2026-06-30 22:32:02.000'),(4,'uri12','urielstrozzi@gmail.com','$2a$10$vHR.qsMNXF5/slJz9gk7duk5emSTBew7JHnObQ.mm1Ii0SAEzMbbe','Uriel','Strozzi',NULL,0,NULL,NULL,NULL,NULL,'2026-07-01 23:36:59.000','2026-07-01 23:36:59.000');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_mis_pujas`
--

DROP TABLE IF EXISTS `v_mis_pujas`;
/*!50001 DROP VIEW IF EXISTS `v_mis_pujas`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_mis_pujas` AS SELECT 
 1 AS `puja_id`,
 1 AS `usuario_id`,
 1 AS `subasta_id`,
 1 AS `monto`,
 1 AS `fecha_hora`,
 1 AS `estado_puja`,
 1 AS `estado_subasta`,
 1 AS `monto_actual`,
 1 AS `es_oferta_lider`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_subastas_publicas`
--

DROP TABLE IF EXISTS `v_subastas_publicas`;
/*!50001 DROP VIEW IF EXISTS `v_subastas_publicas`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_subastas_publicas` AS SELECT 
 1 AS `subasta_id`,
 1 AS `estado`,
 1 AS `precio_base`,
 1 AS `incremento_minimo`,
 1 AS `monto_actual`,
 1 AS `fecha_inicio`,
 1 AS `fecha_cierre`,
 1 AS `subasta_descripcion`,
 1 AS `publicada_at`,
 1 AS `producto_id`,
 1 AS `producto_titulo`,
 1 AS `producto_condicion`,
 1 AS `categoria_nombre`,
 1 AS `vendedor_id`,
 1 AS `vendedor_username`,
 1 AS `total_pujas`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping routines for database 'subastas_db'
--

--
-- Final view structure for view `v_mis_pujas`
--

/*!50001 DROP VIEW IF EXISTS `v_mis_pujas`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_mis_pujas` AS select `pj`.`id` AS `puja_id`,`pj`.`usuario_id` AS `usuario_id`,`pj`.`subasta_id` AS `subasta_id`,`pj`.`monto` AS `monto`,`pj`.`fecha_hora` AS `fecha_hora`,`pj`.`estado` AS `estado_puja`,`s`.`estado` AS `estado_subasta`,`s`.`monto_actual` AS `monto_actual`,`pj`.`id` = (select `pujas`.`id` from `pujas` where `pujas`.`subasta_id` = `pj`.`subasta_id` and `pujas`.`estado` = 'CONFIRMADA' order by `pujas`.`monto` desc,`pujas`.`fecha_hora` limit 1) AS `es_oferta_lider` from (`pujas` `pj` join `subastas` `s` on(`s`.`id` = `pj`.`subasta_id`)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_subastas_publicas`
--

/*!50001 DROP VIEW IF EXISTS `v_subastas_publicas`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_subastas_publicas` AS select `s`.`id` AS `subasta_id`,`s`.`estado` AS `estado`,`s`.`precio_base` AS `precio_base`,`s`.`incremento_minimo` AS `incremento_minimo`,`s`.`monto_actual` AS `monto_actual`,`s`.`fecha_inicio` AS `fecha_inicio`,`s`.`fecha_cierre` AS `fecha_cierre`,`s`.`descripcion` AS `subasta_descripcion`,`s`.`created_at` AS `publicada_at`,`p`.`id` AS `producto_id`,`p`.`titulo` AS `producto_titulo`,`p`.`condicion` AS `producto_condicion`,`c`.`nombre` AS `categoria_nombre`,`u`.`id` AS `vendedor_id`,`u`.`username` AS `vendedor_username`,(select count(0) from `pujas` where `pujas`.`subasta_id` = `s`.`id` and `pujas`.`estado` = 'CONFIRMADA') AS `total_pujas` from (((`subastas` `s` join `productos` `p` on(`p`.`id` = `s`.`producto_id`)) join `categorias` `c` on(`c`.`id` = `p`.`categoria_id`)) join `usuarios` `u` on(`u`.`id` = `s`.`vendedor_id`)) where `s`.`estado` <> 'BORRADOR' and `p`.`deleted_at` is null and `u`.`deleted_at` is null */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-01 23:26:54
