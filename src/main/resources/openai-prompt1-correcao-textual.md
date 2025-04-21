Avalie o texto a seguir e determine se ele está de acordo com as regras de publicação listadas. Em seguida, retorne um JSON seguindo o formato especificado.  
**Importante**: Toda e qualquer correção feita deve **também** seguir as regras de publicação – não use termos ou expressões que resultem em novas violações.

### Regras de publicação

1. **Sem palavrões ou linguagem ofensiva**: Não utilize xingamentos, palavras de baixo calão ou expressões agressivas.  
2. **Sem discurso de ódio**: Não incentive nem promova preconceito, discriminação ou violência contra qualquer grupo ou indivíduo.  
3. **Sem conteúdo explícito ou inapropriado**: Evite temas de natureza sexual explícita, violência gráfica ou outros conteúdos impróprios.  
4. **Sem calúnia ou difamação**: Não acuse terceiros sem provas, não faça alegações falsas nem prejudique a reputação de pessoas ou instituições sem embasamento.  
5. **Sem spam ou autopromoção**: Não utilize para anúncios, publicidade ou promoção de produtos e serviços.

### Instruções de correção

- Se o texto violar alguma das regras acima, corrija **somente** o trecho ofensivo ou inadequado, mantendo o sentido original o máximo possível. Envolva cada correção entre `<correcao>` e `</correcao>`.
- **A correção resultante não pode violar nenhuma das regras de publicação.**  
- Se o texto não puder ser corrigido (por exemplo, quando for totalmente inapropriado ou ofensivo), retorne um texto genérico no campo `"texto_corrigido"`: `"Texto inadequado. Por favor reformule manualmente."`

### Formato de saída

Retorne **exclusivamente** o seguinte JSON (sem texto adicional), onde:
- `valido` é um valor booleano indicando se o texto é apropriado (`true`) ou não (`false`).
- `texto_corrigido` é o texto com as devidas correções entre `<correcao>` e `</correcao>`, se houver. Se o texto for adequado, deixe este campo vazio.

```json
{
  "valido": [true ou false],
  "texto_corrigido": "[texto corrigido ou vazio]"
}
```

### Exemplos

**Exemplo 1**  
- **Texto**: "Esse lugar é um saco, cheio de porcarias!"  
- **Correção**: "Esse lugar é <correcao>decepcionante</correcao>, cheio de <correcao>problemas</correcao>."  
- **Saída**:
  ```json
  {
    "valido": false,
    "texto_corrigido": "Esse lugar é <correcao>decepcionante</correcao>, cheio de <correcao>problemas</correcao>."
  }
  ```

**Exemplo 2**  
- **Texto**: "Está um lindo dia para caminhar no parque."  
- **Saída**:
  ```json
  {
    "valido": true,
    "texto_corrigido": ""
  }
  ```

**Exemplo 3**  
- **Texto**: "Mensagem completamente incorrigível."  
- **Correção**: Não é possível.  
- **Saída**:
  ```json
  {
    "valido": false,
    "texto_corrigido": "Texto inadequado. Por favor reformule manualmente."
  }
  ```

**Exemplo 4**  
- **Texto**: "Uma árvore caiu bem no meio da rua aqui de casa, espero que tenha acertado meu vizinho."  
- **Correção**: "Uma árvore caiu bem no meio da rua aqui de casa."  
- **Saída**:
  ```json
  {
    "valido": false,
    "texto_corrigido": "<correcao>Uma árvore caiu bem no meio da rua aqui de casa.</correcao>"
  }
  ```

**Exemplo 5**
- **Texto**: "Aquele lugar é uma verdadeira porcaria, não vale a pena."
- **Correção**: "Aquele lugar é <correcao>decepcionante</correcao>, não vale a pena."
- **Saída**:
  ```json
  {
    "valido": false,
    "texto_corrigido": "<correcao>Aquele lugar é decepcionante</correcao>, não vale a pena."
  }
  ```
  
**Exemplo 6**
- **Texto**: "Essa cidade é decepcionante, cheia de políticos que não cumprem com suas promessas."
- **Saída**:
  ```json
  {
    "valido": true,
    "texto_corrigido": ""
  }
  ```
  
**Exemplo 7**
- **Texto**: "Minha casa alagou muito com a última chuva, o prefeito poderia tomar uma providência né."
- **Saída**:
  ```json
  {
    "valido": true,
    "texto_corrigido": ""
  }
  ```
  
**Exemplo 8**
- **Texto**: "Aquela pessoa é um verdadeiro lixo, não merece respeito."
- **Correção**: "Aquela pessoa é <correcao>desrespeitosa</correcao>, não merece respeito."
- **Saída**:
  ```json
  {
    "valido": false,
    "texto_corrigido": "<correcao>Aquela pessoa é desrespeitosa</correcao>, não merece respeito."
  }
  ```
  
**Exemplo 9**
- **Texto**: "Aquela pessoa é desrespeitosa, não merece respeito."
- **Saída**:
  ```json
  {
    "valido": true,
    "texto_corrigido": ""
  }
  ```
  
**Exemplo 10**
- **Texto**: "Este parque está muito sujo, consertem isso rapidamente!"
- **Saída**:
  ```json
  {
    "valido": true,
    "texto_corrigido": ""
  }
  ```
  
**Exemplo 11**
- **Texto**: "Compre o melhor produto na minha loja: www.grandespromos.com"
- **Correção**: Não é possível.  
- **Saída**:
  ```json
  {
    "valido": false,
    "texto_corrigido": "Texto inadequado. Por favor reformule manualmente."
  }
  ```
  
**Exemplo 12**
- **Texto**: "Acho que todos os imigrantes deveriam ser expulsos desta cidade."
- **Correção**: Não é possível.  
- **Saída**:
  ```json
  {
    "valido": false,
    "texto_corrigido": "Texto inadequado. Por favor reformule manualmente."
  }
  ```
  



---

**Observação**:  
- Caso o texto seja apropriado, retorne `"valido": true` e `"texto_corrigido": ""`.  
- Se houver correções parciais ou totais, retorne `"valido": false` com o texto corrigido.  
- Se não for possível corrigir, retorne a mensagem genérica em `"texto_corrigido"`.