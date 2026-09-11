-- Usuário administrador inicial.
-- Sem esta linha não há como criar o primeiro ADMIN/DONO: o cadastro publico
-- (POST /api/auth/register) sempre cria com cargo CLIENTE.
-- Senha em texto puro: ADMIN0000 (hash BCrypt, strength 10). Trocar em produção.
INSERT INTO usuarios (nome, email, senha, cargo)
SELECT 'Administrador',
       'admin@trimly.com',
       '$2a$10$dS8JisUhBjavp1reNy/1P.HUnb6/bhHOF1dcK2MOHDOq71Z9EXDCu',
       'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@trimly.com');
