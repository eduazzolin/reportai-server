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
public class Categoria {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 255)
   private String nome;

   @Column(length = 50)
   private String icone;

   @CreationTimestamp
   @Column(updatable = false)
   private LocalDateTime dtCriacao;

   @UpdateTimestamp
   @Column()
   private LocalDateTime dtModificacao;

   @Column()
   private Boolean isDeleted = false;
}
