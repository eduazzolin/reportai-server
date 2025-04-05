INSERT INTO registro (descricao,
                      dt_criacao,
                      dt_modificacao,
                      is_concluido,
                      is_deleted,
                      latitude,
                      localizacao,
                      longitude,
                      titulo,
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
       categoria_id,
       usuario_id
FROM registro
WHERE usuario_id = 2