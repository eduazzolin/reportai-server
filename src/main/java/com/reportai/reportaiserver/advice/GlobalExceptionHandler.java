package com.reportai.reportaiserver.advice;

import com.reportai.reportaiserver.dto.ErrorDTO;
import com.reportai.reportaiserver.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
   @ExceptionHandler(CustomException.class)
   public ResponseEntity<ErrorDTO> handleCustomException(CustomException ex) {
      // Monta o DTO de erro com código e descrição
      ErrorDTO error = new ErrorDTO(
              ex.getErrorDictionary().getCodigo(),
              ex.getErrorDictionary().getDescricao()
      );
      // Normalmente, erro de negócio costuma ser 400
      return ResponseEntity.badRequest().body(error);
   }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorDTO> handleException(Exception ex) {
      // Erro genérico (não previsto)
      ErrorDTO error = new ErrorDTO(
              "ERRO-500",
              "Ocorreu um erro inesperado"
      );
      return ResponseEntity.internalServerError().body(error);
   }
}
