-- Listagem e filtro de agendamentos do usuário; o PostgreSQL não indexa chaves estrangeiras sozinho.
CREATE INDEX IF NOT EXISTS idx_agendamentos_usuario_id ON agendamentos (usuario_id);

-- Verificação de agendamentos pendentes antes de excluir um serviço, e joins por serviço.
CREATE INDEX IF NOT EXISTS idx_agendamentos_servico_id ON agendamentos (servico_id);

-- Checagem de conflito de horário: busca por status AGENDADO dentro do intervalo de datas do dia.
CREATE INDEX IF NOT EXISTS idx_agendamentos_status_data ON agendamentos (status, data);

-- Validação de disponibilidade e de conflito entre janelas do mesmo dia da semana.
CREATE INDEX IF NOT EXISTS idx_disponibilidades_dia_semana ON disponibilidades (dia_semana);
