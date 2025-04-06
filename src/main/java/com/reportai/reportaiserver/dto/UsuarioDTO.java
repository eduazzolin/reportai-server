package com.reportai.reportaiserver.dto;

import com.reportai.reportaiserver.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

   private Long id;
   private String nome;
   private String email;
   private String cpf;
   private LocalDateTime dtCriacao;
   private LocalDateTime dtModificacao;
   private Usuario.Roles role;

}

