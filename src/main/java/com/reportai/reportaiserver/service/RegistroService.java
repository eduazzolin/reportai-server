package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.dto.MeusRegistrosDTO;
import com.reportai.reportaiserver.dto.RegistroDTO;
import com.reportai.reportaiserver.dto.RegistroListagemAdminProjection;
import com.reportai.reportaiserver.dto.RegistrosAdminPaginadoDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.mapper.RegistroMapper;
import com.reportai.reportaiserver.model.ConclusaoProgramada;
import com.reportai.reportaiserver.model.Imagem;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.ConclusaoProgramadaRepository;
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
   private ConclusaoProgramadaRepository conclusaoProgramadaRepository;

   @Autowired
   private Validacoes validacoes;

   @Autowired
   private ImagemService imagemService;

   /**
    * Salva um registro no banco de dados.
    *
    * @param registro
    * @return registro com ID gerado
    */
   public Registro salvar(Registro registro) {
      validacoes.validarRegistro(registro);
      return repository.save(registro);
   }

   /**
    * Busca um registro por ID.
    *
    * @param id
    * @return registro encontrado
    */
   public Registro buscarPorId(Long id) {
      Optional<Registro> registro = repository.findByIdAndIsDeleted(id, false);
      if (registro.isEmpty()) {
         throw new CustomException(ErrorDictionary.REGISTRO_NAO_ENCONTRADO);
      }
      return registro.get();
   }


   /**
    * Busca registros por distância a partir de uma localização (latitude e longitude).
    * Para mais informações, consulte a procedure SP_REGISTROS_POR_DISTANCIA.
    *
    * @param latitude
    * @param longitude
    * @param distancia
    * @param limite
    * @param filtro
    * @param ordenacao
    * @return lista de registros encontrados
    */
   public List<Registro> buscarPorDistancia(double latitude, double longitude, double distancia, int limite, String filtro, String ordenacao) {
      return repository.findByDistance(latitude, longitude, distancia, limite, filtro, ordenacao);
   }

   /**
    * Busca todos os registros.
    *
    * @return lista de registros encontrados
    */
   public List<Registro> buscarTodos() {
      return repository.findAll();
   }

   /**
    * Marca um registro como excluído.
    *
    * @param registro
    */
   public void remover(Registro registro) {
      registro.setIsDeleted(true);
      registro.setDtExclusao(LocalDateTime.now());
      repository.save(registro);

      // Remover as imagens
      for (Imagem imagem : registro.getImagens()) {
         imagemService.remover(imagem);
      }
   }

   /**
    * Busca registros de um usuário específico, paginados.
    *
    * @param usuario
    * @param pagina
    * @param limite
    * @return MeusRegistrosDTO com informações de paginação e lista de registros
    */
   public MeusRegistrosDTO buscarMeusRegistrosDTOPorUsuario(Usuario usuario, int pagina, int limite) {

      Pageable pageable = PageRequest.of(pagina, limite, Sort.by("isConcluido").and(Sort.by("dtCriacao").descending()));
      Page<Registro> resultado = repository.findByUsuarioAndIsDeleted(usuario, false, pageable);

      List<Registro> registros = resultado.getContent();
      int totalPaginas = resultado.getTotalPages();
      long totalRegistros = resultado.getTotalElements();

      List<RegistroDTO> registrosDTO = new ArrayList<>();
      for (Registro registro : registros) {
         RegistroDTO registroDTO = RegistroMapper.toDTO(registro);
         registroDTO.setDtConclusaoProgramada(buscarDtConclusaoProgramadaPorId(registro.getId()));
         registrosDTO.add(registroDTO);
      }

      return MeusRegistrosDTO.builder()
              .pagina(pagina)
              .limite(limite)
              .totalPaginas(totalPaginas)
              .totalRegistros(totalRegistros)
              .registros(registrosDTO)
              .build();
   }

   /**
    * Conclui um registro, marcando-o como concluído e definindo a data de conclusão.
    * Se houver uma conclusão programada associada, ela será removida.
    *
    * @param registro
    * @return
    */
   public void concluirPorId(Registro registro) {

      if (registro.getIsConcluido()) {
         throw new CustomException(ErrorDictionary.REGISTRO_JA_CONCLUIDO);
      }

      removerConclusaoProgramada(registro);

      registro.setIsConcluido(true);
      registro.setDtConclusao(LocalDateTime.now());
      repository.save(registro);

   }

   /**
    * Marca uma conclusão programada como removida, definindo a data de remoção.
    *
    * @param registro
    */
   public void removerConclusaoProgramada(Registro registro) {
      ConclusaoProgramada conclusaoProgramada = conclusaoProgramadaRepository.findByRegistroAndRemovidaEm(registro, null);
      if (conclusaoProgramada != null) {
         conclusaoProgramada.setRemovidaEm(LocalDateTime.now());
         conclusaoProgramadaRepository.save(conclusaoProgramada);
      }
   }

   /**
    * Busca registros de forma paginada para o admin, com base em diversos parâmetros de filtro.
    * Para mais informações, consulte a procedure SP_ADMIN_LISTAR_REGISTROS.
    *
    * @param pIdNome
    * @param idUsuario
    * @param idCategoria
    * @param bairro
    * @param status
    * @param pagina
    * @param limite
    * @param ordenacao
    * @return RegistrosAdminPaginadoDTO
    */
   public RegistrosAdminPaginadoDTO buscarRegistrosAdminpaginadoDTOPorTermos(String pIdNome, Long idUsuario, Long idCategoria, String bairro, String status, int pagina, int limite, String ordenacao) {

      int offset = pagina * limite;
      int totalRegistros = repository.countAdminRegistros(pIdNome, idUsuario, idCategoria, bairro, status);
      int totalPaginas = (int) Math.ceil((double) totalRegistros / limite);

      List<RegistroListagemAdminProjection> registrosDTO = repository.searchAdminRegistros(pIdNome, idUsuario, idCategoria, bairro, status, offset, limite, ordenacao);


      return RegistrosAdminPaginadoDTO.builder()
              .pagina(pagina)
              .limite(limite)
              .totalPaginas(totalPaginas)
              .totalRegistros(totalRegistros)
              .registros(registrosDTO)
              .build();

   }

   public LocalDateTime buscarDtConclusaoProgramadaPorId(Long id) {
      Registro registro = buscarPorId(id);
      ConclusaoProgramada conclusaoProgramada = conclusaoProgramadaRepository.findByRegistroAndRemovidaEm(registro, null);
      if (conclusaoProgramada == null) {
         return null;
      } else {
         return conclusaoProgramada.getConclusaoProgramadaPara();
      }
   }

}
