package com.reportai.reportaiserver.dto;

import com.reportai.reportaiserver.model.Categoria;
import com.reportai.reportaiserver.model.Imagem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDTO {

   private Long id;
   private String titulo;
   private String descricao;
   private String localizacao;
   private Double latitude;
   private Double longitude;
   private LocalDateTime dtCriacao;
   private LocalDateTime dtModificacao;
   private Boolean isConcluido = false;
   private Boolean isDeleted = false;

   private Categoria categoria;

   private List<Imagem> imagens;

   private UsuarioResumidoDTO usuario;

   private Integer interacoesRelevante;
   private Integer interacoesConcluido;

}
