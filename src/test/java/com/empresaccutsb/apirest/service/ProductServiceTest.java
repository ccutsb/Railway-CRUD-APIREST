package com.empresaccutsb.apirest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresaccutsb.apirest.dto.product.ProductRequest;
import com.empresaccutsb.apirest.exception.NotFoundException;
import com.empresaccutsb.apirest.model.Product;
import com.empresaccutsb.apirest.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private AuditLogService auditLogService;

    private ProductService productService;

    @BeforeEach
    void setup() {
        productService = new ProductService(productRepository, auditLogService);
    }

    @Test
    void createShouldPersistProduct() {
        ProductRequest request = new ProductRequest("Mouse", "Gaming", BigDecimal.valueOf(50));

        Product saved = new Product();
        ReflectionTestUtils.setField(saved, "id", 1L);
        saved.setNombre("Mouse");
        saved.setDescripcion("Gaming");
        saved.setPrecio(BigDecimal.valueOf(50));

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        var response = productService.create(request, "admin");

        assertThat(response.nombre()).isEqualTo("Mouse");
        verify(productRepository).save(any(Product.class));
        verify(auditLogService).log("CREATE", "PRODUCT", "1", "admin", true, "create");
    }

    @Test
    void findByIdShouldFailWhenDeletedOrMissing() {
        when(productRepository.findByIdAndDeletedAtIsNull(33L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.findById(33L)).isInstanceOf(NotFoundException.class);
    }
}
