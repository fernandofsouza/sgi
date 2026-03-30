-- ============================================================
-- SGI - Sistema de Gestão de Indicadores
-- Migration V1: Criação das tabelas principais
-- PostgreSQL (Heroku Postgres)
-- ============================================================

-- Membros da equipe
CREATE TABLE team_members (
    id            VARCHAR(50)   NOT NULL PRIMARY KEY,
    name          VARCHAR(200)  NOT NULL,
    role          VARCHAR(100)  NOT NULL,
    email         VARCHAR(200)  NULL,
    avatar_url    VARCHAR(500)  NULL,
    active        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Critérios de relevância
CREATE TABLE relevance_criteria (
    id          VARCHAR(50)   NOT NULL PRIMARY KEY,
    name        VARCHAR(200)  NOT NULL,
    description TEXT          NULL,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Labels da escala de relevância (1-5 por critério)
CREATE TABLE relevance_scale_labels (
    id           VARCHAR(50)  NOT NULL PRIMARY KEY,
    criterion_id VARCHAR(50)  NOT NULL REFERENCES relevance_criteria(id) ON DELETE CASCADE,
    scale_value  INT          NOT NULL CHECK (scale_value BETWEEN 1 AND 5),
    label        VARCHAR(200) NOT NULL
);

-- Indicadores
CREATE TABLE indicators (
    id                VARCHAR(50)   NOT NULL PRIMARY KEY,
    seq_id            SERIAL,
    title             VARCHAR(500)  NOT NULL,
    description       TEXT          NULL,
    pdg_id            VARCHAR(100)  NULL,
    creation_status   VARCHAR(100)  NOT NULL DEFAULT 'Não iniciado',
    progress_status   VARCHAR(100)  NOT NULL DEFAULT 'Não iniciado',
    progress          INT           NOT NULL DEFAULT 0 CHECK (progress BETWEEN 0 AND 100),
    target_date       DATE          NULL,
    parent_id         VARCHAR(50)   NULL REFERENCES indicators(id),
    editor_id         VARCHAR(50)   NULL REFERENCES team_members(id),
    validator_id      VARCHAR(50)   NULL REFERENCES team_members(id),
    observation       TEXT          NULL,
    reference_year    INT           NOT NULL,
    reference_range   VARCHAR(20)   NOT NULL CHECK (reference_range IN ('mensal','bimestral','trimestral','semestral','anual')),
    reference_label   VARCHAR(10)   NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Escala de conquista por indicador (1-5)
CREATE TABLE achievement_scale_labels (
    id             VARCHAR(50)  NOT NULL PRIMARY KEY,
    indicator_id   VARCHAR(50)  NOT NULL REFERENCES indicators(id) ON DELETE CASCADE,
    scale_value    INT          NOT NULL CHECK (scale_value BETWEEN 1 AND 5),
    label          VARCHAR(200) NOT NULL
);

-- Critérios de avaliação do indicador
CREATE TABLE evaluation_criteria (
    id             VARCHAR(50)    NOT NULL PRIMARY KEY,
    indicator_id   VARCHAR(50)    NOT NULL REFERENCES indicators(id) ON DELETE CASCADE,
    name           VARCHAR(200)   NOT NULL,
    weight         DECIMAL(5,2)   NOT NULL CHECK (weight BETWEEN 0 AND 100),
    target_value   DECIMAL(18,4)  NOT NULL,
    current_value  DECIMAL(18,4)  NOT NULL DEFAULT 0,
    unit           VARCHAR(50)    NULL
);

-- Responsáveis pelo indicador (N:N)
CREATE TABLE indicator_assignees (
    indicator_id  VARCHAR(50) NOT NULL REFERENCES indicators(id) ON DELETE CASCADE,
    member_id     VARCHAR(50) NOT NULL REFERENCES team_members(id),
    PRIMARY KEY (indicator_id, member_id)
);

-- Check-ins
CREATE TABLE check_ins (
    id            VARCHAR(50)  NOT NULL PRIMARY KEY,
    indicator_id  VARCHAR(50)  NOT NULL REFERENCES indicators(id),
    check_date    DATE         NOT NULL,
    progress      INT          NOT NULL CHECK (progress BETWEEN 0 AND 100),
    notes         TEXT         NULL,
    author_id     VARCHAR(50)  NOT NULL REFERENCES team_members(id),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Atualizações de critério dentro do check-in
CREATE TABLE check_in_criteria_updates (
    id            VARCHAR(50)    NOT NULL PRIMARY KEY,
    check_in_id   VARCHAR(50)    NOT NULL REFERENCES check_ins(id) ON DELETE CASCADE,
    criteria_id   VARCHAR(50)    NOT NULL,
    value         DECIMAL(18,4)  NOT NULL
);

-- Avaliações de relevância por indicador
CREATE TABLE relevance_assessments (
    indicator_id  VARCHAR(50) NOT NULL REFERENCES indicators(id) ON DELETE CASCADE,
    criterion_id  VARCHAR(50) NOT NULL REFERENCES relevance_criteria(id),
    score         INT         NOT NULL CHECK (score BETWEEN 1 AND 5),
    PRIMARY KEY (indicator_id, criterion_id)
);

-- Pesos de membros por indicador
CREATE TABLE member_weights (
    member_id     VARCHAR(50)   NOT NULL REFERENCES team_members(id),
    indicator_id  VARCHAR(50)   NOT NULL REFERENCES indicators(id) ON DELETE CASCADE,
    weight        DECIMAL(5,2)  NOT NULL CHECK (weight BETWEEN 0 AND 100),
    PRIMARY KEY (member_id, indicator_id)
);

-- Log de auditoria
CREATE TABLE audit_logs (
    id           BIGSERIAL      PRIMARY KEY,
    action       VARCHAR(100)   NOT NULL,
    entity_type  VARCHAR(50)    NOT NULL,
    entity_id    VARCHAR(50)    NOT NULL,
    details      TEXT           NULL,
    user_id      VARCHAR(100)   NULL,
    user_name    VARCHAR(200)   NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- Configurações de sistema (statuses configuráveis)
CREATE TABLE system_configs (
    config_key    VARCHAR(100)  NOT NULL PRIMARY KEY,
    config_value  TEXT          NOT NULL,
    updated_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ============================================================
-- Dados iniciais
-- ============================================================

INSERT INTO system_configs (config_key, config_value) VALUES
('creation_statuses', '["Não iniciado","Em edição","Solicitada aprovação","Solicitada revisão","Aprovado"]'),
('progress_statuses',  '["Não iniciado","Em andamento normal","Em andamento em atraso","Concluído"]');

INSERT INTO relevance_criteria (id, name, description) VALUES
('rc1', 'Impacto Estratégico', 'Nível de impacto deste indicador nos objetivos estratégicos da organização');

INSERT INTO relevance_scale_labels (id, criterion_id, scale_value, label) VALUES
('rsl1','rc1',1,'Baixo'),
('rsl2','rc1',2,'Médio'),
('rsl3','rc1',3,'Alto'),
('rsl4','rc1',4,'Muito Alto'),
('rsl5','rc1',5,'Máximo');

INSERT INTO team_members (id, name, role, email) VALUES
('1','Ana Silva',      'Product Manager', 'ana@sgi.gov.br'),
('2','Carlos Mendes',  'Tech Lead',        'carlos@sgi.gov.br'),
('3','Beatriz Lima',   'Designer',         'beatriz@sgi.gov.br'),
('4','Diego Rocha',    'Developer',        'diego@sgi.gov.br'),
('5','Fernanda Costa', 'QA Lead',          'fernanda@sgi.gov.br');

-- Índices de performance
CREATE INDEX ix_indicators_parent_id       ON indicators(parent_id);
CREATE INDEX ix_indicators_reference_year  ON indicators(reference_year, reference_range, reference_label);
CREATE INDEX ix_check_ins_indicator_id     ON check_ins(indicator_id);
CREATE INDEX ix_audit_logs_entity          ON audit_logs(entity_type, entity_id);
