CREATE SCHEMA IF NOT EXISTS public;

-- Tenants do sistema
CREATE TABLE tenant
(
    id         UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    code       VARCHAR(50) UNIQUE NOT NULL,
    name       VARCHAR(255)       NOT NULL,
    enabled    BOOLEAN                  DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Compradores
CREATE TABLE buyer
(
    id                 UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    external_reference VARCHAR(100) UNIQUE NOT NULL,
    name               VARCHAR(255)        NOT NULL,
    credit_limit       DECIMAL(15, 2)      NOT NULL DEFAULT 0,
    tenant_code        VARCHAR(50)         NOT NULL,
    enabled            BOOLEAN                      DEFAULT true,
    created_at         TIMESTAMP WITH TIME ZONE     DEFAULT now(),
    last_modified      TIMESTAMP WITH TIME ZONE     DEFAULT now(),
    version            BIGINT                       DEFAULT 0
);

-- Vendedores
CREATE TABLE seller
(
    id                 UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    external_reference VARCHAR(100) UNIQUE NOT NULL,
    name               VARCHAR(255)        NOT NULL,
    tenant_code        VARCHAR(50)         NOT NULL,
    enabled            BOOLEAN                  DEFAULT true,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Armazéns
CREATE TABLE warehouse
(
    id                 UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    external_reference VARCHAR(100) UNIQUE NOT NULL,
    name               VARCHAR(255)        NOT NULL,
    seller_id          UUID                NOT NULL REFERENCES seller (id),
    tenant_code        VARCHAR(50)         NOT NULL,
    enabled            BOOLEAN                  DEFAULT true,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Tabela de preços por produto/warehouse
CREATE TABLE product_price
(
    id            UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    product_code  VARCHAR(100)   NOT NULL,
    product_name  VARCHAR(255)   NOT NULL,
    warehouse_id  UUID           NOT NULL REFERENCES warehouse (id),
    unit_price    DECIMAL(15, 4) NOT NULL,
    list_price    DECIMAL(15, 4) NOT NULL,
    tenant_code   VARCHAR(50)    NOT NULL,
    enabled       BOOLEAN                  DEFAULT true,
    last_modified TIMESTAMP WITH TIME ZONE DEFAULT now(),
    UNIQUE (product_code, warehouse_id)
);

-- Condições de pagamento
CREATE TABLE payment_condition
(
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_code             VARCHAR(50)  NOT NULL,          -- Identifica o tenant
    code                    VARCHAR(50)  NOT NULL,          -- Código da condição
    description             VARCHAR(255) NOT NULL,          -- Descrição
    min_order_value         DECIMAL(15, 2)   DEFAULT 0,     -- Valor mínimo do pedido
    max_items               INT              DEFAULT NULL,  -- Máximo de itens
    allow_bonus_order       BOOLEAN          DEFAULT false, -- Permite pedido bônus
    business_hours_only     BOOLEAN          DEFAULT false, -- Bloqueia fora do horário comercial
    operational_fee_percent DECIMAL(5, 2)    DEFAULT 0,     -- Taxa operacional (%)
    discount_percent        DECIMAL(5, 2)    DEFAULT 0,     -- Desconto padrão (%)
    discount_extra_percent  DECIMAL(5, 2)    DEFAULT 0,     -- Desconto extra (ex: à vista)
    discount_condition      VARCHAR(255)     DEFAULT NULL,  -- Regra do desconto extra
    free_shipping_threshold DECIMAL(15, 2)   DEFAULT 0,     -- Valor para frete grátis
    always_free_shipping    BOOLEAN          DEFAULT false, -- Frete sempre grátis
    enabled                 BOOLEAN          DEFAULT true,

    CONSTRAINT uq_payment_condition_tenant_code_code UNIQUE (tenant_code, code)
);

-- Pedidos
CREATE TABLE "order"
(
    id                   UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    code                 VARCHAR(50) UNIQUE  NOT NULL, -- Código do pedido gerado pela API
    external_reference   VARCHAR(100) UNIQUE NOT NULL,
    buyer_id             UUID                NOT NULL REFERENCES buyer (id),
    seller_id            UUID                NOT NULL REFERENCES seller (id),
    warehouse_id         UUID                NOT NULL REFERENCES warehouse (id),
    payment_condition_id UUID                NOT NULL REFERENCES payment_condition (id),
    status               VARCHAR(30)         NOT NULL DEFAULT 'PENDING',
    subtotal             DECIMAL(15, 2)      NOT NULL,
    discount_value       DECIMAL(15, 2)               DEFAULT 0,
    total                DECIMAL(15, 2)      NOT NULL,
    origin               VARCHAR(30)         NOT NULL DEFAULT 'API',
    tenant_code          VARCHAR(50)         NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE     DEFAULT now(),
    last_modified        TIMESTAMP WITH TIME ZONE     DEFAULT now(),
    version              BIGINT                       DEFAULT 0
);

-- Itens do pedido
CREATE TABLE order_item
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     UUID           NOT NULL REFERENCES "order" (id),
    product_code VARCHAR(100)   NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(15, 4) NOT NULL,
    list_price   DECIMAL(15, 4) NOT NULL,
    subtotal     DECIMAL(15, 2) NOT NULL
);

CREATE INDEX idx_order_tenant ON "order" (tenant_code);
CREATE INDEX idx_order_buyer ON "order" (buyer_id);
CREATE INDEX idx_order_status ON "order" (status);
CREATE INDEX idx_order_created ON "order" (created_at);
CREATE INDEX idx_buyer_tenant ON buyer (tenant_code);
CREATE INDEX idx_product_price_warehouse ON product_price (warehouse_id);
CREATE INDEX idx_payment_condition_tenant_code ON payment_condition (tenant_code, code);
