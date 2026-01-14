# Referencia API Maestra - Siglo F (Backend v2)

Este documento es la fuente de la verdad para el desarrollo Frontend. Contiene todos los endpoints, estructuras JSON exactas, roles permitidos y guías de visualización UI.

---

## Índice

1.  [Tablas de Enums (Diccionario UI)](#1-tablas-de-enums-diccionario-ui)
2.  [Módulo de Autenticación](#2-módulo-de-autenticación)
3.  [Módulo Core (Usuarios y Clientes)](#3-módulo-core-usuarios-y-clientes)
4.  [Módulo Logística (Vehículos y Rutas)](#4-módulo-logística-vehículos-y-rutas)
5.  [Módulo Operación (Ventas y Liquidaciones)](#5-módulo-operación-ventas-y-liquidaciones)
6.  [Módulo Auditoría](#6-módulo-auditoría)

---

## 1. Tablas de Enums (Diccionario UI)

Utiliza estos valores exactos y colores sugeridos para badges/etiquetas.

### **ClientType**
| Valor | Descripción UI | Icono Sugerido | Color |
| :--- | :--- | :--- | :--- |
| `RESTAURANTE` | Cliente comercial de alto volumen | 🏢 Edificio | Indigo |
| `CONVENCIONAL` | Cliente doméstico / Casa | 🏠 Casa | Verde |

### **CommercialStatus** (Semáforo de Clientes)
| Valor | Descripción UI | Color | Comportamiento Frontend |
| :--- | :--- | :--- | :--- |
| `ACTIVO` | Cliente normal | ✅ Verde | Mostrar normal |
| `FRECUENTE` | Compra seguido (VIP) | ⭐ Dorado | Resaltar en lista |
| `ALERTA` | Deuda o problema | 🚨 Rojo | **Mostrar Alerta Modal al intentar vender** |

### **Role**
| Valor | Descripción |
| :--- | :--- |
| `SUPER_ADMIN` | Dueño del sistema (Dios) |
| `ADMIN` | Gerente de planta (Operación) |
| `REPARTIDOR` | Chofer (App Móvil/Web Móvil) |

### **RouteStatus**
| Valor | Descripción UI | Color |
| :--- | :--- | :--- |
| `OPEN` | Ruta en curso | 🔵 Azul |
| `CLOSED` | Ruta finalizada | ⚫ Gris |

### **LiquidationStatus**
| Valor | Descripción UI | Color |
| :--- | :--- | :--- |
| `PENDING` | Chofer cerró, falta aprobar | ⏳ Naranja (Action Required) |
| `APPROVED` | Dinero verificado por Admin | ✅ Verde |

---

## 2. Módulo de Autenticación (`/api/v1/auth`)

### **Login**
*   **POST** `/authenticate`
*   **Acceso:** Público

**Payload:**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**Respuesta (200 OK):**
Guarda el token en `localStorage`.
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### **Registro (Crear Usuario)**
*   **POST** `/register`
*   **Acceso:** Público (Actualmente). **NOTA:** En producción esto debería ser protegido o solo interno.

**Payload:**
```json
{
  "fullName": "Juan Perez",
  "username": "jperez",
  "password": "securePass",
  "role": "REPARTIDOR" // Valores: SUPER_ADMIN, ADMIN, REPARTIDOR
}
```

---

## 3. Módulo Core (Usuarios y Clientes)

### A. Usuarios (`/api/v1/users`)

#### **Obtener Mi Perfil**
*   **GET** `/me`
*   **Acceso:** Todos
*   **Uso:** Para mostrar "Hola, [Nombre]" en el header.

#### **Listar Empleados**
*   **GET** `/`
*   **Acceso:** `SUPER_ADMIN`, `ADMIN`
*   **Uso:** Dropdown para asignar chofer a ruta.

---

### B. Clientes (`/api/v1/clients`)

#### **Listar Clientes**
*   **GET** `/`
*   **Acceso:** `SUPER_ADMIN`, `ADMIN`
*   **Uso:** Tabla principal de clientes.

#### **Crear Cliente (Backoffice)**
*   **POST** `/`
*   **Acceso:** `SUPER_ADMIN`, `ADMIN`

**Payload Completo:**
```json
{
  "fullName": "Pollería El Rey",
  "documentNumber": "20123456789",
  "address": "Av. Principal 123",
  "phoneNumber": "999888777",
  "type": "RESTAURANTE",              // Ver Enum
  "commercialStatus": "ACTIVO",       // Ver Enum
  "paymentFrequency": "SEMANAL",      // SEMANAL, MENSUAL
  "latitude": -12.043,
  "longitude": -77.028,
  // Opcional: Asignar QR al crear
  "tokenQr": {
      "code": "UUID-GENERADO-FRONTEND",
      "status": "ASIGNADO"
  }
}
```

---

## 4. Módulo Logística (Vehículos y Rutas)

### A. Vehículos (`/api/v1/vehicles`)
CRUD estándar. Campos clave: `plate` (Placa), `gpsDeviceId` (ID GPS Externo).

### B. Rutas (`/api/v1/routes`)

#### **Abrir Ruta (Iniciar Día)**
*   **POST** `/open`
*   **Acceso:** `ADMIN`, `SUPER_ADMIN`
*   **Lógica:** Asigna un vehículo cargado a un chofer. Descuenta Stock de Planta automáticamente.

**Payload:**
```json
{
  "driverId": 5,      // ID del Usuario Repartidor
  "vehicleId": 2,     // ID del Vehículo
  "stock": [
    { "productId": 1, "quantity": 50 } // Carga 50 balones
  ]
}
```

#### **Obtener Rutas Activas (Monitor)**
*   **GET** `/active`
*   **Acceso:** `ADMIN`, `SUPER_ADMIN`
*   **Uso:** Dashboard en tiempo real. Muestra quién está en la calle.

#### **Obtener MI Ruta (App Chofer)**
*   **GET** `/current`
*   **Acceso:** `REPARTIDOR`
*   **Lógica:** "Visión de Túnel". El chofer no envía ID. El backend responde con su ruta abierta.
*   **Respuesta:** Objeto Ruta con todo su stock actual.
*   **Error 404:** Si el chofer no tiene ruta asignada hoy.

---

## 5. Módulo Operación (Ventas y Liquidaciones)

### A. Ventas (`/api/v1/sales`)

#### **Registrar Venta (App Chofer)**
*   **POST** `/`
*   **Acceso:** `REPARTIDOR` (En su propia ruta), `ADMIN`
*   **Validación:** Verifica que haya stock suficiente en la RUTA (RouteStock).

**Payload:**
```json
{
  "routeId": 105,              // ID de la ruta actual (obtenido de /routes/current)
  "clientId": 45,
  "paymentMethod": "EFECTIVO", // EFECTIVO, YAPE, PLIN, TARJETA, CREDITO
  "latitude": -12.00,          // Coordenadas donde se hizo la venta
  "longitude": -77.00,
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

---

### B. Liquidaciones (`/api/v1/liquidation`)

#### **1. Cerrar Ruta y Reportar Stock (Chofer)**
*   **POST** `/close`
*   **Acceso:** `REPARTIDOR`
*   **Momento:** Al final del día, al volver a planta.

**Payload:**
```json
{
  "routeId": 105,
  "savedStock": [
     // Stock FÍSICO que trae de vuelta (lo que no vendió)
     { "productId": 1, "quantity": 10 }
  ]
}
```
**Efecto:**
1.  Calcula ventas totales.
2.  Registra retorno de stock a planta.
3.  Crea una `Liquidation` con status `PENDING`.
4.  Cierra la ruta (`CLOSED`).

#### **2. Aprobar Liquidación (Admin)**
*   **POST** `/approve/{id}`
*   **Acceso:** `ADMIN`, `SUPER_ADMIN`
*   **Momento:** Después de contar el dinero físico del chofer.
*   **Efecto:** Cambia status a `APPROVED` y guarda quién aprobó.

---

## 6. Módulo Auditoría (`/api/v1/audit`)

#### **Ver Log de Acciones**
*   **GET** `/`
*   **Acceso:** `SUPER_ADMIN` (Exclusivo)
*   **Uso:** Tabla de seguridad ("Ojo de Dios"). Muestra quién hizo qué y cuándo.
