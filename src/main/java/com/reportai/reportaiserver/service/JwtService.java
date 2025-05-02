package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.JwtRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class JwtService implements JwtRepository {

   @Value("${JWT_EXPIRACAO}")
   private String expiracao;

   @Value("${JWT_CHAVE_ASSINATURA}")
   private String chaveAssinatura;

   @Autowired
   private UsuarioService usuarioService;


   /**
    * Gera um token JWT para o usuário informado.
    *
    * @param usuario objeto do tipo Usuario contendo as informações do usuário
    * @return um token JWT gerado em string
    */
   @Override
   public String gerarToken(Usuario usuario) {

      LocalDateTime dataExpiracao = gerarDataExpiracao();
      SecretKey key = Keys.hmacShaKeyFor(chaveAssinatura.getBytes(StandardCharsets.UTF_8));
      Instant instant = dataExpiracao.atZone(ZoneId.systemDefault()).toInstant();

      return Jwts.builder()
              .setSubject(usuario.getEmail())
              .claim("id", usuario.getId())
              .claim("nome", usuario.getNome())
              .claim("expiracao", dataExpiracao.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
              .claim("role", usuario.getRole())
              .setExpiration(Date.from(instant))
              .signWith(key, SignatureAlgorithm.HS512)
              .compact();
   }

   /**
    * Gera a data de expiração do token JWT.
    *
    * @return LocalDateTime
    */
   public LocalDateTime gerarDataExpiracao() {
      return LocalDateTime.now().plusMinutes(Long.parseLong(this.expiracao));
   }

   /**
    * Obtém as claims do token JWT informado. (id, email, nome, expiracao)
    *
    * @param token
    * @return Claims
    * @throws ExpiredJwtException
    */
   @Override
   public Claims obterClaims(String token) throws ExpiredJwtException {
      SecretKey key = Keys.hmacShaKeyFor(chaveAssinatura.getBytes(StandardCharsets.UTF_8));
      return Jwts.parserBuilder()
              .setSigningKey(key)
              .build()
              .parseClaimsJws(token)
              .getBody();
   }

   /**
    * Verifica se o token JWT informado é válido e verifica a data de expiração também.
    *
    * @param token
    * @return
    */
   @Override
   public boolean isTokenValido(String token) {
      try {
         Claims claims = obterClaims(token);
         Date dataExpiracao = claims.getExpiration();
         LocalDateTime data = dataExpiracao.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
         return LocalDateTime.now().isBefore(data);
      } catch (Exception e) {
         return false;
      }
   }

   /**
    * Obtém o login do usuário a partir do token JWT informado.
    *
    * @param token
    * @return String
    */
   @Override
   public String obterLoginUsuario(String token) {
      Claims claims = obterClaims(token);
      return claims.getSubject();
   }

   /**
    * Retorna um objeto do tipo Usuario a partir do token JWT informado.
    *
    * @param token
    * @return Usuario
    */
   public Usuario obterUsuarioRequisitante(String token) {
      token = token.replace("Bearer ", "");
      Claims claims = obterClaims(token);
      Long id = claims.get("id", Long.class);
      return usuarioService.buscarPorId(id);
   }

   /**
    * Verifica se o usuário requisitante é ADMIN.
    * Se não for, lança uma exceção USUARIO_SEM_PERMISSAO.
    *
    * @param usuarioRequisitante
    */
   public void verificarSeUsuarioADMIN(Usuario usuarioRequisitante) {
      if (usuarioRequisitante.getRole() != Usuario.Roles.ADMIN) {
         throw new CustomException(ErrorDictionary.USUARIO_SEM_PERMISSAO);
      }
   }

}
