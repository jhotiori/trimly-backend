package org.trimly.backend.config.openapi;

/**
 * Corpos de erro de exemplo usados nas anotações do Swagger.
 */
public final class OpenApiExamples {
    /**
     * Exemplo `400` de corpo ausente ou mal formatado.
     */
    public static final String CORPO_INVALIDO = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Corpo da requisição ausente ou mal formatado\"}";

    /**
     * Exemplo `400` de parâmetro com valor inválido.
     */
    public static final String PARAMETRO_INVALIDO = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Parâmetro da requisição com valor inválido\"}";

    /**
     * Exemplo `400` de campos inválidos na criação de agendamento.
     */
    public static final String AGENDAMENTO_CRIACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Data não pode ser nula; Horário não pode ser nulo; \
            Serviço não pode ser nulo; Usuário não pode ser nulo"}""";

    /**
     * Exemplo `400` de campos inválidos na atualização de agendamento.
     */
    public static final String AGENDAMENTO_ATUALIZACAO_INVALIDA = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Horario deve estar no presente ou futuro\"}";

    /**
     * Exemplo `400` de campos inválidos na criação de disponibilidade.
     */
    public static final String DISPONIBILIDADE_CRIACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Dia da semana não pode ser nulo; Hora de fim não pode \
            ser nula; Hora de início não pode ser nula"}""";

    /**
     * Exemplo `400` de campos inválidos na criação de serviço.
     */
    public static final String SERVICO_CRIACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Duração não pode ser vazia; Nome não pode ser vazio; \
            Valor não pode ser vazio"}""";

    /**
     * Exemplo `400` de campos inválidos na atualização de serviço.
     */
    public static final String SERVICO_ATUALIZACAO_INVALIDA = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Duração deve ser positiva; Valor deve ser positivo\"}";

    /**
     * Exemplo `400` de campos inválidos na criação de usuário.
     */
    public static final String USUARIO_CRIACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Email não pode ser vazio; Nome não pode ser vazio; \
            Senha deve ter no mínimo 6 caracteres; Senha não pode ser vazia"}""";

    /**
     * Exemplo `400` de campos inválidos na atualização de usuário.
     */
    public static final String USUARIO_ATUALIZACAO_INVALIDA = """
            {"status": 400, "error": "Bad Request", "message": "Não foi recebido um formato de e-mail válido; \
            Senha deve ter no mínimo 6 caracteres"}""";

    /**
     * Exemplo `400` de campos inválidos no login.
     */
    public static final String LOGIN_INVALIDO = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Email não pode ser vazio; Senha não pode ser vazia\"}";

    /**
     * Exemplo `401` de credenciais inválidas.
     */
    public static final String CREDENCIAIS_INVALIDAS = "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Credenciais inválidas\"}";

    /**
     * Exemplo `404` de agendamento não encontrado.
     */
    public static final String AGENDAMENTO_NAO_ENCONTRADO = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Agendamento não foi encontrado\"}";

    /**
     * Exemplo `404` de disponibilidade não encontrada.
     */
    public static final String DISPONIBILIDADE_NAO_ENCONTRADA = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Disponibilidade não foi encontrada\"}";

    /**
     * Exemplo `404` de serviço não encontrado.
     */
    public static final String SERVICO_NAO_ENCONTRADO = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Serviço não encontrado\"}";

    /**
     * Exemplo `404` de usuário não encontrado.
     */
    public static final String USUARIO_NAO_ENCONTRADO = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"Usuario não foi encontrado\"}";

    /**
     * Exemplo `409` de horário ocupado por outro agendamento.
     */
    public static final String AGENDAMENTO_CONFLITO = "{\"status\": 409, \"error\": \"Conflict\", \"message\": \"O horário escolhido já está ocupado por outro agendamento\"}";

    /**
     * Exemplo `409` de janela sobreposta a outra disponibilidade.
     */
    public static final String DISPONIBILIDADE_CONFLITO = """
            {"status": 409, "error": "Conflict", "message": "O horário informado já está ocupado por outra \
            disponibilidade nesse dia"}""";

    /**
     * Exemplo `409` de nome de serviço duplicado.
     */
    public static final String SERVICO_NOME_DUPLICADO = "{\"status\": 409, \"error\": \"Conflict\", \"message\": \"Já existe um serviço com esse nome\"}";

    /**
     * Exemplo `409` de remoção de serviço com agendamento pendente.
     */
    public static final String SERVICO_COM_AGENDAMENTO_PENDENTE = """
            {"status": 409, "error": "Conflict", "message": "Não é possível remover um serviço com agendamento \
            pendente"}""";

