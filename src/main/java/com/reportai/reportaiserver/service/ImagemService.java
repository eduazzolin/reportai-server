package com.reportai.reportaiserver.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.reportai.reportaiserver.model.Imagem;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.ImagemRepository;
import com.reportai.reportaiserver.utils.Validacoes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ImagemService {

   @Autowired
   private ImagemRepository repository;

   @Value("${gcs.bucket-name}")
   private String bucketName;

   @Autowired
   private Validacoes validacoes;

   @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
   private String googleApplicationCredentials;

   public Imagem save(MultipartFile file, Long idRegistro) throws IOException {

      validacoes.validarImagem(file);

      // #ToDo #SpringSecurity #Validar se o registro pertence ao usuário logado
      Usuario usuario = new Usuario();
      usuario.setId(2L);
      validacoes.validarRegistroPertenceUsuario(usuario, idRegistro);


      String url = uploadToGCS(file, idRegistro);
      Imagem imagem = new Imagem();
      imagem.setCaminho(url);
      imagem.setRegistro(Registro.builder().id(idRegistro).build());

      return repository.save(imagem);
   }

   public Imagem findById(Long id) {
      return repository.findById(id).orElse(null);
   }

   public List<Imagem> findAll() {
      return repository.findAll();
   }

   public void deleteById(Long id) {
      repository.deleteById(id);
   }

   public String uploadToGCS(MultipartFile file, Long idRegistro) throws IOException {
      Storage storage = StorageOptions
              .newBuilder()
              .setCredentials(GoogleCredentials.fromStream(new FileInputStream(googleApplicationCredentials)))
              .setProjectId("reportai-453222").build().getService();


      String fileName = "r" + idRegistro + "_" + UUID.randomUUID().toString();
      BlobId blobId = BlobId.of(bucketName, fileName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
              .setContentType(file.getContentType())
              .build();

      storage.create(blobInfo, file.getBytes());
      return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
   }

}
