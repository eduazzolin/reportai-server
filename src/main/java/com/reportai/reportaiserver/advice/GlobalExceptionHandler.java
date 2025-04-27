package com.reportai.reportaiserver.advice;

import com.reportai.reportaiserver.dto.ErrorDTO;
import com.reportai.reportaiserver.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

   /**
    * Tratar exceções personalizadas. Monta um DTO com código e descrição e devolve com status 400.
    *
    * @param ex CustomException
    * @return ResponseEntity<ErrorDTO>
    */
   @ExceptionHandler(CustomException.class)
   public ResponseEntity<ErrorDTO> handleCustomException(CustomException ex) {

      ErrorDTO error = new ErrorDTO(
              ex.getErrorDictionary().getCodigo(),
              ex.getErrorDictionary().getDescricao()
      );

      return ResponseEntity.badRequest().body(error);
   }

   /**
    * Tratar exceções genéricas. Monta um DTO com código genérico e descrição e devolve com status 500.
    * @param ex Exception
    * @return ResponseEntity<ErrorDTO>
    */
   @ExceptionHandler(Exception.class)
   public ResponseEntity<ErrorDTO> handleException(Exception ex) {

      // Erro genérico
      ErrorDTO error = new ErrorDTO(
              "ERRO-500",
              "Ocorreu um erro inesperado"
      );

      return ResponseEntity.internalServerError().body(error);
   }

}
