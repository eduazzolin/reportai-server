package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.dto.MeusRegistrosDTO;
import com.reportai.reportaiserver.dto.RegistroDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.mapper.RegistroMapper;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.RegistroRepository;
import com.reportai.reportaiserver.utils.Validacoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RegistroService {

   @Autowired
   private RegistroRepository repository;

   @Autowired
   private Validacoes validacoes;

   public Registro save(Registro registro) {
      validacoes.validarRegistro(registro);
      return repository.save(registro);
   }

   public Registro findById(Long id) {
      Optional<Registro> registro = repository.findById(id);
      if (registro.isEmpty()) {
         throw new CustomException(ErrorDictionary.REGISTRO_NAO_ENCONTRADO);
      }
      return registro.get();
   }


   public List<Registro> findByDistancia(double latitude, double longitude, double distancia, int limite, String filtro, String ordenacao) {
      return repository.findByDistance(latitude, longitude, distancia, limite, filtro, ordenacao);
   }

   public List<Registro> findAll() {
      return repository.findAll();
   }

   public void deleteById(Long id, Usuario usuario) {
      Registro registro = findById(id);

      if (!registro.getUsuario().getId().equals(usuario.getId())) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }

      registro.setIsDeleted(true);
      registro.setDtExclusao(LocalDateTime.now());
      repository.save(registro);
   }

   public MeusRegistrosDTO listarMeusRegistros(Usuario usuario, int pagina, int limite) {

      Pageable pageable = PageRequest.of(pagina, limite, Sort.by("isConcluido").and(Sort.by("dtCriacao").descending()));
      Page<Registro> resultado = repository.findByUsuarioAndIsDeleted(usuario, false, pageable);

      List<Registro> registros = resultado.getContent();
      int totalPaginas = resultado.getTotalPages();
      long totalRegistros = resultado.getTotalElements();

      List<RegistroDTO> registrosDTO = new ArrayList<>();
      for (Registro registro : registros) {
         registrosDTO.add(RegistroMapper.toDTO(registro));
      }

      return MeusRegistrosDTO.builder()
              .pagina(pagina)
              .limite(limite)
              .totalPaginas(totalPaginas)
              .totalRegistros(totalRegistros)
              .registros(registrosDTO)
              .build();
   }

   public void ConcluirById(Long id, Usuario usuario) {
      Registro registro = findById(id);

      if (!registro.getUsuario().getId().equals(usuario.getId())) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }

      registro.setIsConcluido(true);
      registro.setDtConclusao(LocalDateTime.now());
      repository.save(registro);
   }
}
