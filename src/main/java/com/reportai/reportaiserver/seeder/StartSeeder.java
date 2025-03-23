package com.reportai.reportaiserver.seeder;

import com.reportai.reportaiserver.model.*;
import com.reportai.reportaiserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import static com.reportai.reportaiserver.model.Interacao.TipoInteracao.CONCLUIDO;
import static com.reportai.reportaiserver.model.Interacao.TipoInteracao.RELEVANTE;

@Component
public class StartSeeder implements CommandLineRunner {

   @Autowired
   CategoriaRepository categoriaRepository;

   @Autowired
   UsuarioRepository usuarioRepository;

   @Autowired
   RegistroRepository registroRepository;

   @Autowired
   ImagemRepository imagemRepository;

   @Autowired
   InteracaoRepository interacaoRepository;

   @Autowired
   private JdbcTemplate jdbcTemplate;

   @Override
   public void run(String... args) throws Exception {
      loadCategoria();
      loadUsuario();
      loadRegistro();
      createProcedureRegistroPorDistancia();
      loadImagem();
      loadInteracoes();
   }

   private void createProcedureRegistroPorDistancia() {

      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_REGISTROS_POR_DISTANCIA;");
      jdbcTemplate.execute("""
                                      /*
                                       * ficou assim:
                                       *
                                       * primeiro filtro: o parametro `p_distancia` define até que distância das coordenadas `p_lat` e `p_long` deve ser retornado os registros.
                                       *      - estes parâmetros vêm do mapa, as coordenadas são o centro atual do mapa e a distância é baseada no zoom
                                       *      - tudo isso para que não sejam retornados registros que nem cabem na área que o usuário focou no mapa.
                                       * segundo filtro: o parâmetro `p_filtro` é aplicado na cláusura where para ser possível filtros personalizados, como de status, por exemplo.
                                       * terceiro filtro: o parametro `p_limite` define a quantidade de registros retornados após o primeiro filtro ordenados por distância das coordenadas `p_lat` e `p_long`.
                                       *      - como não há paginação, é definido este teto de registros a serem retornados.
                                       *      - para 'paginar' basta o usuário navegar no mapa, para ir mudando as coordenadas `p_lat` e `p_long` e consequentemente, a distância dos registros.
                                       */
              
                                      CREATE PROCEDURE SP_REGISTROS_POR_DISTANCIA(
                                         IN p_lat DOUBLE,
                                         IN p_long DOUBLE,
                                         IN p_distancia DOUBLE,
                                         IN p_limite INT,
                                         IN p_filtro VARCHAR(1000),
                                         IN p_ordenacao VARCHAR(255)
                                     )
                                     BEGIN
                                         /*
                                          * esta CTE desconsidera registros que estão fora do raio `p_distancia`
                                          * aplica o filtro de `p_filtro`
                                          * e ainda limita a quantidade de registros baseado em `p_limite` por ordem de distância
                                          */
                                         SET @REGISTROS_LIMITADOS_POR_DISTANCIA = CONCAT(
                                                 'WITH REGISTROS_LIMITADOS_POR_DISTANCIA AS  (SELECT *, ',
                                                 '                                               (SQRT(POW(latitude - ', p_lat, ', 2) + POW(longitude - ', p_long, ', 2))) * 100 AS distancia_do_centro',
                                                 '                                           FROM REGISTRO ',
                                                 '                                           WHERE (SQRT(POW(latitude - ', p_lat, ', 2) + POW(longitude - ', p_long, ', 2))) * 100 <= ', p_distancia,
                                                 '                                                    AND ', p_filtro,
                                                 '                                           ORDER BY distancia_do_centro ASC',
                                                 '                                           LIMIT ', p_limite, ')');
              
              
                                         /*
                                          * a query principal serve para ordenar os registros filtrados
                                          */
                                         SET @MAIN_QUERY = CONCAT(
                                                 @REGISTROS_LIMITADOS_POR_DISTANCIA,
                                                 ' SELECT * FROM REGISTROS_LIMITADOS_POR_DISTANCIA ',
                                                 ' ORDER BY ', p_ordenacao, ';');
              
                                         PREPARE stmt FROM @MAIN_QUERY;
                                         EXECUTE stmt;
                                         DEALLOCATE PREPARE stmt;
                                     END
              
              """);
      ;
   }

