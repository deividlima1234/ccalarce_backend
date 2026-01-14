# Guía de Implementación Frontend por Roles

Este documento traduce los scripts de verificación exitosos (`verify_deployment.sh`, `verify_driver_flow.sh`, `verify_admin_lifecycle.sh`) en guías de implementación para el Frontend (React/Mobile).

---

## 1. 📱 Rol: Repartidor (Driver)
**Objetivo:** Interfaz móvil simplificada. "Visión de Túnel".
**Script de Referencia:** `verify_driver_flow.sh`

### Flujo de Datos
1.  **Login:** `POST /auth/authenticate` → Guarda Token.
2.  **Dashboard (Home):**
    *   **NO** llames a listar rutas.
    *   **DEBES** llamar a: `GET /routes/current`
    *   *Lógica:* El backend identifica al chofer por el Token. No necesitas enviar ID.
3.  **Hacer Venta:**
    *   Usa el `id` de la ruta obtenido en el paso anterior.
    *   Usa Geolocalización del navegador/celular (`navigator.geolocation`).
    *   Endpoint: `POST /sales`
    *   Payload Vital: `{ "latitude": -12.x, "longitude": -77.x, ... }`
4.  **Stock en Tiempo Real:**
    *   Después de cada venta, vuelve a llamar a `GET /routes/current`.
    *   El array `stock` vendrá actualizado (restado) automáticamente.
    *   *UX:* Muestra "Cargas Disponibles: X" en grande.
5.  **Cerrar Ruta:**
    *   Endpoint: `POST /liquidation/close`
    *   Calcula lo que queda en el camión y envíalo como `savedStock`.

> **💡 Tip Frontend:** Si `GET /routes/current` retorna 404, muestra pantalla "No tienes ruta asignada. Contacta al Admin".

---

## 2. 👔 Rol: Administrador (Admin)
**Objetivo:** Dashboard de Gestión y Monitoreo.
**Script de Referencia:** `verify_admin_lifecycle.sh`

### Flujo de Datos
1.  **Gestión de Personal:**
    *   Endpoint: `GET /users`
    *   *Uso:* Llena los dropdowns de "Choferes Disponibles" al crear una ruta.
2.  **Logística (Abrir Ruta):**
    *   Endpoint: `POST /routes/open`
    *   *Validación:* Asegúrate de enviar `vehicleId` y `driverId` (obtenidos de sus respectivos endpoints).
    *   *Inventario:* El backend descontará del inventario principal automáticamente. Maneja errores 400 "Insufficient stock".
3.  **Monitoreo en Vivo:**
    *   Endpoint: `GET /routes/active`
    *   *Uso:* Tabla en tiempo real de quién está en la calle.
4.  **Aprobación de Liquidación:**
    *   Cuando un chofer cierra ruta, queda en `PENDING`.
    *   Admin va a "Liquidaciones Pendientes" y da click en "Aprobar".
    *   Endpoint: `POST /liquidation/approve/{id}`

> **🛡️ Seguridad:** El Admin NO debe tener acceso a ver Auditoría. Oculta el botón o link a `/audit` en el sidebar si `role === 'ADMIN'`.

---

## 3. 👑 Rol: Super Admin
**Objetivo:** Auditoría y Control Total.
**Script de Referencia:** `verify_deployment.sh`

### Flujo Exclusivo
1.  **Auditoría del Sistema:**
    *   Endpoint: `GET /audit`
    *   *Uso:* Tabla de logs. Muestra `action`, `username`, `details`, `timestamp`.
2.  **Gestión Global:**
    *   Tiene acceso a TODO lo del Admin + Crear/Eliminar Usuarios.

---

## Resumen de Endpoints por Rol

| Acción | Repartidor | Admin | Super Admin |
| :--- | :---: | :---: | :---: |
| **Login** | ✅ | ✅ | ✅ |
| **Ver Mi Ruta** | `GET /routes/current` | - | - |
| **Vender** | ✅ (Con GPS) | - | - |
| **Cerrar Ruta** | ✅ | - | - |
| **Ver Todas Rutas** | ❌ (403) | ✅ | ✅ |
| **Abrir Ruta** | ❌ (403) | ✅ | ✅ |
| **Crear Clientes** | ❌ (403) | ✅ | ✅ |
| **Ver Logs** | ❌ (403) | ❌ (403) | ✅ |

### Implementación de Errores (Error Handling)
*   **403 Forbidden:** Redirigir a Login o mostrar "No autorizado".
*   **500 Internal Error:** Mostrar "Error del Servidor". (Reportar a Backend).
*   **400 Bad Request:** Mostrar mensaje del error (ej: "Stock insuficiente").

---
**Nota para Devs:** Utilicen los scripts `.sh` en la carpeta `backend/` como referencia viva de qué payloads funcionan exactamente.
