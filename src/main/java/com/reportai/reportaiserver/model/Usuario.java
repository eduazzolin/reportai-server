package com.reportai.reportaiserver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   private Usuario.Roles role;

   private String nome;

   @Column(unique = true)
   private String email;

   @Column(unique = true)
   private String cpf;

   private String senha;

   @CreationTimestamp
   @Column(updatable = false)
   private LocalDateTime dtCriacao;

   @UpdateTimestamp
   @Column()
   private LocalDateTime dtModificacao;

   @Column()
   private Boolean isDeleted = false;

   public enum Roles {
      USUARIO, ADMIN
   }

}