   private void loadCategoria() {
      if (categoriaRepository.count() == 0) {
         categoriaRepository.save(Categoria.builder().nome("Buraco na rua").icone("/markers/street.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Lâmpada queimada").icone("/markers/traffic-light.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Calçada danificada").icone("/markers/path.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Poda de árvore").icone("/markers/tree.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Vazamento de água").icone("/markers/flood.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Sinalização de trânsito").icone("/markers/traffic-sign.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Poluição sonora").icone("/markers/traffic-sign.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Lixo acumulado").icone("/markers/traffic-sign.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Outros").icone("/markers/general.svg").isDeleted(false).build());
         ;
      }
   }

   private void loadUsuario() {
      if (usuarioRepository.count() == 0) {
         usuarioRepository.save(Usuario.builder().role("ADMIN").nome("User").email("Admin").senha("123456").cpf("111111").isDeleted(false).build());
         usuarioRepository.save(Usuario.builder().role("USER").nome("João Silva").email("joao@silva.com").senha("123456").cpf("222222").isDeleted(false).build());
         usuarioRepository.save(Usuario.builder().role("USER").nome("Maria Marques").email("maria@marques.com").senha("123456").cpf("333333").isDeleted(false).build());
         usuarioRepository.save(Usuario.builder().role("USER").nome("Joaquim Silva").email("joaquim@silva.com").senha("123456").cpf("444444").isDeleted(false).build());
         usuarioRepository.save(Usuario.builder().role("USER").nome("Márcio Mendes").email("marcio@mendes.com").senha("123456").cpf("555555").isDeleted(false).build());
      }
   }

