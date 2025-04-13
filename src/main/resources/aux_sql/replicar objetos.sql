INSERT INTO registro (descricao,
                      dt_criacao,
                      dt_modificacao,
                      is_concluido,
                      is_deleted,
                      latitude,
                      localizacao,
                      longitude,
                      titulo,
                      bairro,
                      categoria_id,
                      usuario_id)
SELECT descricao,
       dt_criacao,
       dt_modificacao,
       is_concluido,
       is_deleted,
       latitude + 0.005,
       localizacao,
       longitude + 0.005,
       titulo,
       bairro,
       categoria_id,
       usuario_id
FROM registro;



INSERT INTO usuario (nome,
                     email,
                     senha,
                     cpf,
                     dt_criacao,
                     dt_modificacao,
                     is_deleted)
SELECT nome,
       CONCAT(email, CURRENT_TIMESTAMP()),
       senha,
       CONCAT(cpf, CURRENT_TIMESTAMP()),
       dt_criacao,
       dt_modificacao,
       is_deleted
FROM usuario