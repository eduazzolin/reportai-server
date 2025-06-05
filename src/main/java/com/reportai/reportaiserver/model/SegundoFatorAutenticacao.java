package com.reportai.reportaiserver.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
public class SegundoFatorAutenticacao {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @CreationTimestamp
   @Column(updatable = false)
   private LocalDateTime dtCriacao;

   @ManyToOne
   @JoinColumn(name = "id_usuario", nullable = false)
   @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
   @JsonIdentityReference(alwaysAsId = true)
   private Usuario usuario;

   @Column()
   String codigo;

   @Column()
   private Boolean utilizado;
}
