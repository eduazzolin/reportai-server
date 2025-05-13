package com.reportai.reportaiserver.repository;

import com.reportai.reportaiserver.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;


public interface JwtRepository {

   String gerarToken(Usuario usuario);

   Claims obterClaims(String token) throws ExpiredJwtException;

   boolean isTokenValido(String token);

   String obterLoginUsuario(String token);

}