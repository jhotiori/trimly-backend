-- Três clientes de exemplo para exercitar Agendamento localmente.
-- Senha em texto puro para os três: 123456 (hash BCrypt, strength 10).
INSERT INTO usuarios (nome, email, senha, cargo)
SELECT 'João Pereira',
       'joao.pereira@trimly.com',
       '$2a$10$pQfld7t2N7vHgygnl3Htnec6pjYZkW.lMWz/K7wCIV4Q3PZrSsB.u',
       'CLIENTE'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'joao.pereira@trimly.com');

INSERT INTO usuarios (nome, email, senha, cargo)
SELECT 'Maria Oliveira',
       'maria.oliveira@trimly.com',
       '$2a$10$pQfld7t2N7vHgygnl3Htnec6pjYZkW.lMWz/K7wCIV4Q3PZrSsB.u',
       'CLIENTE'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'maria.oliveira@trimly.com');

INSERT INTO usuarios (nome, email, senha, cargo)
SELECT 'Carlos Andrade',
       'carlos.andrade@trimly.com',
       '$2a$10$pQfld7t2N7vHgygnl3Htnec6pjYZkW.lMWz/K7wCIV4Q3PZrSsB.u',
       'CLIENTE'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'carlos.andrade@trimly.com');

-- Cinco serviços comuns de barbearia, todos ATIVO.
INSERT INTO servicos (nome, valor, duracao, status)
SELECT 'Corte Masculino', 40.00, 30, 'ATIVO'
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Corte Masculino');

INSERT INTO servicos (nome, valor, duracao, status)
SELECT 'Barba', 25.00, 20, 'ATIVO'
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Barba');

INSERT INTO servicos (nome, valor, duracao, status)
SELECT 'Corte e Barba', 60.00, 50, 'ATIVO'
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Corte e Barba');

INSERT INTO servicos (nome, valor, duracao, status)
SELECT 'Sobrancelha', 15.00, 10, 'ATIVO'
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Sobrancelha');

INSERT INTO servicos (nome, valor, duracao, status)
SELECT 'Hidratação Capilar', 35.00, 40, 'ATIVO'
WHERE NOT EXISTS (SELECT 1 FROM servicos WHERE nome = 'Hidratação Capilar');

-- Disponibilidades: SEGUNDA a QUINTA das 07h-12h e 13h-18h; SEXTA das 09h-12h e 13h-16h.
-- Sem linhas para SABADO/DOMINGO.
INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'SEGUNDA', '07:00', '12:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'SEGUNDA' AND hora_inicio = '07:00' AND hora_fim = '12:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'SEGUNDA', '13:00', '18:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'SEGUNDA' AND hora_inicio = '13:00' AND hora_fim = '18:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'TERCA', '07:00', '12:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'TERCA' AND hora_inicio = '07:00' AND hora_fim = '12:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'TERCA', '13:00', '18:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'TERCA' AND hora_inicio = '13:00' AND hora_fim = '18:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'QUARTA', '07:00', '12:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'QUARTA' AND hora_inicio = '07:00' AND hora_fim = '12:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'QUARTA', '13:00', '18:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'QUARTA' AND hora_inicio = '13:00' AND hora_fim = '18:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'QUINTA', '07:00', '12:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'QUINTA' AND hora_inicio = '07:00' AND hora_fim = '12:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'QUINTA', '13:00', '18:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'QUINTA' AND hora_inicio = '13:00' AND hora_fim = '18:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'SEXTA', '09:00', '12:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'SEXTA' AND hora_inicio = '09:00' AND hora_fim = '12:00');

INSERT INTO disponibilidades (dia_semana, hora_inicio, hora_fim)
SELECT 'SEXTA', '13:00', '16:00'
WHERE NOT EXISTS (SELECT 1 FROM disponibilidades WHERE dia_semana = 'SEXTA' AND hora_inicio = '13:00' AND hora_fim = '16:00');
