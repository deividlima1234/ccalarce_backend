# Documentación de API - SIGLO-F Backend

Este documento detalla los endpoints disponibles en el backend, los roles necesarios para acceder a ellos, y el formato de datos esperado.

## Información General
*   **Base URL:** `/api/v1`
*   **Formato de Fecha:** ISO-8601 (`yyyy-MM-ddTHH:mm:ss`) para campos `LocalDateTime`.
*   **Autenticación:** Bearer Token (JWT) requerido en el header `Authorization` para todos los endpoints excepto `/auth/**`.
*   **Roles:**
    *   `SUPER_ADMIN`: Acceso total.
    *   `ADMIN`: Acceso a operaciones (Inventario, Rutas) y Catálogos.
    *   `REPARTIDOR`: Acceso limitado a Ventas y Rutas asignadas.

---

## 1. Autenticación (`/auth`)
No requiere token.

### 1.1 Iniciar Sesión
*   **URL:** `POST /api/v1/auth/authenticate`
*   **Acceso:** Público
*   **Descripción:** Intercambia credenciales por un token JWT.
*   **Request Body:**
    ```json
    {
      "username": "admin",      // String, Requerido
      "password": "password123" // String, Requerido
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "token": "eyJhGcF..." // JWT Token
    }
    ```

### 1.2 Registro de Usuario
*   **URL:** `POST /api/v1/auth/register`
*   **Acceso:** Público (Idealmente debería restringirse o ser solo `SUPER_ADMIN` tras el primer despliegue, pero por diseño actual es público para onboarding inicial o uso interno).
*   **Request Body:**
    ```json
    {
      "fullName": "Juan Perez",
      "username": "juanp",
      "password": "secretPassword",
      "role": "REPARTIDOR" // Enum: SUPER_ADMIN, ADMIN, REPARTIDOR
    }
    ```
*   **Response (200 OK):**
    ```json
    { "token": "eyJhGcF..." }
    ```

---

## 2. Gestión de Usuarios (`/users`)

### 2.1 Ver Perfil Propio
*   **URL:** `GET /api/v1/users/me`
*   **Acceso:** Cualquier usuario autenticado.
*   **Response (200 OK):**
    ```json
    {
      "id": 1,
      "username": "juanp",
      "fullName": "Juan Perez",
      "role": "REPARTIDOR",
      "active": true
    }
    ```

### 2.2 Listar Usuarios (Admin)
*   **URL:** `GET /api/v1/users`
*   **Acceso:** `SUPER_ADMIN`, `ADMIN` (Nuevo: Admin puede listar para asignar choferes).
*   **Response (200 OK):** Lista de objetos UserDto (ver 2.1).

### 2.3 Crear Usuario (Super Admin)
*   **URL:** `POST /api/v1/users`
*   **Acceso:** `SUPER_ADMIN`
*   **Request Body:** Mismo que `RegisterRequest` (ver 1.2).

### 2.4 Editar Usuario
*   **URL:** `PUT /api/v1/users/{id}`
*   **Acceso:** `SUPER_ADMIN`
*   **Request Body:**
    ```json
    {
      "fullName": "Juan Perez Modificado", // Opcional
      "role": "ADMIN",                     // Opcional
      "active": false,                     // Opcional (true/false)
      "password": "newPassword123"         // Opcional (si se envía, se resetea)
    }
    ```

---

## 3. Inventario (`/inventory`)

### 3.1 Registrar Movimiento
*   **URL:** `POST /api/v1/inventory/movement`
*   **Acceso:** `SUPER_ADMIN`, `ADMIN`
*   **Auditoría:** Se registra automáticamente como `REGISTER_MOVEMENT`.
*   **Descripción:** Afecta directamente al stock en planta.
*   **Request Body:**
    ```json
    {
      "productId": 1,         // ID del Producto
      "quantity": 500,        // Cantidad (Positiva)
      "type": "PURCHASE",     // Enum: PURCHASE, ADJUSTMENT, RETURN, ROUTE_LOAD
      "reason": "Factura 001" // String
    }
    ```

---

## 8. Auditoría (`/audit`) - [NUEVO]