    /**
     * Exemplo `409` de e-mail já cadastrado.
     */
    public static final String USUARIO_EMAIL_EXISTENTE = "{\"status\": 409, \"error\": \"Conflict\", \"message\": \"Já existe um usuário com esse e-mail\"}";

    /**
     * Exemplo `409` de remoção de usuário com agendamento pendente.
     */
    public static final String USUARIO_COM_AGENDAMENTO_PENDENTE = """
            {"status": 409, "error": "Conflict", "message": "Não é possível remover um usuário com agendamento \
            pendente"}""";

    /**
     * Exemplo `422` de agendamento no passado.
     */
    public static final String AGENDAMENTO_NO_PASSADO = "{\"status\": 422, \"error\": \"Unprocessable Content\", \"message\": \"O agendamento não pode ser no passado\"}";

    /**
     * Exemplo `422` de agendamento que passa de um dia para o outro.
     */
    public static final String AGENDAMENTO_FORA_DO_HORARIO = """
            {"status": 422, "error": "Unprocessable Content", "message": "O agendamento não pode ultrapassar o \
            horário de um dia para o outro"}""";

    /**
     * Exemplo `422` de antecedência acima de 14 dias.
     */
    public static final String AGENDAMENTO_ANTECEDENCIA_EXCEDIDA = """
            {"status": 422, "error": "Unprocessable Content", "message": "O agendamento não pode ser marcado com mais \
            de 14 dias de antecedência"}""";

    /**
     * Exemplo `422` de dia sem disponibilidade.
     */
    public static final String AGENDAMENTO_SEM_DISPONIBILIDADE = """
            {"status": 422, "error": "Unprocessable Content", "message": "Não há nenhuma disponibilidade nesse dia \
            para realizar este agendamento"}""";

    /**
     * Exemplo `422` de horário fora das janelas do dia.
     */
    public static final String AGENDAMENTO_DISPONIBILIDADE_INSUFICIENTE = """
            {"status": 422, "error": "Unprocessable Content", "message": "Não há disponibilidade suficiente nesse dia \
            para realizar este agendamento"}""";

    /**
     * Exemplo `422` de alteração de agendamento fora de `AGENDADO`.
     */
    public static final String AGENDAMENTO_STATUS_NAO_ALTERAVEL = """
            {"status": 422, "error": "Unprocessable Content", "message": "O agendamento não pode ser alterado neste \
            status"}""";

    /**
     * Exemplo `422` de status de agendamento repetido.
     */
    public static final String AGENDAMENTO_STATUS_REPETIDO = "{\"status\": 422, \"error\": \"Unprocessable Content\", \"message\": \"O agendamento já está nesse status\"}";

    /**
     * Exemplo `422` de hora de início não anterior à de fim.
     */
    public static final String DISPONIBILIDADE_HORARIO_INVALIDO = """
            {"status": 422, "error": "Unprocessable Content", "message": "Horário de inicio deve ser menor do que o \
            horário de fim"}""";

    /**
     * Exemplo `422` de status de serviço repetido.
     */
    public static final String SERVICO_STATUS_REPETIDO = "{\"status\": 422, \"error\": \"Unprocessable Content\", \"message\": \"O serviço já está nesse status\"}";

    /**
     * Exemplo `422` de desativação de serviço com agendamentos futuros.
     */
    public static final String SERVICO_DESATIVACAO_COM_AGENDAMENTO = """
            {"status": 422, "error": "Unprocessable Content", "message": "O serviço não pode ser desativado enquanto \
            tiver agendamentos futuros"}""";

    /**
     * Exemplo `422` de cargo de usuário repetido.
     */
    public static final String USUARIO_CARGO_REPETIDO = "{\"status\": 422, \"error\": \"Unprocessable Content\", \"message\": \"O usuário já possui esse cargo\"}";

    /**
     * Exemplo `422` de remoção do cargo do único `DONO`.
     */
    public static final String USUARIO_UNICO_DONO = """
            {"status": 422, "error": "Unprocessable Content", "message": "Não é possível remover o cargo DONO do \
            único dono cadastrado"}""";

    /**
     * Exemplo `500` de erro inesperado.
     */
    public static final String ERRO_INESPERADO = """
            {"status": 500, "error": "Internal Server Error", "message": "Ocorreu um erro inesperado ao processar a \
            requisição"}""";

    private OpenApiExamples() {}
}
