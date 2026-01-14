# SIGLO-F Backend
**Sistema Integral de Gestión Logística, Operativa y Fidelización**

Este repositorio contiene el backend del sistema SIGLO-F, diseñado para optimizar la distribución de gas, controlar inventarios y gestionar flotas y ventas.

## 🚀 Estado del Proyecto
**Nivel de Desarrollo: FASE 2 COMPLETADA (Operaciones y Logística)**

El sistema actualmente soporta:
1.  **Fase 1: Cimientos y Administración**
    *   Seguridad IAM con JWT (Roles: SUPER_ADMIN, ADMIN, REPARTIDOR).
    *   Gestión de Catálogos Maestros (Clientes, Productos, Vehículos).
2.  **Fase 2: Operaciones y Logística**
    *   **Inventario Central:** Kardex de movimientos y control de stock en planta.
    *   **Gestión de Rutas:** Asignación de carga a vehículos y choferes (Salida de Almacén).
    *   **Ventas Móviles:** Registro de ventas en ruta con validación de stock vehicular.
    *   **Liquidación:** Arqueo de caja y retorno de stock al cerrar la ruta.

---

## 🛠 Tech Stack
*   **Lenguaje:** Java 17
*   **Framework:** Spring Boot 3.3.0
*   **Base de Datos:** PostgreSQL
*   **Seguridad:** Spring Security + JWT
*   **Build Tool:** Maven

## 🔒 Seguridad y Roles
El sistema implementa Control de Acceso Basado en Roles (RBAC):

1.  **SUPER_ADMIN**: Acceso total. Puede administrar usuarios, clientes, productos y ver todos los reportes.
2.  **ADMIN**: Gestión operativa (Inventarios, Rutas, Liquidación). No puede administrar usuarios.
3.  **REPARTIDOR**: Acceso limitado.
    *   Solo puede ver rutas activas.
    *   Solo puede registrar ventas en SU propia ruta asignada (Validación de identidad).
    *   No puede alterar inventarios de planta ni cerrar rutas manualmente.

## 🚀 Optimizaciones
*   **Cálculo de Liquidaciones:** Se utilizan consultas JPQL/Nativas para sumar ventas directamente en la base de datos, evitando procesar listas en memoria para mayor velocidad.


---

## 📚 Documentación de API

Todas las rutas están prefijadas con: `/api/v1`

### 1. Autenticación y Seguridad (`/auth`)
#### Iniciar Sesión
*   **Endpoint:** `POST /api/v1/auth/authenticate`
*   **Body:**
    ```json
    {
      "username": "admin",
      "password": "password123"
    }
    ```
*   **Respuesta:**
    ```json
    { "token": "eyJhGcF..." }
    ```

#### Registrar Usuario (Solo Admins)
*   **Endpoint:** `POST /api/v1/auth/register`
*   **Body:**
    ```json
    {
      "fullName": "Juan Repartidor",
      "username": "juanr",
      "password": "securePass",
      "role": "REPARTIDOR" // SUPER_ADMIN, ADMIN, REPARTIDOR
    }
    ```

---

### 2. Inventario (`/inventory`)
#### Registrar Movimiento (Compra / Ajuste)
*   **Endpoint:** `POST /api/v1/inventory/movement`
*   **Descripción:** Registra entradas o ajustes de stock en la planta principal.
*   **Body:**
    ```json
    {
      "productId": 1,
      "quantity": 500,
      "type": "PURCHASE", // PURCHASE, ADJUSTMENT
      "reason": "Factura Compra #001"
    }
    ```

---

### 3. Logística y Rutas (`/routes`)
#### Abrir Ruta (Carga de Camión)
*   **Endpoint:** `POST /api/v1/routes/open`
*   **Descripción:** Asigna stock a un vehículo y conductor, descontándolo del inventario de planta.
*   **Body:**
    ```json
    {
      "vehicleId": 1,
      "driverId": 2,
      "stock": [
        { "productId": 1, "quantity": 50 },
        { "productId": 2, "quantity": 20 }
      ]
    }
    ```

#### Listar Rutas Activas
*   **Endpoint:** `GET /api/v1/routes/active`
*   **Respuesta:** Lista de rutas con estado `OPEN`.

---

### 4. Ventas (`/sales`)
#### Registrar Venta
*   **Endpoint:** `POST /api/v1/sales`
*   **Descripción:** Registra una venta realizada por un repartidor, validando que tenga stock suficiente en su camión.
*   **Body:**
    ```json
    {
      "routeId": 1,
      "clientId": 10,
      "paymentMethod": "CASH", // CASH, YAPE, PLIN, CREDIT
      "items": [
        { "productId": 1, "quantity": 1 }
      ]
    }
    ```

---

### 5. Liquidación (`/liquidation`)
#### Cerrar Ruta y Liquidar
*   **Endpoint:** `POST /api/v1/liquidation/close`
*   **Descripción:** Finaliza la ruta, calcula lo vendido vs lo retornado y reingresa el sobrante a planta.
*   **Body:**
    ```json
    {
      "routeId": 1,
      "savedStock": [
        { "productId": 1, "quantity": 49 }, // Sobrante físico
        { "productId": 2, "quantity": 20 }
      ]
    }
    ```

---

### 6. Catálogos Maestros
*   **Clientes:** `GET /api/v1/clients`, `POST /api/v1/clients`
*   **Productos:** `GET /api/v1/products`, `POST /api/v1/products`
*   **Vehículos:** `GET /api/v1/vehicles`, `POST /api/v1/vehicles`

---

### 7. Usuarios (`/users`)
#### Ver mi perfil
*   **Endpoint:** `GET /api/v1/users/me`
*   **Respuesta:** Datos del usuario logueado.

#### Gestión de Usuarios (SOLO SUPER ADMIN)
*   **Listar:** `GET /api/v1/users`
*   **Crear:** `POST /api/v1/users` (Mismo body que Register)
*   **Editar:** `PUT /api/v1/users/{id}`
    *   **Body:**
        ```json
        {
          "fullName": "Nuevo Nombre",
          "role": "ADMIN",
          "active": false,
          "password": "newPassword" // Opcional
        }
        ```

---

## 👨‍💻 Créditos
**Desarrollado por Eddam para ccalarce**
