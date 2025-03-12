package com.reportai.reportaiserver.utils;

public class RegistroUtils {

   private static final double CENTRO_LAT = -27.596674;
   private static final double CENTRO_LON = -48.5466487;

   public static double calcularDistanciaDoCentro(double lat, double lon) {
      double diferencaLat = CENTRO_LAT - lat;
      double diferencaLon = CENTRO_LON - lon;
      return Math.sqrt(Math.pow(diferencaLat, 2) + Math.pow(diferencaLon, 2)) * 100;
   }


}
