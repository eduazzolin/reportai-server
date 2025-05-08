package com.reportai.reportaiserver.exception;

public enum ErrorDictionary {

   EMAIL_JA_EXISTE("ERRO-001", "E-mail já está em uso.")
   ,CPF_JA_EXISTE("ERRO-002", "CPF já está em uso.")
   ,DADOS_INVALIDOS("ERRO-003", "Dados inválidos.")
   ,USUARIO_NAO_ENCONTRADO("ERRO-004", "Usuário não encontrado.")
   ,ERRO_PREENCHIMENTO("ERRO-005", "Erro no preenchimento dos campos.")
   ,EMAIL_INVALIDO("ERRO-006", "E-mail inválido.")
   ,CPF_INVALIDO("ERRO-007", "CPF inválido.")
   ,SENHA_INVALIDA("ERRO-008", "Senha inválida.")
   ,USUARIO_DELETADO("ERRO-009", "Usuário deletado.")
   ,REGISTRO_NAO_ENCONTRADO("ERRO-010", "Registro não encontrado.")
   ,CATEGORIA_NAO_ENCONTRADA("ERRO-011", "Categoria não encontrada.")
   ,DISTANCIA_INVALIDA("ERRO-012", "Distância do centro de Florianópolis ultrapassa o permitido.")
   ,FORMATO_INCORRETO("ERRO-013", "Formato de imagem incorreto. Utilize .jpeg ou .png.")
   ,TAMANHO_MAXIMO("ERRO-014", "Tamanho máximo de imagem excedido. Limite de 5MB.")
   ,USUARIO_SEM_PERMISSAO("ERRO-015", "Usuário sem permissão para realizar a operação.")
   ,ERRO_OPENAI("ERRO-016", "Erro ao se comunicar com o serviço OpenAI.")
   ,INTERACAO_DUPLICADA("ERRO-017", "Interação já existe para o registro.")
   ,SEM_PERMISSAO("ERRO-018", "Usuário sem permissão para realizar a operação.")
   ,ERRO_GCS("ERRO-019", "Erro ao se comunicar com o Google Cloud Storage.")
   ,REGISTRO_JA_CONCLUIDO("ERRO-020", "Registro já está concluído.")
   ,TOKEN_INVALIDO("ERRO-021", "Erro de autenticação.")
   ,ERRO_EMAIL("ERRO-022", "Erro ao enviar email.")
   ;
   private final String codigo;
   private final String descricao;

   ErrorDictionary(String codigo, String descricao) {
      this.codigo = codigo;
      this.descricao = descricao;
   }

   public String getCodigo() {
      return codigo;
   }

   public String getDescricao() {
      return descricao;
   }
}
