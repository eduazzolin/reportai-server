package com.reportai.reportaiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportai.reportaiserver.dto.OpenAIResponseCorrecaoDTO;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

   private static final String API_URL = "https://api.openai.com/v1/chat/completions";

   @Value("${OPENAI_API_KEY}")
   private String apiKey;

   @Value("classpath:openai-prompt1-correcao-textual.md")
   private Resource prompt1Resource;

   private final RestTemplate restTemplate;

   @Autowired
   public OpenAIService(RestTemplate restTemplate) {
      this.restTemplate = restTemplate;
   }

   /**
    * Lê o prompt do sistema que é um arquivo markdown.
    *
    * @return O conteúdo do prompt do sistema String
    * @throws IOException Se ocorrer um erro ao ler o arquivo
    */
   public String buscarPromptSistema() throws IOException {
      return new String(Files.readAllBytes(Paths.get(prompt1Resource.getURI())));
   }

   /**
    * Executa o prompt de correção de texto do usuário.
    * Para mais informações do prompt e do retorno, consulte o prompt em src/main/resources/openai-prompt1-correcao-textual.md
    *
    * @param userMessage Texto a ser corrigido
    * @return Resposta da IA com o texto corrigido em formato JSON
    */
   public OpenAIResponseCorrecaoDTO executarPromptUsuario(String userMessage) {
      try {
         // Configurando os cabeçalhos
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.setBearerAuth(apiKey);

         // Corpo da requisição
         Map<String, Object> requestBody = new HashMap<>();
         requestBody.put("model", "gpt-4o-mini");
         requestBody.put("messages", List.of(
                 Map.of("role", "system", "content", buscarPromptSistema()),
                 Map.of("role", "user", "content", userMessage)
         ));

         requestBody.put("temperature", 0.0);


         // estrutura do response format 🙄
         Map<String, Object> responseFormat = new HashMap<>();
         responseFormat.put("type", "json_schema");

         // json_schema
         Map<String, Object> jsonSchema = new HashMap<>();
         jsonSchema.put("name", "text_validation");

         // schema
         Map<String, Object> schema = new HashMap<>();
         schema.put("type", "object");

         // properties
         Map<String, Object> properties = new HashMap<>();

         Map<String, Object> valido = new HashMap<>();
         valido.put("type", "boolean");
         valido.put("description", "Indicates whether the text is appropriate.");
         Map<String, Object> textoCorrigido = new HashMap<>();
         textoCorrigido.put("type", "string");
         textoCorrigido.put("description", "The corrected text if necessary, or an empty string if no correction is needed.");

         properties.put("valido", valido);
         properties.put("texto_corrigido", textoCorrigido);

         schema.put("properties", properties);
         schema.put("required", List.of("valido", "texto_corrigido"));
         schema.put("additionalProperties", false);


         jsonSchema.put("schema", schema);
         jsonSchema.put("strict", true);

         responseFormat.put("json_schema", jsonSchema);
         requestBody.put("response_format", responseFormat);


         HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);


         // Fazendo a requisição POST
         ResponseEntity<Map<String, Object>> response =
                 restTemplate.postForEntity(API_URL, requestEntity, (Class<Map<String, Object>>) (Class) Map.class);

         // Extraindo a resposta
         if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

            if (choices == null || choices.isEmpty()) {
               throw new CustomException(ErrorDictionary.ERRO_OPENAI);
            } else {
               Map<String, Object> firstChoice = choices.get(0);
               Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
               String content = (String) message.get("content");

               ObjectMapper objectMapper = new ObjectMapper();
               return objectMapper.readValue(content, OpenAIResponseCorrecaoDTO.class);
            }

         } else {
            throw new CustomException(ErrorDictionary.ERRO_OPENAI);
         }

      } catch (Exception e) {
         System.out.println(e.getMessage());
         throw new CustomException(ErrorDictionary.ERRO_OPENAI);
      }
   }
}
