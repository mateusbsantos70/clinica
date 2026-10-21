-- =====================================================
-- V1 - Estrutura inicial do banco Essência Viva
-- Banco: MySQL 8+
-- IDs são gerados pela aplicação Java como UUID
-- =====================================================

-- =====================================================
-- TABELA DE USUÁRIOS
-- =====================================================

CREATE TABLE usuario (
                         id CHAR(36) NOT NULL,
                         nome VARCHAR(120) NOT NULL,
                         email VARCHAR(180) NOT NULL,
                         senha_hash VARCHAR(255) NOT NULL,

                         perfil VARCHAR(30) NOT NULL,
                         falhas_login INT NOT NULL DEFAULT 0,
                         bloqueado_ate DATETIME(6) NULL,
                         ativo BOOLEAN NOT NULL DEFAULT TRUE,

                         criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                         atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                         CONSTRAINT pk_usuario PRIMARY KEY (id),
                         CONSTRAINT uk_usuario_email UNIQUE (email),
                         CONSTRAINT ck_usuario_perfil
                             CHECK (perfil IN ('ADMINISTRADOR', 'RECEPCIONISTA')),
                         CONSTRAINT ck_usuario_falhas_login
                             CHECK (falhas_login >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =====================================================
-- TABELA DE SESSÕES
-- O token original nunca é salvo no banco.
-- Apenas o SHA-256 hexadecimal é armazenado.
-- =====================================================

CREATE TABLE sessao (
                        id CHAR(36) NOT NULL,
                        usuario_id CHAR(36) NOT NULL,

                        token_hash CHAR(64) NOT NULL,

                        criada_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                        ultima_atividade_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                        expira_em DATETIME(6) NOT NULL,
                        invalidada_em DATETIME(6) NULL,

                        CONSTRAINT pk_sessao PRIMARY KEY (id),
                        CONSTRAINT uk_sessao_token_hash UNIQUE (token_hash),

                        CONSTRAINT fk_sessao_usuario
                            FOREIGN KEY (usuario_id)
                                REFERENCES usuario (id)
                                ON DELETE CASCADE
                                ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_sessao_usuario
    ON sessao (usuario_id);

CREATE INDEX idx_sessao_validade
    ON sessao (expira_em, invalidada_em);


-- =====================================================
-- TABELA DE CLIENTES
--
-- Os campos *_ciphertext armazenam dados cifrados
-- com AES-256-GCM.
--
-- cpf_hash armazena o HMAC-SHA256 do CPF normalizado.
-- O CPF original não é armazenado em texto puro.
-- =====================================================

CREATE TABLE cliente (
                         id CHAR(36) NOT NULL,

                         cpf_ciphertext TEXT NOT NULL,
                         cpf_hash CHAR(64) NOT NULL,

                         nascimento_ciphertext TEXT NOT NULL,
                         telefone_ciphertext TEXT NOT NULL,
                         email_ciphertext TEXT NOT NULL,
                         endereco_ciphertext TEXT NULL,
                         observacoes_ciphertext TEXT NULL,

                         ativo BOOLEAN NOT NULL DEFAULT TRUE,

                         criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                         atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                         CONSTRAINT pk_cliente PRIMARY KEY (id),
                         CONSTRAINT uk_cliente_cpf_hash UNIQUE (cpf_hash)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_cliente_ativo
    ON cliente (ativo);


-- =====================================================
-- TABELA DE PROFISSIONAIS
-- =====================================================

CREATE TABLE profissional (
                              id CHAR(36) NOT NULL,

                              nome VARCHAR(150) NOT NULL,
                              registro VARCHAR(80) NOT NULL,
                              especialidade VARCHAR(120) NOT NULL,

                              ativo BOOLEAN NOT NULL DEFAULT TRUE,

                              criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                              atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                              CONSTRAINT pk_profissional PRIMARY KEY (id),
                              CONSTRAINT uk_profissional_registro UNIQUE (registro)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_profissional_ativo
    ON profissional (ativo);


-- =====================================================
-- TABELA DE BLOQUEIO DE AGENDA
--
-- Usada com SELECT ... FOR UPDATE para serializar
-- reservas simultâneas do mesmo profissional no MySQL.
-- =====================================================

CREATE TABLE profissional_agenda_lock (
                                          profissional_id CHAR(36) NOT NULL,
                                          criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                                          CONSTRAINT pk_profissional_agenda_lock
                                              PRIMARY KEY (profissional_id),

                                          CONSTRAINT fk_agenda_lock_profissional
                                              FOREIGN KEY (profissional_id)
                                                  REFERENCES profissional (id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =====================================================
-- TABELA DE PROCEDIMENTOS
-- =====================================================

CREATE TABLE procedimento (
                              id CHAR(36) NOT NULL,

                              nome VARCHAR(150) NOT NULL,
                              descricao VARCHAR(500) NULL,
                              duracao_minutos INT NOT NULL,
                              valor_atual DECIMAL(12, 2) NOT NULL,

                              ativo BOOLEAN NOT NULL DEFAULT TRUE,

                              criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                              atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                              CONSTRAINT pk_procedimento PRIMARY KEY (id),

                              CONSTRAINT ck_procedimento_duracao
                                  CHECK (duracao_minutos > 0),

                              CONSTRAINT ck_procedimento_valor
                                  CHECK (valor_atual >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_procedimento_ativo
    ON procedimento (ativo);


-- =====================================================
-- TABELA DE AGENDAMENTOS
-- =====================================================

CREATE TABLE agendamento (
                             id CHAR(36) NOT NULL,

                             cliente_id CHAR(36) NOT NULL,
                             profissional_id CHAR(36) NOT NULL,

                             inicio_em DATETIME(6) NOT NULL,
                             fim_em DATETIME(6) NOT NULL,

                             situacao VARCHAR(30) NOT NULL,
                             total DECIMAL(12, 2) NOT NULL,

                             observacoes_ciphertext TEXT NULL,

                             criado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                             atualizado_em DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                             CONSTRAINT pk_agendamento PRIMARY KEY (id),

                             CONSTRAINT fk_agendamento_cliente
                                 FOREIGN KEY (cliente_id)
                                     REFERENCES cliente (id)
                                     ON DELETE RESTRICT
                                     ON UPDATE CASCADE,

                             CONSTRAINT fk_agendamento_profissional
                                 FOREIGN KEY (profissional_id)
                                     REFERENCES profissional (id)
                                     ON DELETE RESTRICT
                                     ON UPDATE CASCADE,

                             CONSTRAINT ck_agendamento_periodo
                                 CHECK (fim_em > inicio_em),

                             CONSTRAINT ck_agendamento_situacao
                                 CHECK (
                                     situacao IN (
                                                  'MARCADO',
                                                  'CONFIRMADO',
                                                  'CONCLUIDO',
                                                  'CANCELADO'
                                         )
                                     ),

                             CONSTRAINT ck_agendamento_total
                                 CHECK (total >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_agendamento_cliente
    ON agendamento (cliente_id);

CREATE INDEX idx_agendamento_profissional
    ON agendamento (profissional_id);

CREATE INDEX idx_agendamento_profissional_horario
    ON agendamento (profissional_id, inicio_em, fim_em);

CREATE INDEX idx_agendamento_situacao
    ON agendamento (situacao);


-- =====================================================
-- TABELA DE ITENS DO AGENDAMENTO
--
-- valor_unitario_snapshot preserva o preço praticado
-- no momento da criação do agendamento.
-- =====================================================

CREATE TABLE agendamento_procedimento (
                                          agendamento_id CHAR(36) NOT NULL,
                                          procedimento_id CHAR(36) NOT NULL,

                                          quantidade INT NOT NULL DEFAULT 1,
                                          valor_unitario_snapshot DECIMAL(12, 2) NOT NULL,

                                          CONSTRAINT pk_agendamento_procedimento
                                              PRIMARY KEY (agendamento_id, procedimento_id),

                                          CONSTRAINT fk_item_agendamento
                                              FOREIGN KEY (agendamento_id)
                                                  REFERENCES agendamento (id)
                                                  ON DELETE CASCADE
                                                  ON UPDATE CASCADE,

                                          CONSTRAINT fk_item_procedimento
                                              FOREIGN KEY (procedimento_id)
                                                  REFERENCES procedimento (id)
                                                  ON DELETE RESTRICT
                                                  ON UPDATE CASCADE,

                                          CONSTRAINT ck_item_quantidade
                                              CHECK (quantidade > 0),

                                          CONSTRAINT ck_item_valor_snapshot
                                              CHECK (valor_unitario_snapshot >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_item_procedimento
    ON agendamento_procedimento (procedimento_id);
