-- ============================================================
-- Script de Populacao do Banco de Dados - AutoManager
-- ============================================================
-- Este script popula o banco com dados de teste para validar
-- todas as regras de negocio e relacionamentos

-- ============================================================
-- 1. EMPRESAS
-- ============================================================
INSERT INTO empresa (razao_social, nome_fantasia, cadastro) VALUES
('AutoShop LTDA', 'AutoShop', NOW()),
('Empresa Serviços', 'ES', NOW()),
('Empresa A', 'A', NOW()),
('Empresa B', 'B', NOW());

-- ============================================================
-- 2. USUARIOS (Clientes, Funcionários, Fornecedores)
-- ============================================================
INSERT INTO usuario (nome, nome_social) VALUES
('João Silva', 'João'),
('Ana Funcionária', NULL),
('Maria Cliente', 'Maria'),
('Pedro Fornecedor', NULL),
('Fornecedor Teste', NULL),
('Usuário Com Endereço', NULL),
('Usuário Sem Endereço', NULL),
('Cliente Teste', NULL),
('Funcionário Teste', Null),
('Cliente Dup', NULL),
('Funcionário Dup', NULL);

-- ============================================================
-- 3. PERFIS DE USUARIO (0=CLIENTE, 1=FUNCIONARIO, 2=FORNECEDOR)
-- ============================================================
INSERT INTO usuario_perfis (usuario_id, perfis) VALUES
(1, 0),
(2, 1),
(3, 0),
(4, 2),
(5, 1),
(6, 1),
(7, 1),
(8, 0),
(9, 0),
(10, 2),
(11, 2);

-- ============================================================
-- 4. ENDERECOS
-- ============================================================
INSERT INTO endereco (estado, cidade, bairro, rua, numero, codigo_postal, informacoes_adicionais) VALUES
('SP', 'São Paulo', 'Centro', 'Av Paulista', '1000', '01310-100', NULL),
('RJ', 'Rio de Janeiro', 'Copacabana', 'Av Atlântica', '500', '22010-020', NULL),
('MG', 'Belo Horizonte', 'Savassi', 'Rua Bahia', '1500', '30140-071', NULL),
('SP', 'São Paulo', 'Vila Mariana', 'Rua Vergueiro', '2000', '01504-001', NULL);

-- ============================================================
-- 5. VINCULAÇÃO USUARIO-ENDERECO
-- ============================================================
UPDATE usuario SET endereco_id = 1 WHERE id = 6;
UPDATE usuario SET endereco_id = 2 WHERE id = 1;
UPDATE usuario SET endereco_id = 3 WHERE id = 2;
UPDATE usuario SET endereco_id = 4 WHERE id = 3;

-- ============================================================
-- 6. TELEFONES
-- ============================================================
INSERT INTO telefone (ddd, numero) VALUES
('11', '99999-0000'),
('21', '88888-1111'),
('31', '77777-2222'),
('11', '66666-3333'),
('11', '55555-4444'),
('21', '44444-5555');

-- ============================================================
-- 7. VINCULAÇÃO USUARIO-TELEFONES
-- ============================================================
INSERT INTO usuario_telefones (usuario_id, telefones_id) VALUES
(1, 1),
(1, 2),
(2, 3),
(3, 4),
(4, 5),
(5, 6);

