-- Usuário administrador inicial.
-- Sem esta linha não há como criar o primeiro ADMIN/DONO: o cadastro publico
-- (POST /api/auth/register) sempre cria com cargo CLIENTE.
-- Senha em texto puro: admin123 (hash BCrypt, strength 10). Trocar em produção.
INSERT INTO usuarios (nome, email, senha, cargo)
SELECT 'Administrador',
       'admin@trimly.com',
       '$2a$10$Pf.UQ1uTfGNqb2qnY36eHeIoG2ibVgBeHpCZe.EqF4HyscAxV14QC',
       'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@trimly.com');
