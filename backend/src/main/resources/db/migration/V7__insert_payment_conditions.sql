INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('006eb155-096c-4303-a160-e047565e548b', 'TEN001', 'AVISTA', 'Condição padrão Farma',
        50.00, 100,
        false, false,
        0.00,
        5.00, 0.00, 'Pedidos acima de R$500',
        1000.00, false, true);

INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('04054235-0c05-4abc-b026-bf3ec0b9eeb9', 'TEN001', '30-60-90', 'Condição premium Farma',
        0.00, 500,
        true, false,
        2.00,
        8.00, 3.00, 'Pagamento à vista',
        0.00, true, true);

INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('1d90415f-a0c6-4ede-a83c-468bed278d15', 'TEN001', '0-30', 'Condição restritiva Farma',
        200.00, 50,
        false, true,
        0.00,
        0.00, 2.00, 'Pagamento à vista',
        0.00, false, true);
-- Tenant FARMA-DEFAULT (TEN002)
INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('1dac9d63-c20d-431d-b8cb-17f0b5b2783b', 'TEN002', 'AVISTA', 'Condição padrão Farma',
        50.00, 100,
        false, false,
        0.00,
        5.00, 0.00, 'Pedidos acima de R$500',
        1000.00, false, true);

INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('2f168cde-165c-441b-9909-670e1d976380', 'TEN002', '30-60-90', 'Condição premium Farma',
        0.00, 500,
        true, false,
        2.00,
        8.00, 3.00, 'Pagamento à vista',
        0.00, true, true);

INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('8b734f25-9bb8-4fbc-930b-8a7e2156de08', 'TEN002', '0-30', 'Condição restritiva Farma',
        200.00, 50,
        false, true,
        0.00,
        0.00, 2.00, 'Pagamento à vista',
        0.00, false, true);

INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('9a547e66-5dd9-486a-8160-1648fb73694a', 'TEN003', 'AVISTA', 'Condição padrão Farma',
        50.00, 100,
        false, false,
        0.00,
        5.00, 0.00, 'Pedidos acima de R$500',
        1000.00, false, true);

INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('9afa5731-5a08-4a94-8343-b1b8e253c600', 'TEN003', '30-60-90', 'Condição premium Farma',
        0.00, 500,
        true, false,
        2.00,
        8.00, 3.00, 'Pagamento à vista',
        0.00, true, true);

INSERT INTO payment_condition (id, tenant_code, code, description,
                               min_order_value, max_items,
                               allow_bonus_order, business_hours_only,
                               operational_fee_percent,
                               discount_percent, discount_extra_percent, discount_condition,
                               free_shipping_threshold, always_free_shipping, enabled)
VALUES ('bb751b28-0f4d-46dd-86b6-61184fd70fa7', 'TEN003', '0-30', 'Condição restritiva Farma',
        200.00, 50,
        false, true,
        0.00,
        0.00, 2.00, 'Pagamento à vista',
        0.00, false, true);