-- Vinculação de telefones para empresas
INSERT INTO empresa_telefones (empresa_id, telefones_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

-- ============================================================
-- 8. DOCUMENTOS
-- ============================================================
INSERT INTO documento (tipo, numero, data_emissao) VALUES
(1, 1111111111, '2020-01-01'),
(1, 11112111111, '2019-06-15'),
(2, 111311111, '2021-03-20'),
(2, 3333333, '2018-02-10'),
(3, 4444444444444, '2017-12-05'),
(4, 6666666666666, '2022-08-30');

-- ============================================================
-- 9. VINCULAÇÃO USUARIO-DOCUMENTOS
-- ============================================================
INSERT INTO usuario_documentos (usuario_id, documentos_id) VALUES
(1, 1),
(1, 2),
(2, 3),
(3, 4),
(4, 5),
(5, 6);

-- ============================================================
-- 10. EMAILS
-- ============================================================
INSERT INTO email (endereco) VALUES
('joao@email.com'),
('ana@email.com'),
('maria@email.com'),
('pedro@email.com'),
('fornecedor@email.com'),
('usuario@email.com'),
('teste1@email.com'),
('teste2@email.com');

-- ============================================================
-- 11. VINCULAÇÃO USUARIO-EMAILS
-- ============================================================
INSERT INTO usuario_emails (usuario_id, emails_id) VALUES
(1, 1),
(1, 7),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(8, 8);

-- ============================================================
-- 12. MERCADORIAS
-- ============================================================
INSERT INTO mercadoria (nome, descricao, quantidade, valor, validade, fabricacao, cadastro) VALUES
('Oleo Motor 5W30', 'Óleo sintético de alta qualidade', 10, 45.90, '2026-12-31', '2024-01-01', NOW()),
('Filtro Ar', 'Filtro de ar para motor', 5, 25.00, '2026-12-31', '2024-01-01', NOW()),
('Filtro Oleo', 'Filtro de óleo original', 8, 35.50, '2026-12-31', '2024-01-01', NOW()),
('Pastilha Freio', 'Pastilha de freio semi-metálica', 12, 120.00, '2026-12-31', '2024-01-01', NOW()),
('Disco Freio', 'Disco de freio ventilado', 6, 180.00, '2026-12-31', '2024-01-01', NOW()),
('Correia Distribuição', 'Correia de distribuição original', 3, 250.00, '2026-12-31', '2024-01-01', NOW()),
('Bateria Automotiva', 'Bateria 60Ah 12V', 4, 450.00, '2026-12-31', '2024-01-01', NOW()),
('Vela Ignição', 'Jogo de velas de ignição', 15, 45.00, '2026-12-31', '2024-01-01', NOW());

-- ============================================================
-- 13. VINCULAÇÃO USUARIO-MERCADORIAS (Fornecedores)
-- ============================================================
INSERT INTO usuario_mercadorias (usuario_id, mercadorias_id) VALUES
(4, 1),
(4, 2),
(4, 3),
(5, 4),
(5, 5),
(5, 6),
(5, 7),
(5, 8);

-- Vinculação de mercadorias para empresas
INSERT INTO empresa_mercadorias (empresa_id, mercadorias_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 4),
(2, 5),
(3, 6),
(3, 7),
(4, 8);

-- ============================================================
-- 14. SERVICOS
-- ============================================================
INSERT INTO servico (nome, valor, descricao) VALUES
('Troca de Óleo', 80.00, 'Troca completa de óleo e filtro'),
('Alinhamento', 60.00, 'Alinhamento de rodas'),
('Balanceamento', 50.00, 'Balanceamento de rodas'),
('Revisão', 150.00, 'Revisão completa do veículo'),
('Troca de Pastilha', 120.00, 'Substituição de pastilhas de freio'),
('Polimento', 120.00, 'Polimento da lataria'),
('Limpeza Interior', 80.00, 'Limpeza completa do interior'),
('Troca de Pneus', 200.00, 'Substituição de pneus');

-- ============================================================
-- 15. VINCULAÇÃO EMPRESA-SERVICOS
-- ============================================================
INSERT INTO empresa_servicos (empresa_id, servicos_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 4),
(2, 5),
(3, 6),
(3, 7),
(4, 8);

-- ============================================================
-- 16. VEICULOS
-- ============================================================
INSERT INTO veiculo (tipo, modelo, placa, proprietario_id) VALUES
(1, 'Honda Civic', 'ABC-1234', 1),
(2, 'Toyota Corolla', 'XYZ-5678', 3),
(3, 'Yamaha XJ6', 'DEF-9012', 7),
(1, 'Ford Ranger', 'GHI-3456', 8),
(1, 'VW Gol', 'JKL-7890', 10),
(1, 'Fiat Uno', 'MNO-1234', 6);

-- ============================================================
-- 17. VENDAS
-- ============================================================
INSERT INTO venda (identificacao, cadastro, cliente_id, funcionario_id, veiculo_id) VALUES
('VND-001', NOW(), 1, 2, 1),
('VND-002', NOW(), 3, 2, 2),
('VND-003', NOW(), 7, 9, 3),
('VND-004', NOW(), 8, 9, 4),
('VND-005', NOW(), 10, 11, 5),
('VND-006', NOW(), 6, 4, 6);

-- ============================================================
-- 18. VINCULAÇÃO VENDA-MERCADORIAS (Uma venda pode ter múltiplas mercadorias)
-- ============================================================
INSERT INTO venda_mercadorias (venda_id, mercadorias_id) VALUES
(1, 1),
(2, 2),
(2, 3),
(2, 4),
(3, 5),
(3, 6),
(4, 6),
(4, 7),
(5, 8),
(5, 1),
(6, 3),
(6, 4);

-- ============================================================
-- 19. VINCULAÇÃO VENDA-SERVICOS
-- ============================================================
INSERT INTO venda_servicos (venda_id, servicos_id) VALUES
(1, 1),
(1, 2),
(2, 3),
(2, 4),
(3, 5),
(4, 6),
(4, 7),
(5, 8),
(6, 1),
(6, 3);

-- ============================================================
-- 20. VINCULOS FINAIS USUARIO-VENDA
-- ============================================================
INSERT INTO usuario_vendas (usuario_id, vendas_id) VALUES
(1, 1),
(1, 2),
(3, 3),
(8, 4),
(10, 5),
(6, 6);

-- ============================================================
-- 21. VINCULOS FINAIS EMPRESA-VENDA
-- ============================================================
INSERT INTO empresa_vendas (empresa_id, vendas_id) VALUES
(1, 1),
(1, 2),
(2, 3),
(2, 4),
(3, 5),
(4, 6);

-- ============================================================
-- 22. VINCULOS FINAIS USUARIO-VEICULO
-- ============================================================
INSERT INTO usuario_veiculos (usuario_id, veiculos_id) VALUES
(1, 1),
(3, 2),
(7, 3),
(8, 4),
(10, 5),
(6, 6);

-- ============================================================
-- FIM DO SCRIPT DE POPULACAO
-- ============================================================


select * from Usuario;