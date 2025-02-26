package com.reportai.reportaiserver.service;

import com.reportai.reportaiserver.model.Registro;
import com.reportai.reportaiserver.repository.RegistroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistroService {

   @Autowired
   private RegistroRepository repository;

   public Registro save(Registro registro) {
      return repository.save(registro);
   }

   public Registro findById(Long id) {
      return repository.findById(id).orElse(null);
   }

   public List<Registro> findByDistancia(double latitude, double longitude, double distancia, int paginacao, int pagina) {
      return repository.findByDistance(latitude, longitude, distancia, paginacao, pagina);
   }

   public List<Registro> findAll() {
      return repository.findAll();
   }

   public void deleteById(Long id) {
      repository.deleteById(id);
   }

}
