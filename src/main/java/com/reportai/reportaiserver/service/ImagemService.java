package com.reportai.reportaiserver.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.reportai.reportaiserver.exception.CustomException;
import com.reportai.reportaiserver.exception.ErrorDictionary;
import com.reportai.reportaiserver.model.Imagem;
import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.model.Usuario;
import com.reportai.reportaiserver.repository.ImagemRepository;
import com.reportai.reportaiserver.utils.Validacoes;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ImagemService {

   @Autowired
   private ImagemRepository repository;

   @Autowired
   private Validacoes validacoes;

   @Value("${gcs.bucket-name}")
   private String bucketName;

   @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
   private String googleApplicationCredentials;

   /**
    * Salva uma imagem no Google Cloud Storage e no banco de dados.
    *
    * @param file
    * @param idRegistro
    * @return imagem salva
    * @throws IOException
    */
   public Imagem salvar(MultipartFile file, Long idRegistro) throws IOException {

      validacoes.validarImagem(file);

      // #ToDo #SpringSecurity #Validar se o registro pertence ao usuário logado
      Usuario usuario = new Usuario();
      usuario.setId(2L);
      validacoes.validarRegistroPertenceUsuario(usuario, idRegistro);


      String url = uploadParaGCS(file, idRegistro);
      Imagem imagem = new Imagem();
      imagem.setCaminho(url);
      imagem.setRegistro(Registro.builder().id(idRegistro).build());

      return repository.save(imagem);
   }

   /**
    * Busca uma imagem por ID.
    *
    * @param id ID da imagem a ser buscada
    * @return imagem encontrada
    */
   public Imagem buscarPorId(Long id) {
      return repository.findById(id).orElse(null);
   }

   /**
    * Busca todas as imagens.
    *
    * @return lista de imagens
    */
   public List<Imagem> buscarTodos() {
      return repository.findAll();
   }

   /**
    * Remove uma imagem do Google Cloud Storage e do banco de dados.
    *
    * @param imagem
    */
   public void remover(Imagem imagem) {

      try {
         removerDoGCS(imagem.getCaminho());
      } catch (Exception e) {
         throw new CustomException(ErrorDictionary.ERRO_GCS);
      }

      repository.deleteById(imagem.getId());
   }

   /**
    * Faz o upload de uma imagem para o Google Cloud Storage.
    * A localização da imagem é definida pelo ID do registro. registros/id_registro=1/uuid
    *
    * @param file
    * @param idRegistro
    * @return url da imagem no GCS
    * @throws IOException
    */
   public String uploadParaGCS(MultipartFile file, Long idRegistro) throws IOException {

      // #ToDo Poderia ser um serice do Google
      Storage storage = StorageOptions
              .newBuilder()
              .setCredentials(GoogleCredentials.fromStream(new FileInputStream(googleApplicationCredentials)))
              .setProjectId("reportai-453222").build().getService();

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Thumbnails.of(file.getInputStream())
              .scale(1) // Mantém o tamanho original
              .outputQuality(0.5) // Reduz a qualidade para 50%
              .outputFormat("jpg")
              .toOutputStream(outputStream);

      byte[] compressedImage = outputStream.toByteArray();

      String fileName = "registros/id_registro=" + idRegistro + "/" + UUID.randomUUID().toString();
      BlobId blobId = BlobId.of(bucketName, fileName);
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
              .setContentType(file.getContentType())
              .build();

      storage.create(blobInfo, compressedImage);
      return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
   }

   /**
    * Remove uma imagem do Google Cloud Storage.
    *
    * @param url URL da imagem a ser removida
    * @throws IOException
    */
   public void removerDoGCS(String url) throws IOException {
      String fileName = url.substring(url.indexOf("registros/"));
      Storage storage = StorageOptions
              .newBuilder()
              .setCredentials(GoogleCredentials.fromStream(new FileInputStream(googleApplicationCredentials)))
              .setProjectId("reportai-453222").build().getService();
      BlobId blobId = BlobId.of(bucketName, fileName);
      storage.delete(blobId);
   }

}
