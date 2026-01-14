# Guía de Implementación Frontend: Roles y APIs

Este documento sirve como guía para el equipo de Frontend sobre qué pantallas, botones y acciones mostrar a cada tipo de usuario, y qué APIs consumir en cada caso.

## 🎭 Roles y Permisos de UI

El sistema maneja 3 roles jerárquicos. El frontend debe ocultar/mostrar elementos del menú basándose en el campo `role` del token JWT o del endpoint `/me`.

### 1. SUPER_ADMIN (Gerencia / TI)
**"El que todo lo ve y todo lo puede."**

*   **Acceso UI:**
    *   ✅ **Dashboard Completo:** Métricas globales.
    *   ✅ **Gestión de Usuarios:** Crear, editar, desactivar usuarios (Admins y Repartidores).
    *   ✅ **Auditoría:** Ver logs de transacciones (`/audit`).
    *   ✅ **Operaciones:** Puede hacer todo lo que hace un Admin (emergencias).
    *   ✅ **Catálogos:** CRUD completo de Productos, Clientes, Vehículos.

*   **Restricciones:** Ninguna.

### 2. ADMIN (Jefe de Planta / Supervisor)
**"El encargado de la operación diaria."**

*   **Acceso UI:**
    *   ✅ **Dashboard Operativo:** Inventario actual, Rutas del día.
    *   ✅ **Inventario:** Registrar ingresos (Compras) y salidas manuales.
    *   ✅ **Rutas:** "Abrir Ruta" (Cargar camión y asignar chofer).
    *   ✅ **Liquidación:** Aprobar/Cerrar la liquidación al final del día (Revisión de dinero).
    *   ✅ **Catálogos:** Puede crear/editar Clientes y Vehículos.
    *   ⚠️ **Usuarios:** SOLO lectura. Puede ver la lista para asignar choferes a rutas, pero **NO** puede crear ni editar usuarios.

*   **Restricciones:** 
    *   ⛔ Sin acceso a "Registrar Nuevo Usuario" (Botón oculto).
    *   ⛔ Sin acceso a "Auditoría".

### 3. REPARTIDOR (Chofer / Vendedor)
**"El usuario móvil en calle."**

*   **Acceso UI (App Móvil / Vista Móvil):**
    *   ✅ **Mi Ruta:** Ver su ruta activa del día.
    *   ✅ **Venta:** Registrar ventas  a clientes.
    *   ✅ **Cierre:** Enviar solicitud de cierre/liquidación al volver a planta.

*   **Restricciones:**
    *   ⛔ NO ve inventario general de planta.
    *   ⛔ NO ve usuarios ni catálogos.
    *   ⛔ NO puede abrir rutas. Solo opera sobre la ruta que le asignaron.

---

## 🔌 Mapeo de APIs por Rol

A continuación, la lista detallada de qué endpoints debe consumir el frontend para cada perfil.

### A. Para SUPER_ADMIN
| Acción UI | Endpoint | Método | Cuerpo (Ejemplo) |
| :--- | :--- | :--- | :--- |
| **Ver Auditoría** | `/api/v1/audit` | `GET` | N/A |
| **Crear Usuario** | `/api/v1/users` | `POST` | `{ "username": "...", "role": "ADMIN" }` |
| **Editar Usuario** | `/api/v1/users/{id}` | `PUT` | `{ "active": false }` |
| **Listar Usuarios** | `/api/v1/users` | `GET` | N/A |
| *+ Todo lo de Admin* | | | |

### B. Para ADMIN
| Acción UI | Endpoint | Método | Cuerpo (Ejemplo) |
| :--- | :--- | :--- | :--- |
| **Abastecer Planta** | `/api/v1/inventory/movement` | `POST` | `{ "type": "PURCHASE", "quantity": 1000 }` |
| **Cargar Camión** | `/api/v1/routes/open` | `POST` | `{ "vehicleId": 1, "driverId": 5, "stock": [...] }` |
| **Listar Choferes** | `/api/v1/users` | `GET` | *(Para el dropdown de "Asignar Chofer")* |
| **Dashboard/Rutas** | `/api/v1/routes/active` | `GET` | N/A |
| **Crear Cliente** | `/api/v1/clients` | `POST` | `{ "name": "Tienda Don Pepe", "ruc": "..." }` |
| **Crear Producto** | `/api/v1/products` | `POST` | `{ "name": "Gas 10kg", "price": 45.0 }` |

### C. Para REPARTIDOR
| Acción UI | Endpoint | Método | Notas Importantes |
| :--- | :--- | :--- | :--- |
| **Ver Mi Ruta** | `/api/v1/routes/active` | `GET` | El frontend debe filtrar la lista donde `driver.id === me.id`. |
| **Vender** | `/api/v1/sales` | `POST` | `{ "routeId": 10, "items": [{"productId":1, "quantity":1}] }` <br> *Solo funciona si la ruta es suya.* |
| **Liquidar** | `/api/v1/liquidation/close` | `POST` | `{ "routeId": 10, "savedStock": [...] }` <br> *Devuelve los sobrantes.* |

---
**Notas Técnicas:**
1.  Si un **ADMIN** intenta crear un usuario, el backend devolverá `403 Forbidden`. El frontend debe capturar esto o, mejor aún, no mostrar el formulario.
2.  Si un **REPARTIDOR** intenta vender en una ruta ajena (hackeando la petición), recibirá `500/403` "Access Denied: You are not the driver".
