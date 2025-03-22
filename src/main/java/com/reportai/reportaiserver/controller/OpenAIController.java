package com.reportai.reportaiserver.controller;

import com.reportai.reportaiserver.dto.OpenAIResponseCorrecaoDTO;
import com.reportai.reportaiserver.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ia")
@RequiredArgsConstructor
public class OpenAIController {

   @Autowired
   private OpenAIService service;

   @PostMapping("/correcao")
   public ResponseEntity<OpenAIResponseCorrecaoDTO> validarTexto(@RequestBody String texto) {
      // #ToDo #SpringSecurity
      OpenAIResponseCorrecaoDTO responseDTO = service.getChatCompletion(texto);
      return ResponseEntity.ok(responseDTO);
   }


}
