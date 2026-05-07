-- 1. TENANT: FARMA-DEFAULT
-- Regras: Min 50.00 | Max 100 itens | Desconto 5% (Base) | Frete Grátis > 1000
INSERT INTO payment_condition (id, code, description, tenant_code,
                               min_value, max_items,
                               discount_percentage, extra_discount_percentage,
                               free_shipping_threshold, operational_fee_percentage)
VALUES ('fd865b0c-a09c-4a60-a74f-ffbc0c2b19aa', 'AVISTA', 'Á vista', 'TEN001',
        50.00, 100,
        5.00, 0.00,
        1000.00, 0.00);

-- 2. TENANT: FARMA-PREMIUM
-- Regras: Max 500 itens | Bonus liberado | Taxa 2% | Frete sempre grátis (0.00)
-- Desconto: 8% (Base) para todos + 3% (Extra) para à vista
INSERT INTO payment_condition (id, code, description, tenant_code,
                               min_value, max_items, allow_bonus,
                               discount_percentage, extra_discount_percentage,
                               free_shipping_threshold, operational_fee_percentage, max_installments)
VALUES ('56cbd1e3-984e-47d8-8301-0e027ec388b6', 'AVISTA', 'À Vista', 'TEN002',
        0.00, 500, TRUE,
        8.00, 3.00,
        0.00, 2.00, 1),
       ('6d3f0b64-06cc-4e6f-aac2-08f948672c82', '30-60-90', 'Parcelado 3x', 'TEN002',
        0.00, 500, TRUE,
        8.00, 0.00,
        0.00, 2.00, 12);

-- 3. TENANT: FARMA-ECONOMIA
-- Regras: Min 200.00 | Max 50 itens | Horário Comercial (8h-18h)
-- Desconto: 0% (Base) | 2% (Extra) apenas à vista
INSERT INTO payment_condition (id, code, description, tenant_code,
                               min_value, max_items, allow_only_business_hours,
                               discount_percentage, extra_discount_percentage,
                               free_shipping_threshold, operational_fee_percentage, max_installments)
VALUES ('56cbd1e3-984e-47d8-8301-0e027ec38222', 'AVISTA', 'À Vista', 'TEN003',
        200.00, 50, TRUE,
        0.00, 2.00,
        999999.99, 0.00, 1),
       ('56cbd1e3-984e-47d8-8301-0e027ec38111', '30-60-90', 'Parcelado 3x', 'TEN003',
        200.00, 50, TRUE,
        0.00, 0.00,
        999999.99, 0.00, 6);