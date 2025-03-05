package com.reportai.reportaiserver.exception;

public enum ErrorDictionary {

   // usuário
   EMAIL_JA_EXISTE("ERRO-001", "E-mail já está em uso."),
   CPF_JA_EXISTE("ERRO-002", "CPF já está em uso."),
   DADOS_INVALIDOS("ERRO-003", "Dados inválidos."),
   USUARIO_NAO_ENCONTRADO("ERRO-004", "Usuário não encontrado."),
   ERRO_PREENCHIMENTO("ERRO-005", "Erro no preenchimento dos campos."),
   EMAIL_INVALIDO("ERRO-006", "E-mail inválido.")
   ,CPF_INVALIDO("ERRO-007", "CPF inválido.")
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