### 8.1 Ver Logs de Auditoría
*   **URL:** `GET /api/v1/audit`
*   **Acceso:** `SUPER_ADMIN` (Exclusivo)
*   **Descripción:** Lista cronológica de todas las acciones críticas del sistema (Creación de usuarios, ventas, movimientos, cierres).
*   **Response:**
    ```json
    [
      {
        "id": 15,
        "username": "admin_user",
        "action": "REGISTER_SALE",
        "resource": "SaleService",
        "details": "Args: [...] | Result: {...}",
        "timestamp": "2023-11-01T10:00:00"
      }
    ]
    ```
    *Nota: `ROUTE_LOAD` descuenta stock, `PURCHASE`, `RETURN`, `ADJUSTMENT` suman (normalmente).*

---

## 4. Gestión de Rutas (`/routes`)

### 4.1 Abrir Ruta (Cargar Camión)
*   **URL:** `POST /api/v1/routes/open`
*   **Acceso:** `SUPER_ADMIN`, `ADMIN`
*   **Descripción:** Crea una ruta, asigna stock inicial al camión y lo descuenta de planta.
*   **Request Body:**
    ```json
    {
      "vehicleId": 1,
      "driverId": 2, // ID de un usuario con rol REPARTIDOR
      "stock": [
        { "productId": 1, "quantity": 50 },
        { "productId": 2, "quantity": 20 }
      ]
    }
    ```

### 4.2 Listar Rutas Activas
*   **URL:** `GET /api/v1/routes/active`
*   **Acceso:** Todos los autenticados (Repartidores ven todas para seleccionar la suya en la app, o se filtra en frontend).
*   **Response (200 OK):**
    ```json
    [
      {
        "id": 10,
        "status": "OPEN",
        "openedAt": "2023-10-27T08:00:00",
        "vehicle": { ... },
        "driver": { "id": 2, "fullName": "..." },
        "stock": [ { "product": {...}, "currentQuantity": 50 } ]
      }
    ]
    ```

---

## 5. Ventas (`/sales`)

### 5.1 Registrar Venta Móvil
*   **URL:** `POST /api/v1/sales`
*   **Acceso:** `REPARTIDOR`, `ADMIN`, `SUPER_ADMIN`
*   **Restricción:** Si el usuario es `REPARTIDOR`, solo puede vender en una ruta donde `driver.id == user.id`.
*   **Request Body:**
    ```json
    {
      "routeId": 10,
      "clientId": 5,
      "paymentMethod": "CASH", // Enum: CASH, YAPE, PLIN, CREDIT
      "items": [
        { "productId": 1, "quantity": 1 }
      ]
    }
    ```
*   **Efecto:** Reduce `currentQuantity` en `RouteStock` (Inventario del camión), NO afecta planta.

---

## 6. Liquidación (`/liquidation`)

### 6.1 Cerrar Ruta
*   **URL:** `POST /api/v1/liquidation/close`
*   **Acceso:** `SUPER_ADMIN`, `ADMIN`, `REPARTIDOR` (Nuevo: Choferes liquidan su propia ruta).
*   **Auditoría:** Se registra como `CLOSE_ROUTE`.
*   **Descripción:** Finaliza la jornada. Calcula diferencias y retorna stock sobrante a planta.
*   **Request Body:**
    ```json
    {
      "routeId": 10,
      "savedStock": [ // Stock físico que vuelve en el camión
        { "productId": 1, "quantity": 49 } 
      ]
    }
    ```
*   **Response:** Objeto `Liquidation` con totales monetarios calculados (Efectivo vs Digital).

---

## 7. Catálogos Maestros

### 7.1 Clientes (`/clients`)
*   **GET** `/api/v1/clients`: Listar (Todos)
*   **POST** `/api/v1/clients`: Crear (Admin/Super)
*   **PUT** `/api/v1/clients/{id}`: Actualizar (Admin/Super)
*   **DELETE** `/api/v1/clients/{id}`: Eliminar (Admin/Super)
*   **Campos:** `name`, `address`, `phone`, `ruc`, `email`.

### 7.2 Productos (`/products`)
*   **GET** `/api/v1/products`: Listar (Todos)
*   **POST** `/api/v1/products`: Crear (Admin/Super)
*   **Campos:** `name` (ej: "Balón 10kg"), `exchangable` (true/false), `price` (Precio Base), `stock` (Stock actual en planta).

### 7.3 Vehículos (`/vehicles`)
*   **GET** `/api/v1/vehicles`: Listar (Todos)
*   **POST** `/api/v1/vehicles`: Crear (Admin/Super)
*   **Campos:** `plate` (Placa), `model`, `brand`, `active`.

---
**Desarrollado por Eddam para ccalarce**
