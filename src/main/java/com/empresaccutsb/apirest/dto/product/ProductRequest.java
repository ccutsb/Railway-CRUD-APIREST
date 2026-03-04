package com.empresaccutsb.apirest.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(min = 2, max = 120) String nombre,
        @Size(max = 500) String descripcion,
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") BigDecimal precio) {}
