package com.ccalarce.siglof.model.enums;

public enum LiquidationStatus {
    PENDING, // Chofer cerró ruta, esperando revisión
    APPROVED, // Admin aprobó los números
    REJECTED, // Admin rechazó (discrepancia grave)
    OBSERVED // Admin tiene observaciones, chofer debe revisar
}