   private void loadRegistro() {
      if (registroRepository.count() == 0) {
         registroRepository.save(Registro.builder()
                 .titulo("Buraco na rua")
                 .descricao("A rua onde moro está enfrentando um problema sério há uma semana. Um grande buraco se formou no meio da via, trazendo inúmeros transtornos para os moradores e motoristas que passam pelo local. Este buraco não só representa um obstáculo físico, mas também um risco significativo para a segurança de todos que trafegam pela área.\n\nNos últimos dias, a situação se agravou devido às fortes chuvas que atingiram a região. A água da chuva ampliou o tamanho do buraco e tornou a via ainda mais perigosa. O buraco está ficando mais profundo e largo, aumentando as chances de acidentes. Além disso, a água acumulada dentro do buraco pode causar danos adicionais à infraestrutura da rua e aos veículos que, inadvertidamente, passem por ali.\n\nÉ urgente que medidas sejam tomadas para resolver este problema. Os moradores da rua estão preocupados e apelam para as autoridades responsáveis para que realizem os reparos necessários o mais rápido possível. Uma solução imediata é essencial para garantir a segurança dos pedestres, ciclistas e motoristas, além de evitar que o problema se torne ainda mais grave.\n\nA presença deste buraco já está afetando a rotina diária de todos que vivem e transitam pela região. É imperativo que as autoridades municipais priorizem este reparo e implementem uma solução duradoura para evitar futuros transtornos. A comunidade local espera uma resposta rápida e eficaz para este problema que, embora pareça simples, tem grandes repercussões na vida cotidiana de muitas pessoas")
                 .localizacao("Lauro Linhares perto do Angeloni")
                 .latitude(-27.585152417018662)
                 .longitude(-48.52451187162637)
                 .categoria(categoriaRepository.findById(1L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(2L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Lâmpada queimada")
                 .descricao("A lâmpada do poste em frente à minha casa está queimada há mais de uma semana. A escuridão na rua tem causado insegurança aos moradores e transeuntes que passam pelo local durante a noite. A falta de iluminação pública torna a rua um ambiente propício para a prática de crimes e acidentes, colocando em risco a segurança de todos que circulam pela região.\n\nA escuridão também dificulta a visibilidade dos motoristas e pedestres, aumentando o risco de acidentes de trânsito. A falta de iluminação adequada compromete a segurança de todos que utilizam a via, tornando-a um local perigoso e vulnerável a ocorrências indesejadas.\n\nÉ urgente que a lâmpada seja trocada o mais rápido possível. Os moradores da rua estão preocupados com a situação e pedem que as autoridades competentes realizem os reparos necessários para restabelecer a iluminação pública. Uma rua bem iluminada é essencial para garantir a segurança e o bem-estar de todos que vivem e transitam pela região.\n\nA comunidade local espera uma resposta rápida e eficaz para este problema. A reposição da lâmpada queimada é uma medida simples, mas que tem um impacto significativo na qualidade de vida dos moradores e na segurança da região.")
                 .localizacao("Rua São Vicente de Paula, na Agronômica")
                 .latitude(-27.57841321989771)
                 .longitude(-48.538419398201704)
                 .categoria(categoriaRepository.findById(2L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(3L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Calçada danificada")
                 .descricao("A calçada em frente ao prédio onde moro está danificada há meses. Os buracos e desníveis no passeio representam um risco para os pedestres que circulam pela região, especialmente para idosos, crianças e pessoas com mobilidade reduzida. A falta de manutenção da calçada compromete a segurança e a acessibilidade de todos que utilizam a via, tornando-a um ambiente hostil e perigoso.\n\nOs moradores do prédio têm relatado dificuldades para transitar pela calçada danificada, devido aos obstáculos e irregularidades no piso. Além disso, a situação se agrava nos dias de chuva, quando os buracos se enchem de água e dificultam ainda mais a passagem dos pedestres. A calçada danificada também compromete a estética da região, tornando-a menos atraente e convidativa para os moradores e visitantes.\n\nÉ urgente que a calçada seja reparada o mais rápido possível. Os moradores do prédio pedem que as autoridades competentes realizem os reparos necessários para garantir a segurança e a acessibilidade de todos que circulam pela região. Uma calçada bem conservada é essencial para promover a mobilidade urbana e o bem-estar da comunidade local.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A manutenção da calçada danificada é uma medida simples, mas que tem um impacto significativo na qualidade de vida dos moradores e na segurança da região.")
                 .localizacao("Rua Presidente Coutinho 360")
                 .latitude(-27.590744088315592)
                 .longitude(-48.55018902283705)
                 .categoria(categoriaRepository.findById(3L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(4L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Poda de árvore")
                 .descricao("A árvore em frente à minha casa está com os galhos muito grandes e invadindo a fiação elétrica. Além disso, os galhos estão muito próximos da janela do meu quarto, o que representa um risco para a segurança da minha família. A árvore precisa de uma poda urgente para evitar acidentes e danos à propriedade.\n\nOs galhos da árvore estão encostando nos fios de eletricidade, o que pode causar curtos-circuitos e interrupções no fornecimento de energia. Além disso, a proximidade dos galhos com a janela do meu quarto aumenta o risco de acidentes, como a queda de galhos durante tempestades ou ventanias. A situação se agravou nos últimos dias, com o crescimento descontrolado da árvore e o aumento do risco de danos à propriedade e à segurança da minha família.\n\nÉ urgente que a árvore seja podada o mais rápido possível. Os moradores da rua estão preocupados com a situação e pedem que as autoridades competentes realizem os reparos necessários para garantir a segurança e o bem-estar de todos. Uma poda adequada é essencial para evitar acidentes e danos à propriedade, além de manter a saúde da árvore e a harmonia da paisagem urbana.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A poda da árvore é uma medida simples, mas que tem um impacto significativo na segurança e na qualidade de vida dos moradores da região.")
                 .localizacao("Rua da Capela, Campeche")
                 .latitude(-27.675695192639118)
                 .longitude(-48.486439740613974)
                 .categoria(categoriaRepository.findById(4L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(2L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Vazamento de água")
                 .descricao("Um vazamento de água está ocorrendo na rua onde moro há mais de uma semana. A água está jorrando de uma tubulação subterrânea, formando uma poça que se estende pela via e compromete a circulação de veículos e pedestres. O vazamento de água está causando desperdício de um recurso tão precioso e essencial para a vida, além de representar um risco para a segurança e a saúde da comunidade local.\n\nO vazamento de água está se agravando a cada dia, com o aumento da poça e o desperdício contínuo do recurso. A água acumulada na via pode causar acidentes e danos materiais, além de atrair insetos e roedores que representam um risco para a saúde pública. A situação é preocupante e exige uma intervenção imediata das autoridades responsáveis para conter o vazamento e reparar a tubulação danificada.\n\nÉ urgente que o vazamento de água seja reparado o mais rápido possível. Os moradores da rua estão preocupados com o desperdício do recurso hídrico e pedem que as autoridades competentes tomem as medidas necessárias para resolver o problema. Uma intervenção rápida e eficaz é essencial para evitar danos maiores e garantir o abastecimento de água para a comunidade local.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. O reparo do vazamento de água é uma medida simples, mas que tem um impacto significativo na preservação do meio ambiente e na qualidade de vida dos moradores da região.")
                 .localizacao("Rua Álvaro de Carvalho, perto do TICEN")
                 .latitude(-27.595356626042015)
                 .longitude(-48.55304630289941)
                 .categoria(categoriaRepository.findById(5L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(5L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Sinalização de trânsito")
                 .descricao("A sinalização de trânsito na rua onde moro está danificada e desatualizada há meses. As placas de trânsito estão desgastadas, sujas e mal posicionadas, comprometendo a segurança e a fluidez do tráfego na região. A falta de sinalização adequada torna a via um ambiente propício para acidentes e infrações, colocando em risco a vida dos motoristas, ciclistas e pedestres que circulam pelo local.\n\nA sinalização de trânsito desatualizada confunde os condutores e dificulta a interpretação das normas de trânsito, aumentando o risco de acidentes e infrações. A ausência de placas de sinalização e a má conservação das existentes comprometem a segurança viária e a organização do tráfego, tornando a rua um local caótico e perigoso. A situação se agravou nos últimos dias, com o aumento do fluxo de veículos e a falta de orientação adequada para os condutores.\n\nÉ urgente que a sinalização de trânsito seja revitalizada o mais rápido possível. Os moradores da rua estão preocupados com a segurança viária e pedem que as autoridades competentes realizem os reparos necessários para restabelecer a ordem e a segurança no tráfego local. Uma sinalização eficiente é essencial para garantir a segurança e a fluidez do trânsito, além de promover a convivência harmoniosa entre os diferentes modais de transporte.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A revitalização da sinalização de trânsito é uma medida simples, mas que tem um impacto significativo na segurança viária e na qualidade de vida dos moradores da região.")
                 .localizacao("Canasvieiras, em frente ao Supermercado Magia")
                 .latitude(-27.429936753773585)
                 .longitude(-48.45811156129758)
                 .categoria(categoriaRepository.findById(6L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(2L).get())
                 .build());


         registroRepository.save(Registro.builder()
                 .titulo("Barulho excessivo")
                 .descricao("O barulho excessivo na rua onde moro está prejudicando a qualidade de vida dos moradores. O som alto e constante perturba o sossego e o descanso das pessoas, causando estresse, irritação e problemas de saúde. O barulho excessivo é uma forma de poluição sonora que afeta a saúde física e mental dos moradores, interferindo no sono, na concentração e no bem-estar geral.\n\nO barulho é causado por veículos com escapamentos modificados, festas em residências e estabelecimentos comerciais, obras em horários inadequados e outros eventos que desrespeitam os limites de ruído estabelecidos pela legislação. A exposição contínua ao barulho excessivo pode causar danos auditivos, distúrbios do sono, problemas de concentração e irritabilidade, afetando a qualidade de vida e o bem-estar das pessoas.\n\nÉ urgente que medidas sejam tomadas para controlar o barulho excessivo na rua. Os moradores estão sofrendo com os impactos negativos do ruído constante e pedem que as autoridades competentes tomem providências para garantir o respeito aos limites de ruído e à saúde da comunidade. O controle do barulho é essencial para promover um ambiente saudável e harmonioso, onde todos possam viver com tranquilidade e segurança.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. O controle do barulho excessivo é uma medida simples, mas que tem um impacto significativo na qualidade de vida dos moradores e na preservação do ambiente urbano.")
                 .localizacao("Rua dos Marimbaus, Jurerê")
                 .latitude(-27.441071927510585)
                 .longitude(-48.50369053721775)
                 .categoria(categoriaRepository.findById(7L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(3L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Lixo acumulado")
                 .descricao("O lixo acumulado na rua onde moro está se tornando um problema sério para a comunidade. Os resíduos sólidos se acumulam nas calçadas, canteiros e terrenos baldios, causando mau cheiro, proliferação de insetos e roedores, e riscos à saúde pública. O acúmulo de lixo compromete a limpeza e a estética da região, além de representar um risco para o meio ambiente e a qualidade de vida dos moradores.\n\nO lixo acumulado é resultado da falta de coleta regular de resíduos sólidos, da má disposição dos resíduos pelos moradores e da ausência de conscientização ambiental na comunidade. Os resíduos sólidos se acumulam em locais inadequados, obstruindo as vias públicas, entupindo bueiros e causando transtornos para os moradores e transeuntes. A situação se agrava nos dias de chuva, quando o lixo se mistura à água da chuva e se espalha pela região, aumentando os riscos à saúde e ao meio ambiente.\n\nÉ urgente que o lixo acumulado seja recolhido e destinado corretamente. Os moradores da rua estão preocupados com a situação e pedem que as autoridades competentes realizem a limpeza e a coleta dos resíduos sólidos de forma regular e eficiente. A gestão adequada dos resíduos é essencial para promover a saúde pública, preservar o meio ambiente e garantir a qualidade de vida da comunidade local.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A limpeza e a coleta do lixo acumulado são medidas simples, mas que têm um impacto significativo na saúde pública e na preservação do meio ambiente.")
                 .localizacao("Rua dos Timbres em Jurerê")
                 .latitude(-27.441283946410383)
                 .longitude(-48.49486652631194)
                 .categoria(categoriaRepository.findById(8L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(4L).get())
                 .build());


         registroRepository.save(Registro.builder()
                 .titulo("Local perigoso para caminhar")
                 .descricao("A rua onde moro está se tornando um local perigoso para caminhar. A falta de calçadas adequadas, a presença de buracos e obstáculos no passeio, e a ausência de sinalização para pedestres comprometem a segurança e a acessibilidade dos pedestres que circulam pela região. A rua se tornou um ambiente hostil e inseguro para os pedestres, especialmente para idosos, crianças e pessoas com mobilidade reduzida.\n\nA falta de calçadas adequadas dificulta a circulação dos pedestres, obrigando-os a caminhar pela rua e se expor ao risco de acidentes. Os buracos e obstáculos no passeio representam um perigo para os pedestres, aumentando as chances de quedas e lesões. A ausência de sinalização para pedestres confunde os condutores e dificulta a travessia segura das vias, colocando em risco a vida e a integridade física dos pedestres.\n\nÉ urgente que medidas sejam tomadas para tornar a rua um local seguro para caminhar. Os moradores da região estão preocupados com a falta de infraestrutura adequada para os pedestres e pedem que as autoridades competentes realizem os reparos necessários para garantir a segurança e a acessibilidade de todos que circulam pela via. Uma rua segura e acessível é essencial para promover a mobilidade urbana e o bem-estar da comunidade local.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A melhoria da infraestrutura para pedestres é uma medida simples, mas que tem um impacto significativo na segurança viária e na qualidade de vida dos moradores da região.")
                 .localizacao("Itacurubi - AV. Buriti")
                 .latitude(-27.5916421583386)
                 .longitude(-48.49706333872131)
                 .categoria(categoriaRepository.findById(9L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(5L).get())
                 .build());


         registroRepository.save(Registro.builder()
                 .titulo("Buraco enorme na calçada")
                 .descricao("Na rua onde moro, há um buraco enorme na calçada que representa um risco para a segurança dos pedestres. O buraco se formou devido ao desgaste do piso e à falta de manutenção da calçada, tornando o local perigoso e intransitável. O buraco é profundo e largo, dificultando a passagem dos pedestres e aumentando as chances de acidentes e lesões.\n\nO buraco na calçada compromete a acessibilidade e a segurança dos pedestres, especialmente de idosos, crianças e pessoas com mobilidade reduzida. A falta de sinalização e proteção ao redor do buraco aumenta o risco de acidentes e quedas, colocando em perigo a vida e a integridade física dos transeuntes. A situação se agravou nos últimos dias, com o aumento do tráfego de pedestres e a falta de reparos na calçada.\n\nÉ urgente que o buraco na calçada seja reparado o mais rápido possível. Os moradores da rua estão preocupados com a situação e pedem que as autoridades competentes realizem os reparos necessários para garantir a segurança e a acessibilidade de todos que circulam pela via. Uma calçada bem conservada é essencial para promover a mobilidade urbana e o bem-estar da comunidade local.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. O reparo do buraco na calçada é uma medida simples, mas que tem um impacto significativo na segurança viária e na qualidade de vida dos moradores da região.")
                 .localizacao("Rua Coronel Maurício Spalding - Santa Monica")
                 .latitude(-27.593814473798016)
                 .longitude(-48.50800504391546)
                 .categoria(categoriaRepository.findById(1L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(2L).get())
                 .build());
      }
   }

   private void loadImagem() {
      if (imagemRepository.count() == 0) {
         imagemRepository.save(Imagem.builder().caminho("https://www.educolorir.com/imagem-numero-1-dl20182.jpg").registro(registroRepository.findById(1L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://www.educolorir.com/imagem-numero-1-dl20182.jpg").registro(registroRepository.findById(1L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://www.educolorir.com/imagem-numero-1-dl20182.jpg").registro(registroRepository.findById(1L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://img.freepik.com/fotos-gratis/numero-2-feito-de-flores-e-grama-isoladas-em-branco_169016-57072.jpg").registro(registroRepository.findById(2L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://img.freepik.com/fotos-gratis/numero-2-feito-de-flores-e-grama-isoladas-em-branco_169016-57072.jpg").registro(registroRepository.findById(2L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://img.freepik.com/fotos-gratis/numero-2-feito-de-flores-e-grama-isoladas-em-branco_169016-57072.jpg").registro(registroRepository.findById(2L).get()).build());

      }

   }

   private void loadInteracoes() {
      if (interacaoRepository.count() == 0) {
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(2L).get()).usuario(usuarioRepository.findById(2L).get()).tipo(CONCLUIDO).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(5L).get()).usuario(usuarioRepository.findById(2L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(9L).get()).usuario(usuarioRepository.findById(2L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(1L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(3L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(5L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(8L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(10L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(1L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(2L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(2L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(CONCLUIDO).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(4L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(5L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(6L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(7L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(10L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(1L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(CONCLUIDO).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(3L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(4L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(4L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(CONCLUIDO).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(6L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(CONCLUIDO).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(8L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(RELEVANTE).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(10L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(RELEVANTE).build());
      }

   }


}
