package com.reportai.reportaiserver.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interacao {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private TipoInteracao tipo;

   @ManyToOne
   @JoinColumn(name = "id_usuario", nullable = false)
   @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
   @JsonIdentityReference(alwaysAsId = true)
   private Usuario usuario;

   @ManyToOne
   @JoinColumn(name = "id_registro", nullable = false)
   @JsonBackReference
   private Registro registro;

   public enum TipoInteracao {
      RELEVANTE, CONCLUIDO
   }
}
