package com.reportai.reportaiserver.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Registro {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 255)
   private String titulo;

   @Lob
   @Column(nullable = false, length = 3500)
   private String descricao;

   @Column(length = 512)
   private String localizacao;

   @Column(precision = 10)
   private Double latitude;

   @Column(precision = 11)
   private Double longitude;

   @CreationTimestamp
   @Column(updatable = false)
   private LocalDateTime dtCriacao;

   @UpdateTimestamp
   @Column()
   private LocalDateTime dtModificacao;

   @Column()
   private LocalDateTime dtConclusao;

   @Column()
   private LocalDateTime dtExclusao;

   @Column()
   private Boolean isConcluido = false;

   @Column()
   private Boolean isDeleted = false;

   @ManyToOne
   @JoinColumn(nullable = false)
   private Categoria categoria;

   @ManyToOne
   @JoinColumn(nullable = false)
   private Usuario usuario;

   @OneToMany(mappedBy = "registro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
   @JsonManagedReference
   private List<Imagem> imagens;

   @OneToMany(mappedBy = "registro", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
   @JsonManagedReference
   private List<Interacao> interacoes;


}