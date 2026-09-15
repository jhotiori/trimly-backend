package org.trimly.backend.config.openapi;

/**
 * Corpos de erro de exemplo usados nas anotações do Swagger dos controllers.
 *
 * Cada constante reproduz um {@code ErrorResponseDTO} real devolvido pelos handlers de exceção, com a
 * mensagem fixa lançada pela regra correspondente.
 */
public final class OpenApiExamples {
    public static final String CORPO_INVALIDO = """
            {"status": 400, "error": "Bad Request", "message": "Corpo da requisição ausente ou mal formatado"}""";
    public static final String PARAMETRO_INVALIDO = """
            {"status": 400, "error": "Bad Request", "message": "Parâmetro da requisição com valor inválido"}""";
    public static final String AGENDAMENTO_CRIACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Data não pode ser nula; Horário não pode ser nulo; \
            Serviço não pode ser nulo; Usuário não pode ser nulo"}""";
    public static final String AGENDAMENTO_ATUALIZACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Horario deve estar no presente ou futuro"}""";
    public static final String DISPONIBILIDADE_CRIACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Dia da semana não pode ser nulo; Hora de fim não pode \
            ser nula; Hora de início não pode ser nula"}""";
    public static final String SERVICO_CRIACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Duração não pode ser vazia; Nome não pode ser vazio; \
            Valor não pode ser vazio"}""";
    public static final String SERVICO_ATUALIZACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Duração deve ser positiva; Valor deve ser positivo"}""";
    public static final String USUARIO_CRIACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Email não pode ser vazio; Nome não pode ser vazio; \
            Senha não pode ser vazia"}""";
    public static final String USUARIO_ATUALIZACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Não foi recebido um formato de e-mail válido"}""";
    public static final String LOGIN_INVALIDO = """
            {"status": 400, "error": "Bad Request", "message": "Email não pode ser vazio; Senha não pode ser vazia"}""";

    public static final String CREDENCIAIS_INVALIDAS = """
            {"status": 401, "error": "Unauthorized", "message": "Credenciais inválidas"}""";

    public static final String AGENDAMENTO_NAO_ENCONTRADO = """
            {"status": 404, "error": "Not Found", "message": "Agendamento não foi encontrado"}""";
    public static final String DISPONIBILIDADE_NAO_ENCONTRADA = """
            {"status": 404, "error": "Not Found", "message": "Disponibilidade não foi encontrada"}""";
    public static final String SERVICO_NAO_ENCONTRADO = """
            {"status": 404, "error": "Not Found", "message": "Serviço não encontrado"}""";
    public static final String USUARIO_NAO_ENCONTRADO = """
            {"status": 404, "error": "Not Found", "message": "Usuario não foi encontrado"}""";

    public static final String AGENDAMENTO_CONFLITO = """
            {"status": 409, "error": "Conflict", "message": "O horário escolhido já está ocupado por outro agendamento"}""";
    public static final String DISPONIBILIDADE_CONFLITO = """
            {"status": 409, "error": "Conflict", "message": "O horário informado já está ocupado por outra \
            disponibilidade nesse dia"}""";
    public static final String SERVICO_NOME_DUPLICADO = """
            {"status": 409, "error": "Conflict", "message": "Já existe um serviço com esse nome"}""";
    public static final String SERVICO_COM_AGENDAMENTO_PENDENTE = """
            {"status": 409, "error": "Conflict", "message": "Não é possível remover um serviço com agendamento \
            pendente"}""";
    public static final String USUARIO_EMAIL_EXISTENTE = """
            {"status": 409, "error": "Conflict", "message": "Já existe um usuário com esse e-mail"}""";
    public static final String USUARIO_COM_AGENDAMENTO_PENDENTE = """
            {"status": 409, "error": "Conflict", "message": "Não é possível remover um usuário com agendamento \
            pendente"}""";

    public static final String AGENDAMENTO_NO_PASSADO = """
            {"status": 422, "error": "Unprocessable Content", "message": "O agendamento não pode ser no passado"}""";
    public static final String AGENDAMENTO_FORA_DO_HORARIO = """
            {"status": 422, "error": "Unprocessable Content", "message": "O agendamento não pode ultrapassar o \
            horário de um dia para o outro"}""";
    public static final String AGENDAMENTO_SEM_DISPONIBILIDADE = """
            {"status": 422, "error": "Unprocessable Content", "message": "Não há nenhuma disponibilidade nesse dia \
            para realizar este agendamento"}""";
    public static final String AGENDAMENTO_DISPONIBILIDADE_INSUFICIENTE = """
            {"status": 422, "error": "Unprocessable Content", "message": "Não há disponibilidade suficiente nesse dia \
            para realizar este agendamento"}""";
    public static final String AGENDAMENTO_STATUS_NAO_ALTERAVEL = """
            {"status": 422, "error": "Unprocessable Content", "message": "O agendamento não pode ser alterado neste \
            status"}""";
    public static final String AGENDAMENTO_STATUS_REPETIDO = """
            {"status": 422, "error": "Unprocessable Content", "message": "O agendamento já está nesse status"}""";
    public static final String DISPONIBILIDADE_HORARIO_INVALIDO = """
            {"status": 422, "error": "Unprocessable Content", "message": "Horário de inicio deve ser menor do que o \
            horário de fim"}""";
    public static final String SERVICO_STATUS_REPETIDO = """
            {"status": 422, "error": "Unprocessable Content", "message": "O serviço já está nesse status"}""";
    public static final String SERVICO_DESATIVACAO_COM_AGENDAMENTO = """
            {"status": 422, "error": "Unprocessable Content", "message": "O serviço não pode ser desativado enquanto \
            tiver agendamentos futuros"}""";
    public static final String USUARIO_CARGO_REPETIDO = """
            {"status": 422, "error": "Unprocessable Content", "message": "O usuário já possui esse cargo"}""";
    public static final String USUARIO_UNICO_DONO = """
            {"status": 422, "error": "Unprocessable Content", "message": "Não é possível remover o cargo DONO do \
            único dono cadastrado"}""";

    public static final String ERRO_INESPERADO = """
            {"status": 500, "error": "Internal Server Error", "message": "Ocorreu um erro inesperado ao processar a \
            requisição"}""";

    private OpenApiExamples() {}
}
