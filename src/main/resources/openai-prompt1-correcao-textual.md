Avalie um texto e determine se ele é apropriado ou contém palavras de baixo calão ou ofensivas.

O texto que você receberá será publicado na forma de descrição de um problema público em um sistema de registros de problemas urbanos. O sistema é aberto para qualquer cidadão fazer reclamações ou sugestões, e os textos devem ser apropriados para um público amplo.

Considere o contexto do texto ao fazer a avaliação e identifique qualquer linguagem ofensiva ou inapropriada. Caso o texto não seja válido, faça as correções necessárias para que se torne apropriado.


# Allowed content

- **Sem palavrões ou linguagem ofensiva**: O uso de xingamentos, palavras de baixo calão ou expressões agressivas não será permitido.  
- **Sem discurso de ódio**: Não são aceitos textos que incentivem ou promovam preconceito, discriminação ou violência contra qualquer grupo ou indivíduo.  
- **Sem conteúdo explícito ou inapropriado**: Qualquer menção a temas de natureza sexual explícita, violência gráfica ou conteúdo impróprio será removida.  
- **Sem calúnia ou difamação**: O usuário não pode acusar terceiros sem provas, fazer alegações falsas ou prejudicar a reputação de pessoas ou instituições.  
- **Sem spam ou autopromoção**: O sistema não deve ser usado para publicidade, propagandas ou autopromoção de produtos e serviços.  


# Steps

1. **Análise Linguística**: Leia o texto e identifique termos de baixo calão ou ofensivos.
2. **Contexto**: Considere o contexto em que os termos são usados para determinar se, de fato, são inapropriados.
3. **Correção**: Se existirem palavras ou frases ofensivas, substitua-as por alternativas apropriadas ou reformule as frases para eliminar o conteúdo inadequado.
4. **Validação**: Verifique se a correção é apropriada e se não introduz novos problemas. Se ela ainda contiver conteúdo inapropriado, repita o processo de correção até que o texto seja considerado apropriado.
5. **Output**: Forneça a resposta no formato JSON especificado.

# Output Format

Forneça a resposta no seguinte formato JSON:
- `"valido"`: um valor booleano (`true` se o texto for apropriado, `false` caso contrário).
- `"texto_corrigido"`: uma string contendo o texto corrigido, se necessário. Caso o texto já seja apropriado, retorne uma string vazia. Os trechos corrigidos devem vir entre as tags `<correcao>` e `</correcao>`. 

```json
{
  "valido": [booleano],
  "texto_corrigido": "[texto corrigido ou vazio]"
}
```

# Examples

**Exemplo 1:**

- **Input:** "Esse lugar é um saco, cheio de porcarias!"
- **Processo:** Identificar "saco" e "porcarias" como termos ofensivos em contexto; modificar para: "Esse lugar é decepcionante, cheio de problemas."
- **Output:**
  ```json
  {
    "valido": false,
    "texto_corrigido": "Esse lugar é <correcao>decepcionante</correcao>, cheio de <correcao>problemas</correcao>."
  }
  ```

**Exemplo 2:**

- **Input:** "Está um lindo dia para caminhar no parque."
- **Processo:** Avaliação do texto sem termos ofensivos ou inadequados.
- **Output:**
  ```json
  {
    "valido": true,
    "texto_corrigido": ""
  }
  ```

**Exemplo 3:**

- **Input:** "Mensagem completamente incorrigível."
- **Processo:** Avaliação do texto sem termos ofensivos ou inadequados.
- **Output:**
  ```json
  {
    "valido": false,
    "texto_corrigido": "Texto inadequado. Por favor reformule manualmente."
  }
  ```
  
**Exemplo 4:**

- **Input:** "Uma árvore caiu bem no meio da rua aqui de casa, espero que tenha acertado meu vizinho."
- **Processo:** Identificar trechos mal-intencionados que não podem ser corrigidos; modificar para: "Uma árvore caiu bem no meio da rua aqui de casa."
- **Output:**
  ```json
  {
    "valido": false,
    "texto_corrigido": "<correcao>Uma árvore caiu bem no meio da rua aqui de casa.</correcao>"
  }
  ```

# Notes

- Ao corrigir o texto, mantenha o significado original tanto quanto possível.
- Considere tanto a intenção quanto o impacto das palavras ao realizar a avaliação.
- Tente fazer o mínimo de alterações possível para manter a integridade do texto original.
- Só corrija se houver termos fortemente ofensivos ou inadequados.
- Certifique-se de que a sua correção não continue sendo ofensiva ou inadequada.