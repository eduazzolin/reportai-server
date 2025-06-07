package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.dto.UsuarioDTO;
import com.reportai.reportaiserver.dto.UsuarioListagemAdminProjection;
import com.reportai.reportaiserver.dto.UsuariosAdminPaginadoDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.mapper.UsuarioMapper;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.UsuarioRepository;
import com.reportai.reportaiserver.utils.CriptografiaUtils;
import com.reportai.reportaiserver.utils.Validacoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

   @Autowired
   private UsuarioRepository repository;

   @Autowired
   private Validacoes validacoes;

   public Usuario salvar(Usuario usuario) {
      validacoes.validarUsuario(usuario);
      usuario.setSenha(CriptografiaUtils.criptografar(usuario.getSenha()));
      return repository.save(usuario);
   }

   public Usuario editar(Usuario usuario) {
      validacoes.validarUsuario(usuario);
      return repository.save(usuario);
   }

   public Usuario autenticar(String email, String senha) {

      Optional<Usuario> usuario = repository.findByEmail(email);

      if (usuario.isEmpty()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }

      if (!CriptografiaUtils.verificarCorrespondencia(senha, usuario.get().getSenha())) {
         throw new CustomException(ErrorDictionary.SENHA_INVALIDA);
      }

      if (usuario.get().getIsDeleted()) {
         throw new CustomException(ErrorDictionary.USUARIO_DELETADO);
      }

      return usuario.get();
   }

   public UsuarioDTO buscarDTOPorId(Long id) {
      Optional<Usuario> usuario = repository.findById(id);
      if (usuario.isEmpty()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }
      return UsuarioMapper.toDTO(usuario.get());
   }

   /**
    * Busca um usuário por ID. Caso o usuário não seja encontrado, lança uma exceção.
    *
    * @param id
    * @return
    */
   public Usuario buscarPorId(Long id) {
      Optional<Usuario> usuario = repository.findByIdAndIsDeleted(id, false);
      if (usuario.isEmpty()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }
      return usuario.get();
   }

   /**
    * Busca um usuário por email. Caso o usuário não seja encontrado, lança uma exceção.
    *
    * @param email
    * @return
    */
   public Usuario buscarPorEmail(String email) {
      Optional<Usuario> usuario = repository.findByEmailAndIsDeleted(email, false);
      if (usuario.isEmpty()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }
      return usuario.get();
   }

   /**
    * Altera a senha de um usuário, os outros campos do usuário não são alterados.
    *
    * @param usuario
    * @return Usuario
    */
   public Usuario alterarSenha(Usuario usuario) {
      Usuario usuarioEncontrado = buscarPorId(usuario.getId());
      validacoes.validarSenha(usuario);
      usuarioEncontrado.setSenha(CriptografiaUtils.criptografar(usuario.getSenha()));
      return repository.save(usuarioEncontrado);
   }

   public Usuario buscarAtivosPorId(Long id) {
      Optional<Usuario> usuario = repository.findByIdAndIsDeleted(id, false);
      if (usuario.isEmpty()) {
         throw new CustomException(ErrorDictionary.USUARIO_NAO_ENCONTRADO);
      }
      return usuario.get();
   }

   public UsuariosAdminPaginadoDTO buscarUsuariosAdminPaginadoDTOPorTermos(int pagina, int limite, String termo, String id_usuario, String ordenacao) {

      int offset = pagina * limite;
      int totalUsuarios = repository.countAtivosByTermo(termo, id_usuario);
      int totalPaginas = (int) Math.ceil((double) totalUsuarios / limite);


      List<UsuarioListagemAdminProjection> usuariosDTO = repository.searchAtivosByTermo(termo, id_usuario, offset, limite, ordenacao);

      return UsuariosAdminPaginadoDTO.builder()
              .pagina(pagina)
              .limite(limite)
              .totalPaginas(totalPaginas)
              .totalUsuarios(totalUsuarios)
              .usuarios(usuariosDTO)
              .build();
   }

   public void removerPorId(Long id) {
      Usuario usuario = buscarAtivosPorId(id);
      usuario.setIsDeleted(true);
      repository.save(usuario);
   }


}
