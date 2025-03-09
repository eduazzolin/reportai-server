package com.reportai.reportaiserver.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.reportai.reportaiserver.model.Imagem;
import com.reportai.reportaiserver.service.ImagemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/imagens")
@RequiredArgsConstructor
public class ImagemController {

   @Autowired
   private ImagemService service;

   @Value("${gcs.bucket-name}")
   private String bucketName;

   @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
   private String googleApplicationCredentials;

   @PostMapping
   public ResponseEntity<?> salvar(@RequestBody Imagem imagem) {
      try {
         Imagem imagemSalvo = service.save(imagem);
         return ResponseEntity.ok(imagemSalvo);
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @GetMapping
   public ResponseEntity<?> listar() {
      try {
         return ResponseEntity.ok(service.findAll());
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @GetMapping("/{id}")
   public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
      try {
         return ResponseEntity.ok(service.findById(id));
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @DeleteMapping("/{id}")
   public ResponseEntity<?> excluir(@PathVariable Long id) {
      try {
         service.deleteById(id);
         return ResponseEntity.ok().build();
      } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
      }
   }

   @PostMapping("/upload")
   public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

      Storage storage = StorageOptions
              .newBuilder()
              .setCredentials(GoogleCredentials.fromStream(new FileInputStream(googleApplicationCredentials)))
              .setProjectId("reportai-453222").build().getService();


      String fileName = UUID.randomUUID().toString();
      BlobId blobId = BlobId.of(bucketName, fileName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
              .setContentType(file.getContentType())
              .build();

      storage.create(blobInfo, file.getBytes());
      return "https://storage.googleapis.com/" + bucketName + "/" + fileName;

   }

}
