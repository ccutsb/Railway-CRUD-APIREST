DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'producto') THEN
        INSERT INTO products (id, nombre, descripcion, precio, created_at, updated_at, created_by, updated_by)
        SELECT p.id,
               p.nombre,
               NULL,
               p.precio,
               NOW(),
               NOW(),
               'legacy-migration',
               'legacy-migration'
        FROM producto p
        ON CONFLICT (id) DO NOTHING;

        PERFORM setval('products_id_seq', COALESCE((SELECT MAX(id) FROM products), 1), true);
    END IF;
END $$;
