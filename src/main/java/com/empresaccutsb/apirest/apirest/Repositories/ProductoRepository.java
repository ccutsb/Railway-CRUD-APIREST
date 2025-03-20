package com.empresaccutsb.apirest.apirest.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresaccutsb.apirest.apirest.Entities.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
}
