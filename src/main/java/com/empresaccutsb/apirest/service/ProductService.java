package com.empresaccutsb.apirest.service;

import com.empresaccutsb.apirest.dto.common.PagedResponse;
import com.empresaccutsb.apirest.dto.product.ProductRequest;
import com.empresaccutsb.apirest.dto.product.ProductResponse;
import com.empresaccutsb.apirest.exception.NotFoundException;
import com.empresaccutsb.apirest.mapper.ProductMapper;
import com.empresaccutsb.apirest.model.Product;
import com.empresaccutsb.apirest.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    public ProductService(ProductRepository productRepository, AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> findAll(String search, boolean includeDeleted, Pageable pageable) {
        Specification<Product> specification =
                (root, query, cb) -> {
                    var predicates = new ArrayList<Predicate>();
                    if (!includeDeleted) {
                        predicates.add(cb.isNull(root.get("deletedAt")));
                    }
                    if (search != null && !search.isBlank()) {
                        String like = "%" + search.toLowerCase() + "%";
                        predicates.add(
                                cb.or(
                                        cb.like(cb.lower(root.get("nombre")), like),
                                        cb.like(cb.lower(root.get("descripcion")), like)));
                    }
                    return cb.and(predicates.toArray(Predicate[]::new));
                };

        Page<Product> page = productRepository.findAll(specification, pageable);
        return new PagedResponse<>(
                page.map(ProductMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product =
                productRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new NotFoundException("No se encontro el producto con id " + id));
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request, String actor) {
        Product product = ProductMapper.toEntity(request);
        Product saved = productRepository.save(product);
        auditLogService.log("CREATE", "PRODUCT", saved.getId().toString(), actor, true, "create");
        return ProductMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request, String actor) {
        Product product =
                productRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new NotFoundException("No se encontro el producto con id " + id));
        ProductMapper.apply(product, request);
        Product saved = productRepository.save(product);
        auditLogService.log("UPDATE", "PRODUCT", saved.getId().toString(), actor, true, "update");
        return ProductMapper.toResponse(saved);
    }

    @Transactional
    public void softDelete(Long id, String actor) {
        Product product =
                productRepository
                        .findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new NotFoundException("No se encontro el producto con id " + id));
        product.setDeletedAt(Instant.now());
        product.setDeletedBy(actor);
        productRepository.save(product);
        auditLogService.log("DELETE", "PRODUCT", id.toString(), actor, true, "soft delete");
    }

    @Transactional
    public ProductResponse restore(Long id, String actor) {
        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() -> new NotFoundException("No se encontro el producto con id " + id));
        product.setDeletedAt(null);
        product.setDeletedBy(null);
        Product saved = productRepository.save(product);
        auditLogService.log("RESTORE", "PRODUCT", id.toString(), actor, true, "restore");
        return ProductMapper.toResponse(saved);
    }
}
