package com.dcInmobiliaria.proyecto.Inmobiliaria.DC.model;

public enum EstadoPago {
    PENDIENTE,           // Recién creado - transferencia sin verificar
    CONFIRMADO,          // Verificado por corredor
    RECHAZADO,           // Rechazado por corredor
    EFECTIVO_CONFIRMADO  // Efectivo confirmado automáticamente
}