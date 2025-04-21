package com.reportai.reportaiserver.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Imagem {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Lob
   @Column(length = 9999)
   private String caminho;

   @ManyToOne
   @JoinColumn(name = "id_registro", nullable = false)
   @JsonBackReference
   private Registro registro;


   @CreationTimestamp
   @Column(updatable = false)
   private LocalDateTime dtCriacao;
}
