package com.reportai.reportaiserver.seeder;

import com.reportai.reportaiserver.model.*;
import com.reportai.reportaiserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.reportai.reportaiserver.model.Interacao.TipoInteracao.*;
import static com.reportai.reportaiserver.model.Usuario.Roles.ADMIN;
import static com.reportai.reportaiserver.model.Usuario.Roles.USUARIO;

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
      createProcedureAdminListarUsuarios();
      createProcedureAdminListarRegistros();
      createProcedureConclusaoAutomatica();
      createProceduresRelatorios();
   }

   private void callProcedure(String procedureName) {
      jdbcTemplate.execute("CALL " + procedureName + "();");
   }

   private void createProcedureConclusaoAutomatica() {

      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_INCLUIR_RESOLUCAO_AUTOMATICA");
      jdbcTemplate.execute("""
              CREATE PROCEDURE SP_INCLUIR_RESOLUCAO_AUTOMATICA(IN p_id_registro INT)
              BEGIN
                  /*
                   * insere os registros na tabela de conclusão programada se:
                   * A quantidade de interações for múltiplo de 5;
                   * O registro não existir na tabela de conclusão programada ou, caso exista,
                   * ele não deve estar com a data de remoção aberta e a data de remoção deve ser anterior
                   * à data da última interação.
                   */
              
                  DROP TEMPORARY TABLE IF EXISTS REGISTRO_DETALHE;
                  CREATE TEMPORARY TABLE REGISTRO_DETALHE AS
                  SELECT R.ID                                                     AS id_registro,
                         R.usuario_id                                             AS id_usuario,
                         MAX(I.dt_criacao)                                        AS dt_ultima_interacao,
                         COUNT(I.ID)                                              AS qt_interacao,
                         CASE WHEN MAX(C.id) IS NOT NULL THEN TRUE ELSE FALSE END AS fl_possui_agendamento,
                         MAX(C.removida_em)                                       AS dt_ultimo_removido_em
                  FROM registro R
                           JOIN interacao I ON I.id_registro = R.ID AND I.TIPO = 'CONCLUIDO' AND NOT I.is_deleted
                           LEFT JOIN conclusao_programada C ON C.id_registro = R.id
                  WHERE NOT R.is_concluido
                    AND NOT R.is_deleted
                    AND R.ID = p_id_registro
                  GROUP BY R.ID, R.usuario_id;
              
                  INSERT INTO conclusao_programada (DT_CRIACAO, ID_REGISTRO, ID_USUARIO, REMOVIDA_EM, CONCLUSAO_PROGRAMADA_PARA)
                  SELECT CURRENT_TIMESTAMP                              AS dt_criacao,
                         id_registro                                    AS id_registro,
                         id_usuario                                     AS id_usuario,
                         NULL                                           AS removida_em,
                         DATE_ADD(dt_ultima_interacao, INTERVAL 30 DAY) AS conclusao_programada_para
                  FROM REGISTRO_DETALHE r
                  WHERE qt_interacao % 5 = 0
                    AND (NOT fl_possui_agendamento OR dt_ultima_interacao > dt_ultimo_removido_em);
              
              END;
              """);

      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_CONCLUSAO_AUTOMATICA");
      jdbcTemplate.execute("""
              CREATE PROCEDURE SP_CONCLUSAO_AUTOMATICA()
              BEGIN
              
                  /*
                   * conclui os registros programados para remoção
                   * e sinaliza na tabela de conclusão programada que foram removidos
                   */
                  UPDATE registro
                  SET is_concluido = TRUE,
                      dt_conclusao = CURRENT_TIMESTAMP
                  WHERE id IN
                        (SELECT ID_REGISTRO FROM conclusao_programada WHERE conclusao_programada_para < CURRENT_TIMESTAMP AND removida_em IS NULL);
              
                  UPDATE conclusao_programada
                  SET removida_em = CURRENT_TIMESTAMP
                  WHERE conclusao_programada_para < CURRENT_TIMESTAMP
                    AND removida_em IS NULL;
              
              
              END;
              """);

      jdbcTemplate.execute("SET GLOBAL event_scheduler = ON;");
      jdbcTemplate.execute("""
                       CREATE EVENT IF NOT EXISTS EV_CONCLUSAO_AUTOMATICA
                       ON SCHEDULE EVERY 1 DAY
                       STARTS TIMESTAMP(CURRENT_DATE, '03:00:00')
                       DO
                       CALL SP_CONCLUSAO_AUTOMATICA();
              """);
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
                                          * esta CTE desconsidera registros que estão fora do raio `p_distancia`, aplica o filtro de `p_filtro`
                                          * e ainda limita a quantidade de registros baseado em `p_limite` por ordem de distância.
                                          * a distância é calculada através da formula da distância euclidiana.
                                          */
                                         SET @REGISTROS_LIMITADOS_POR_DISTANCIA = CONCAT(
                                                 ', REGISTROS_LIMITADOS_POR_DISTANCIA AS  (SELECT *, ',
                                                 '                                               (SQRT(POW(latitude - ', p_lat, ', 2) + POW(longitude - ', p_long, ', 2))) * 100 AS distancia_do_centro',
                                                 '                                           FROM registro ',
                                                 '                                           WHERE NOT is_deleted ',
                                                 '                                                AND (SQRT(POW(latitude - ', p_lat, ', 2) + POW(longitude - ', p_long, ', 2))) * 100 <= ', p_distancia,
                                                 '                                                ', p_filtro,
                                                 '                                           ORDER BY distancia_do_centro ASC',
                                                 '                                           LIMIT ', p_limite, ')');
              
              
                                        /*
                                         * esta CTE calcula as interações do tipo RELEVANTE por registro, serve para ordenar depois
                                         */
                                         SET @INTERACOES_RELEVANTES = ' WITH INTERACOES_RELEVANTES AS (SELECT ID_REGISTRO, COUNT(ID) AS interacoesRelevante FROM interacao WHERE TIPO = ''RELEVANTE'' GROUP BY ID_REGISTRO ) ';
              
              
                                         /*
                                          * a query principal serve para ordenar os registros filtrados
                                          */
                                         SET @MAIN_QUERY = CONCAT(
                                                 @INTERACOES_RELEVANTES,
                                                 @REGISTROS_LIMITADOS_POR_DISTANCIA,
                                                 ' SELECT R.*, I.interacoesRelevante FROM REGISTROS_LIMITADOS_POR_DISTANCIA R',
                                                 ' LEFT JOIN INTERACOES_RELEVANTES I ON R.ID = I.ID_REGISTRO',
                                                 ' ORDER BY ', p_ordenacao, ';');
              
                                         PREPARE stmt FROM @MAIN_QUERY;
                                         EXECUTE stmt;
                                         DEALLOCATE PREPARE stmt;
                                     END
              
              """);
      ;
   }

   private void createProcedureAdminListarRegistros() {
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_ADMIN_LISTAR_REGISTROS;");
      jdbcTemplate.execute("""
               CREATE PROCEDURE SP_ADMIN_LISTAR_REGISTROS(IN p_id_nome VARCHAR(1000), IN p_id_usuario INT, IN p_id_categoria VARCHAR(1000), IN p_bairro VARCHAR(1000), IN p_status VARCHAR(1000), IN p_offset INT, IN p_limite INT, IN p_ordenacao VARCHAR(255))
                                         BEGIN
              
                                             /*
                                              * select base com os campos que serão retornados
                                              */
                                             SET @SELECT_BASE = CONCAT(
                                                     ' SELECT r.id                                                    AS id,                              ',
                                                     '        r.titulo                                                AS titulo,                          ',
                                                     '        r.usuario_id                                            AS usuarioId,                       ',
                                                     '        r.dt_criacao                                            AS dtCriacao,                       ',
                                                     '        r.dt_modificacao                                        AS dtModificacao,                   ',
                                                     '        r.dt_conclusao                                          AS dtConclusao,                     ',
                                                     '        c.nome                                                  AS categoria,                       ',
                                                     '        r.bairro                                                AS bairro,                          ',
                                                     '        cp.conclusao_programada_para                            AS dtAteConclusao,                  ',
                                                     '        SUM(CASE WHEN i.tipo = ''CONCLUIDO'' THEN 1 ELSE 0 END)   AS qtConcluido,                   ',
                                                     '        SUM(CASE WHEN i.tipo = ''RELEVANTE'' THEN 1 ELSE 0 END)   AS qtRelevante,                   ',
                                                     '        SUM(CASE WHEN i.tipo = ''IRRELEVANTE'' THEN 1 ELSE 0 END) AS qtIrrelevante                  ',
                                                     ' FROM registro r                                                                                    ',
                                                     '          LEFT JOIN categoria c ON r.categoria_id = c.id                                            ',
                                                     '          LEFT JOIN interacao i ON i.id_registro = r.id                                             ',
                                                     '          LEFT JOIN conclusao_programada cp ON r.id = cp.id_registro AND cp.removida_em IS NULL     ');
              
                                             /*
                                              * filtros simples
                                              */
                                             SET @FILTROS_BASE = CONCAT(
                                                     ' WHERE not r.is_deleted AND (r.id LIKE LOWER(''%', p_id_nome, '%'') ',
                                                     ' OR r.titulo LIKE LOWER(''%', p_id_nome, '%'') )',
                                                     ' AND (r.usuario_id = ', p_id_usuario, ' OR ', p_id_usuario, ' = 0) ',
                                                     ' AND (c.id = ', p_id_categoria, ' OR ', p_id_categoria, ' = 0) ',
                                                     ' AND (r.bairro LIKE LOWER(''%', p_bairro, '%'') OR '', p_bairro,'' = '''') ');
              
                                             /*
                                              * filtro de status que precisou ser separado por causa do tipo
                                              */
                                             SET @FILTRO_STATUS = CASE
                                                                      WHEN p_status <> '' THEN
                                                                          CASE
                                                                              WHEN p_status = 'ATIVO' THEN ' AND (r.dt_conclusao IS NULL) '
                                                                              WHEN p_status = 'CONCLUIDO' THEN ' AND (r.dt_conclusao IS NOT NULL) '
                                                                              END
                                                                      ELSE '' END;
              
              
                                             /*
                                              * ordenação e paginação
                                              */
                                             SET @MAIN_QUERY = CONCAT(
                                                     @SELECT_BASE,
                                                     @FILTROS_BASE,
                                                     @FILTRO_STATUS,
                                                     ' GROUP BY r.id, r.titulo, r.usuario_id, r.dt_criacao, r.dt_modificacao, r.dt_conclusao, c.nome, r.bairro, cp.conclusao_programada_para ',
                                                     ' ORDER BY ', p_ordenacao,
                                                     ' LIMIT ', p_limite, ' OFFSET ', p_offset);
              
              
                                             PREPARE stmt FROM @MAIN_QUERY;
                                             EXECUTE stmt;
                                             DEALLOCATE PREPARE stmt;
                                         END
              
              """);
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_ADMIN_LISTAR_REGISTROS_COUNT;");
      jdbcTemplate.execute("""
              CREATE PROCEDURE SP_ADMIN_LISTAR_REGISTROS_COUNT(IN p_id_nome VARCHAR(1000), IN p_id_usuario INT, IN p_id_categoria VARCHAR(1000), IN p_bairro VARCHAR(1000), IN p_status VARCHAR(1000))
              BEGIN
              
                  /*
                   * select base com os campos que serão retornados
                   */
                  SET @SELECT_BASE = CONCAT(
                          ' SELECT COUNT(*) FROM (',
                          ' SELECT r.id                                                      AS id,                ',
                          '        r.titulo                                                  AS titulo,            ',
                          '        r.usuario_id                                              AS usuarioId,         ',
                          '        r.dt_criacao                                              AS dtCriacao,         ',
                          '        r.dt_modificacao                                          AS dtModificacao,     ',
                          '        r.dt_conclusao                                            AS dtConclusao,       ',
                          '        c.nome                                                    AS categoria,         ',
                          '        r.bairro                                                  AS bairro,            ',
                          '        NULL                                                      AS dtAteConclusao,    ',
                          '        SUM(CASE WHEN i.tipo = ''CONCLUIDO'' THEN 1 ELSE 0 END)   AS qtConcluido,       ',
                          '        SUM(CASE WHEN i.tipo = ''RELEVANTE'' THEN 1 ELSE 0 END)   AS qtRelevante,       ',
                          '        SUM(CASE WHEN i.tipo = ''IRRELEVANTE'' THEN 1 ELSE 0 END) AS qtIrrelevante      ',
                          ' FROM registro r                                                                        ',
                          '          LEFT JOIN categoria c ON r.categoria_id = c.id                                ',
                          '          LEFT JOIN interacao i ON i.id_registro = r.id                                 ');
              
                  /*
                   * filtros simples
                   */
                  SET @FILTROS_BASE = CONCAT(
                          ' WHERE not r.is_deleted AND (r.id LIKE LOWER(''%', p_id_nome, '%'') ',
                          ' OR r.titulo LIKE LOWER(''%', p_id_nome, '%'') )',
                          ' AND (r.usuario_id = ', p_id_usuario, ' OR ', p_id_usuario, ' = 0) ',
                          ' AND (c.id = ', p_id_categoria, ' OR ', p_id_categoria, ' = 0) ',
                          ' AND (r.bairro LIKE LOWER(''%', p_bairro, '%'') OR '', p_bairro,'' = '''') ');
              
                  /*
                   * filtro de status que precisou ser separado por causa do tipo
                   */
                  SET @FILTRO_STATUS = CASE
                                           WHEN p_status <> '' THEN
                                               CASE
                                                   WHEN p_status = 'ATIVO' THEN ' AND (r.dt_conclusao IS NULL) '
                                                   WHEN p_status = 'CONCLUIDO' THEN ' AND (r.dt_conclusao IS NOT NULL) '
                                                   END
                                           ELSE '' END;
              
              
                  /*
                   * ordenação e paginação
                   */
                  SET @MAIN_QUERY = CONCAT(
                          @SELECT_BASE,
                          @FILTROS_BASE,
                          @FILTRO_STATUS,
                          ' GROUP BY r.id, r.titulo, r.usuario_id, r.dt_criacao, r.dt_modificacao, r.dt_conclusao, c.nome, r.bairro ',
                          ' ) e');
              
              
                  PREPARE stmt FROM @MAIN_QUERY;
                  EXECUTE stmt;
                  DEALLOCATE PREPARE stmt;
              END;
              
              """);

      ;
   }

   private void createProcedureAdminListarUsuarios() {

      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_ADMIN_LISTAR_USUARIOS;");
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_ADMIN_LISTAR_USUARIOS_COUNT;");
      jdbcTemplate.execute("""
              /*
               * listagem de usuários com paginação e pesquisa por termos
               */
              CREATE PROCEDURE SP_ADMIN_LISTAR_USUARIOS(
                       IN p_termo VARCHAR(1000),
                       IN p_offset INT,
                       IN p_limite INT,
                       IN p_ordenacao VARCHAR(255)
                   )
                   BEGIN
                       SET @sql = CONCAT('
                           SELECT u.id,
                                  u.nome,
                                  u.cpf,
                                  u.dt_criacao AS dtCriacao,
                                  u.dt_modificacao AS dtModificacao,
                                  u.email AS email,
                                  u.is_deleted AS isDeleted,
                                  u.role,
                                  COUNT(r.ID) AS totalRegistros
                           FROM usuario u
                           LEFT JOIN registro r ON r.usuario_id = u.ID
                           WHERE NOT u.is_deleted
                             AND (
                               LOWER(u.nome) LIKE LOWER(CONCAT("%","', p_termo, '", "%")) OR
                               LOWER(u.cpf) LIKE LOWER(CONCAT("%","', p_termo, '", "%")) OR
                               LOWER(u.email) LIKE LOWER(CONCAT("%","', p_termo, '", "%")) OR
                               u.ID LIKE CONCAT("%","', p_termo, '", "%")
                             )
                           GROUP BY u.id, u.cpf, u.dt_criacao, u.dt_modificacao, u.email, u.is_deleted, u.nome, u.role
                           ORDER BY ', p_ordenacao, '
                           LIMIT ', p_limite, ' OFFSET ', p_offset);
              
                       PREPARE stmt FROM @sql;
                       EXECUTE stmt;
                       DEALLOCATE PREPARE stmt;
                   END;
              """);
      jdbcTemplate.execute("""
              CREATE PROCEDURE SP_ADMIN_LISTAR_USUARIOS_COUNT(
                  IN p_termo VARCHAR(1000)
              )
              BEGIN
                  SELECT COUNT(*)
                  FROM usuario
                  WHERE NOT is_deleted
                    AND (
                      LOWER(nome) LIKE LOWER(CONCAT('%', p_termo, '%')) OR
                      LOWER(cpf) LIKE LOWER(CONCAT('%', p_termo, '%')) OR
                      LOWER(email) LIKE LOWER(CONCAT('%', p_termo, '%')) OR
                      ID LIKE CONCAT('%', p_termo, '%')
                      );
              END;
              """);
   }

   private void createProceduresRelatorios() {
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_RELATORIO_BAIRRO");
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_RELATORIO_CATEGORIA");
      jdbcTemplate.execute("DROP PROCEDURE IF EXISTS SP_RELATORIO_STATUS");
      jdbcTemplate.execute("""
              CREATE PROCEDURE SP_RELATORIO_BAIRRO(IN p_data_inicio DATETIME, IN p_data_fim DATETIME)
              BEGIN
                  SELECT 
                      bairro,
                      SUM(CASE WHEN is_concluido THEN 1 ELSE 0 END) AS CONCLUIDO,
                      SUM(CASE WHEN is_concluido THEN 0 ELSE 1 END) AS ATIVO,
                      COUNT(*) AS QUANTIDADE
                  FROM registro
                  WHERE NOT IS_DELETED
                    AND CAST(DT_CRIACAO AS DATE) BETWEEN p_data_inicio AND p_data_fim
                  GROUP BY BAIRRO
                  ORDER BY QUANTIDADE DESC;
              END;
              """);
      jdbcTemplate.execute("""
              CREATE PROCEDURE SP_RELATORIO_CATEGORIA(IN p_data_inicio DATETIME, IN p_data_fim DATETIME)
              BEGIN
                  SELECT 
                      C.NOME AS CATEGORIA,
                      SUM(CASE WHEN IS_CONCLUIDO THEN 1 ELSE 0 END) AS CONCLUIDO,
                      SUM(CASE WHEN IS_CONCLUIDO THEN 0 ELSE 1 END) AS ATIVO,
                      COUNT(*) AS QUANTIDADE
                  FROM registro R
                           LEFT JOIN CATEGORIA C ON C.ID = R.categoria_id
                  WHERE NOT R.IS_DELETED
                    AND  CAST(R.DT_CRIACAO AS DATE) BETWEEN p_data_inicio AND p_data_fim
                  GROUP BY C.NOME
                  ORDER BY QUANTIDADE DESC;
              END;
              """);
      jdbcTemplate.execute("""
              CREATE PROCEDURE SP_RELATORIO_STATUS(IN p_data_inicio DATETIME, IN p_data_fim DATETIME)
              BEGIN
                  SELECT CASE WHEN IS_CONCLUIDO THEN 'Resolvido' ELSE 'Aberto' END AS STATUS, COUNT(*) QUANTIDADE
                  FROM registro
                  WHERE NOT IS_DELETED
                    AND CAST(DT_CRIACAO AS DATE) BETWEEN p_data_inicio AND p_data_fim
                  GROUP BY IS_CONCLUIDO
                  ORDER BY QUANTIDADE DESC;
              END;
              """);
   }

   private void loadCategoria() {
      if (categoriaRepository.count() == 0) {
         categoriaRepository.save(Categoria.builder().nome("Buraco na rua").icone("/markers/street.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Calçada danificada").icone("/markers/path.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Lâmpada queimada").icone("/markers/light.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Árvore no caminho").icone("/markers/tree.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Sinalização de trânsito").icone("/markers/traffic-sign.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Vazamento de água").icone("/markers/leak.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Alagamento").icone("/markers/flood.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Lixo acumulado").icone("/markers/trash.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Problema de segurança").icone("/markers/safe.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Problema de esgoto").icone("/markers/sewage.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Engarrafamento constante").icone("/markers/traffic-jam.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Poluição sonora").icone("/markers/noise.svg").isDeleted(false).build());
         categoriaRepository.save(Categoria.builder().nome("Outros").icone("/markers/general.svg").isDeleted(false).build());
         ;
      }
   }

   private void loadUsuario() {
      if (usuarioRepository.count() == 0) {
         usuarioRepository.save(Usuario.builder().role(ADMIN).nome("UsuarioADM").email("adm@reportai.com").senha("15dd7a705552261b26df5b5e32190f8f").cpf("160.410.930-09").isDeleted(false).build());
         usuarioRepository.save(Usuario.builder().role(USUARIO).nome("Eduardo Azzolin").email("user@reportai.com").senha("f71b02bc59d0f555ec6bbca49ffdc041").cpf("839.925.170-47").isDeleted(false).build());
         usuarioRepository.save(Usuario.builder().role(USUARIO).nome("Maria Marques").email("maria@marques.com").senha("123456").cpf("532.162.220-55").isDeleted(false).build());
         usuarioRepository.save(Usuario.builder().role(USUARIO).nome("Joaquim Silva").email("joaquim@silva.com").senha("123456").cpf("540.924.870-88").isDeleted(false).build());
         usuarioRepository.save(Usuario.builder().role(USUARIO).nome("Márcio Mendes").email("marcio@mendes.com").senha("123456").cpf("745.704.400-02").isDeleted(false).build());
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
                 .bairro("Trindade")
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
                 .bairro("Agronômica")
                 .categoria(categoriaRepository.findById(3L).get())
                 .isConcluido(true)
                 .dtConclusao(java.time.LocalDateTime.now())
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(2L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Calçada danificada")
                 .descricao("A calçada em frente ao prédio onde moro está danificada há meses. Os buracos e desníveis no passeio representam um risco para os pedestres que circulam pela região, especialmente para idosos, crianças e pessoas com mobilidade reduzida. A falta de manutenção da calçada compromete a segurança e a acessibilidade de todos que utilizam a via, tornando-a um ambiente hostil e perigoso.\n\nOs moradores do prédio têm relatado dificuldades para transitar pela calçada danificada, devido aos obstáculos e irregularidades no piso. Além disso, a situação se agrava nos dias de chuva, quando os buracos se enchem de água e dificultam ainda mais a passagem dos pedestres. A calçada danificada também compromete a estética da região, tornando-a menos atraente e convidativa para os moradores e visitantes.\n\nÉ urgente que a calçada seja reparada o mais rápido possível. Os moradores do prédio pedem que as autoridades competentes realizem os reparos necessários para garantir a segurança e a acessibilidade de todos que circulam pela região. Uma calçada bem conservada é essencial para promover a mobilidade urbana e o bem-estar da comunidade local.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A manutenção da calçada danificada é uma medida simples, mas que tem um impacto significativo na qualidade de vida dos moradores e na segurança da região.")
                 .localizacao("Rua Presidente Coutinho 360")
                 .bairro("Centro")
                 .latitude(-27.590744088315592)
                 .longitude(-48.55018902283705)
                 .categoria(categoriaRepository.findById(2L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(4L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Poda de árvore")
                 .descricao("A árvore em frente à minha casa está com os galhos muito grandes e invadindo a fiação elétrica. Além disso, os galhos estão muito próximos da janela do meu quarto, o que representa um risco para a segurança da minha família. A árvore precisa de uma poda urgente para evitar acidentes e danos à propriedade.\n\nOs galhos da árvore estão encostando nos fios de eletricidade, o que pode causar curtos-circuitos e interrupções no fornecimento de energia. Além disso, a proximidade dos galhos com a janela do meu quarto aumenta o risco de acidentes, como a queda de galhos durante tempestades ou ventanias. A situação se agravou nos últimos dias, com o crescimento descontrolado da árvore e o aumento do risco de danos à propriedade e à segurança da minha família.\n\nÉ urgente que a árvore seja podada o mais rápido possível. Os moradores da rua estão preocupados com a situação e pedem que as autoridades competentes realizem os reparos necessários para garantir a segurança e o bem-estar de todos. Uma poda adequada é essencial para evitar acidentes e danos à propriedade, além de manter a saúde da árvore e a harmonia da paisagem urbana.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A poda da árvore é uma medida simples, mas que tem um impacto significativo na segurança e na qualidade de vida dos moradores da região.")
                 .localizacao("Rua da Capela, Campeche")
                 .bairro("Campeche")
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
                 .bairro("Centro")
                 .latitude(-27.595356626042015)
                 .longitude(-48.55304630289941)
                 .categoria(categoriaRepository.findById(6L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(5L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Sinalização de trânsito")
                 .descricao("A sinalização de trânsito na rua onde moro está danificada e desatualizada há meses. As placas de trânsito estão desgastadas, sujas e mal posicionadas, comprometendo a segurança e a fluidez do tráfego na região. A falta de sinalização adequada torna a via um ambiente propício para acidentes e infrações, colocando em risco a vida dos motoristas, ciclistas e pedestres que circulam pelo local.\n\nA sinalização de trânsito desatualizada confunde os condutores e dificulta a interpretação das normas de trânsito, aumentando o risco de acidentes e infrações. A ausência de placas de sinalização e a má conservação das existentes comprometem a segurança viária e a organização do tráfego, tornando a rua um local caótico e perigoso. A situação se agravou nos últimos dias, com o aumento do fluxo de veículos e a falta de orientação adequada para os condutores.\n\nÉ urgente que a sinalização de trânsito seja revitalizada o mais rápido possível. Os moradores da rua estão preocupados com a segurança viária e pedem que as autoridades competentes realizem os reparos necessários para restabelecer a ordem e a segurança no tráfego local. Uma sinalização eficiente é essencial para garantir a segurança e a fluidez do trânsito, além de promover a convivência harmoniosa entre os diferentes modais de transporte.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A revitalização da sinalização de trânsito é uma medida simples, mas que tem um impacto significativo na segurança viária e na qualidade de vida dos moradores da região.")
                 .localizacao("Canasvieiras, em frente ao Supermercado Magia")
                 .bairro("Canasvieiras")
                 .latitude(-27.429936753773585)
                 .longitude(-48.45811156129758)
                 .categoria(categoriaRepository.findById(5L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(2L).get())
                 .build());


         registroRepository.save(Registro.builder()
                 .titulo("Barulho excessivo")
                 .descricao("O barulho excessivo na rua onde moro está prejudicando a qualidade de vida dos moradores. O som alto e constante perturba o sossego e o descanso das pessoas, causando estresse, irritação e problemas de saúde. O barulho excessivo é uma forma de poluição sonora que afeta a saúde física e mental dos moradores, interferindo no sono, na concentração e no bem-estar geral.\n\nO barulho é causado por veículos com escapamentos modificados, festas em residências e estabelecimentos comerciais, obras em horários inadequados e outros eventos que desrespeitam os limites de ruído estabelecidos pela legislação. A exposição contínua ao barulho excessivo pode causar danos auditivos, distúrbios do sono, problemas de concentração e irritabilidade, afetando a qualidade de vida e o bem-estar das pessoas.\n\nÉ urgente que medidas sejam tomadas para controlar o barulho excessivo na rua. Os moradores estão sofrendo com os impactos negativos do ruído constante e pedem que as autoridades competentes tomem providências para garantir o respeito aos limites de ruído e à saúde da comunidade. O controle do barulho é essencial para promover um ambiente saudável e harmonioso, onde todos possam viver com tranquilidade e segurança.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. O controle do barulho excessivo é uma medida simples, mas que tem um impacto significativo na qualidade de vida dos moradores e na preservação do ambiente urbano.")
                 .localizacao("Rua dos Marimbaus, Jurerê")
                 .bairro("Jurerê Internacional")
                 .latitude(-27.441071927510585)
                 .longitude(-48.50369053721775)
                 .categoria(categoriaRepository.findById(12L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(3L).get())
                 .build());

         registroRepository.save(Registro.builder()
                 .titulo("Lixo acumulado")
                 .descricao("O lixo acumulado na rua onde moro está se tornando um problema sério para a comunidade. Os resíduos sólidos se acumulam nas calçadas, canteiros e terrenos baldios, causando mau cheiro, proliferação de insetos e roedores, e riscos à saúde pública. O acúmulo de lixo compromete a limpeza e a estética da região, além de representar um risco para o meio ambiente e a qualidade de vida dos moradores.\n\nO lixo acumulado é resultado da falta de coleta regular de resíduos sólidos, da má disposição dos resíduos pelos moradores e da ausência de conscientização ambiental na comunidade. Os resíduos sólidos se acumulam em locais inadequados, obstruindo as vias públicas, entupindo bueiros e causando transtornos para os moradores e transeuntes. A situação se agrava nos dias de chuva, quando o lixo se mistura à água da chuva e se espalha pela região, aumentando os riscos à saúde e ao meio ambiente.\n\nÉ urgente que o lixo acumulado seja recolhido e destinado corretamente. Os moradores da rua estão preocupados com a situação e pedem que as autoridades competentes realizem a limpeza e a coleta dos resíduos sólidos de forma regular e eficiente. A gestão adequada dos resíduos é essencial para promover a saúde pública, preservar o meio ambiente e garantir a qualidade de vida da comunidade local.\n\nA comunidade espera uma resposta rápida e eficaz para este problema. A limpeza e a coleta do lixo acumulado são medidas simples, mas que têm um impacto significativo na saúde pública e na preservação do meio ambiente.")
                 .localizacao("Rua dos Timbres em Jurerê")
                 .bairro("Jurerê Internacional")
                 .latitude(-27.441283946410383)
                 .longitude(-48.49486652631194)
                 .categoria(categoriaRepository.findById(8L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(2L).get())
                 .build());


         registroRepository.save(Registro.builder()
                 .titulo("Local perigoso para caminhar")
                 .descricao("A rua onde moro está se tornando um local extremamente perigoso para caminhar devido ao aumento de assaltos e outros crimes. A falta de iluminação pública adequada e o baixo movimento de pessoas tornam o ambiente propício para ações criminosas, colocando em risco a segurança de quem precisa transitar pela região.Muitos moradores relatam ter sido vítimas de roubos e furtos, principalmente no início da manhã e à noite. A sensação de insegurança é constante, e caminhar pela avenida tornou-se uma atividade arriscada, especialmente para idosos, crianças e pessoas que circulam sozinhas.\n\nA ausência de policiamento ostensivo e a presença de áreas mal iluminadas favorecem a atuação de criminosos, que aproveitam a vulnerabilidade dos pedestres. Isso tem gerado medo e restringido o direito de ir e vir dos moradores, prejudicando a qualidade de vida e a mobilidade na região.É urgente que medidas sejam tomadas para aumentar a segurança pública no local. Os moradores da região pedem patrulhamento mais frequente, instalação de câmeras de vigilância e melhorias na iluminação para coibir as ações criminosas e garantir a segurança de todos.\n\nA comunidade espera uma resposta rápida e eficaz das autoridades competentes. Um ambiente seguro é essencial para promover o bem-estar e a tranquilidade dos moradores e visitantes da região.")
                 .localizacao("Itacurubi - AV. Buriti")
                 .bairro("Itacurubi")
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
                 .bairro("Santa Mônica")
                 .latitude(-27.593814473798016)
                 .longitude(-48.50800504391546)
                 .categoria(categoriaRepository.findById(2L).get())
                 .isConcluido(false)
                 .isDeleted(false)
                 .usuario(usuarioRepository.findById(2L).get())
                 .build());
      }
   }

   private void loadImagem() {
      if (imagemRepository.count() == 0) {

         // placeholder padrao
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/566a6405-f41c-44f8-9180-95ae776beaf8").registro(registroRepository.findById(1L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/9bdd15b3-9340-4503-9d8e-fbf841f5628d").registro(registroRepository.findById(1L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/f4bf4716-cfeb-4264-a7a8-183814085bac").registro(registroRepository.findById(1L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/14acf4e4-1c78-4614-819e-49e9618d5faa").registro(registroRepository.findById(2L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/eee24435-7050-40b8-89d8-16646120f11c").registro(registroRepository.findById(2L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/31732c87-6c2e-4118-a8b2-9884f6bafbbb").registro(registroRepository.findById(3L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/ebe19c33-2d84-402e-ae14-b8c85fd38384").registro(registroRepository.findById(4L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/5801c9d1-15bb-4465-9c58-8a655015e7db").registro(registroRepository.findById(5L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/bfb31e36-b073-40fa-aa05-6576525193a4").registro(registroRepository.findById(5L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/9e2e9980-91ad-49f7-a5c7-fec4d6675e25").registro(registroRepository.findById(6L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/c679dad0-4b87-426c-93bd-d992bd82a256").registro(registroRepository.findById(6L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/547d9e77-a932-45a7-b536-8d8e3608c6b6").registro(registroRepository.findById(7L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/c93cbbb3-ab2e-4026-927f-177d32cd5d02").registro(registroRepository.findById(8L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/ec7ad7ea-f994-4d60-adc3-a10e5c589743").registro(registroRepository.findById(8L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/7121413e-0148-4dde-b317-eff918aaf8f4").registro(registroRepository.findById(8L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/fd2b6c07-ce31-4f9b-97ca-ed0c989960f7").registro(registroRepository.findById(9L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/f5103477-fdaf-4fa3-b3c8-9fa6aba9acdf").registro(registroRepository.findById(10L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/88c9351a-52d2-49ab-82c5-4ecbc41c2f72").registro(registroRepository.findById(10L).get()).build());
         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-padrao/17e70737-bff7-4249-9209-029dfffffdc6").registro(registroRepository.findById(10L).get()).build());

         // placeholder dibea
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/08952904-1f3a-44e3-9408-3c3c07f2d40b").registro(registroRepository.findById(1L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/1dcc4ff2-f4c4-45c3-ad06-507c22c39971").registro(registroRepository.findById(1L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/305bd8fe-dcea-444e-9fcd-425048474952").registro(registroRepository.findById(1L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/306b65d8-b40c-4e12-b5aa-87066976b69d").registro(registroRepository.findById(2L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/33960843-2599-45f9-90fe-c1abcd14111a").registro(registroRepository.findById(2L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/33f6ffa2-9f9c-4291-b7a7-99615f9eac04").registro(registroRepository.findById(3L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/ad40b688-f7e0-41b8-a67a-c016df5abfb5").registro(registroRepository.findById(4L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/af77bfcb-d957-42ba-b70f-93b9ed7cf8a6").registro(registroRepository.findById(4L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/378e8685-29dc-43fd-911e-326f51e401fb").registro(registroRepository.findById(4L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/4b02f949-cbba-45a0-9de4-72bd45e16a7e").registro(registroRepository.findById(5L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/4b4d9bc8-a337-4784-a618-1ba6e8329ca5").registro(registroRepository.findById(5L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/5194bdbd-bb82-4fa7-91ac-02515551007c").registro(registroRepository.findById(6L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/5b3a0969-833b-4ba2-880d-5d3b0bcba9d9").registro(registroRepository.findById(6L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/72f64e42-4ccd-4544-9d68-19c83f3a653a").registro(registroRepository.findById(7L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/d0a21b7e-b5fd-40eb-be1a-ad3d1c8a2cdb").registro(registroRepository.findById(7L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/bebf81de-7e13-4528-8287-462aeeb9345f").registro(registroRepository.findById(8L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/ca152612-b14b-47f8-a914-3f60321e97a8").registro(registroRepository.findById(8L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/ce462f9e-0175-426b-afa1-51220e763399").registro(registroRepository.findById(8L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/f8cc64b4-4de3-4339-acb2-056392e2659f").registro(registroRepository.findById(9L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/d2153759-a1f0-4134-a18a-062c99de8648").registro(registroRepository.findById(10L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/e827a889-5ca3-4e4b-9edd-fb8c84e44ac9").registro(registroRepository.findById(10L).get()).build());
//         imagemRepository.save(Imagem.builder().caminho("https://storage.googleapis.com/reportai/registros/placeholder-dibea/f8cc64b4-4de3-4339-acb2-056392e2659f").registro(registroRepository.findById(10L).get()).build());


      }

   }

   private void loadInteracoes() {
      if (interacaoRepository.count() == 0) {
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(3L).get()).usuario(usuarioRepository.findById(2L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(5L).get()).usuario(usuarioRepository.findById(2L).get()).tipo(IRRELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(7L).get()).usuario(usuarioRepository.findById(2L).get()).tipo(IRRELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(9L).get()).usuario(usuarioRepository.findById(2L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(1L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(3L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(3L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(CONCLUIDO).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(3L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(IRRELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(4L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(6L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(8L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(9L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(CONCLUIDO).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(2L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(4L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(4L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(CONCLUIDO).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(5L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(7L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(9L).get()).usuario(usuarioRepository.findById(4L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(1L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(2L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(8L).get()).usuario(usuarioRepository.findById(5L).get()).tipo(RELEVANTE).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(8L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(CONCLUIDO).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(8L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(CONCLUIDO).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(8L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(CONCLUIDO).isDeleted(false).build());
         interacaoRepository.save(Interacao.builder().registro(registroRepository.findById(8L).get()).usuario(usuarioRepository.findById(3L).get()).tipo(CONCLUIDO).isDeleted(false).build());

      }

   }


}
