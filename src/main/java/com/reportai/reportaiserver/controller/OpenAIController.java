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

   /**
    * Executa o prompt de correção de texto do usuário.
    * Para mais informações do prompt e do retorno, consute o prompt em src/main/resources/openai-prompt1-correcao-textual.md
    *
    * @param texto Texto a ser corrigido
    * @return Resposta da IA com o texto corrigido em formato JSON
    */
   @PostMapping("/correcao")
   public ResponseEntity<OpenAIResponseCorrecaoDTO> validarTexto(@RequestBody String texto) {
      OpenAIResponseCorrecaoDTO responseDTO = service.executarPromptUsuario(texto);
      return ResponseEntity.ok(responseDTO);
   }


}
