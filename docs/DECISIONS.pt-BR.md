# Decisões do projeto

[English](DECISIONS.md) · **Português (Brasil)**

Registrem aqui as decisões importantes do time. Cada entrada deve ser curta e trazer o que foi decidido, as alternativas consideradas e o motivo da escolha.

A documentação do projeto deve ser escrita **em inglês** (a versão principal é o [`DECISIONS.md`](DECISIONS.md)), com uma **versão em português** ao lado. Mantenham as duas versões alinhadas.

Mantenham este arquivo na **raiz** do repositório. Incluam uma entrada nova para cada decisão relevante (stack, arquitetura, recorte do MVP, o que ficou de fora, caminhos que vocês tentaram e abandonaram etc.). Este arquivo serve de roteiro para o bloco de decisões da apresentação.

Copiem o modelo abaixo sempre que precisarem de uma entrada nova.

## Modelo

```markdown
## [data] — [título da decisão]
- **Decisão:**
- **Alternativas consideradas:**
- **Motivo:**
```

## 23/11/2026 — Escolha da stack de back-end (exemplo)

- **Decisão:** usar Node.js com Express no back-end.
- **Alternativas consideradas:** Java com Spring Boot; Python com FastAPI.
- **Motivo:** o time já tem mais familiaridade com JavaScript, e o prazo de uma semana favorece uma stack na qual todos conseguem contribuir desde o primeiro dia.

## 23/11/2026 — Escolha da stack de back-end

- **Decisão:** usar java no back-end
- **Alternativas consideradas:** usar javaScript  
- **Motivo:** a maior parte da equipe tem mais facilidade com java e com o prazo de uma semana é melhor que a maior parte já tenha familiaridade com a linguagem usada  


## 24/11/2026 — Escolha da stack de front-end

- **Decisão:** usar java com swing no front-end
- **Alternativas consideradas:** usar javaScript  
- **Motivo:** a maior parte da equipe tem mais facilidade com java e com o prazo de uma semana é melhor que a maior parte já tenha familiaridade com a linguagem usada 


## 24/11/2026 — desktop, mobile ou web?

- **Decisão:** apenas desktop
- **Alternativas consideradas:** criar para web  
- **Motivo:** a maior parte da equipe tem mais facilidade com java e por isso decidimos usar java swing o que nos faz criar para deskto




## 25/11/2026 — Qual IA vamos usar para nos auxiliar no projeto 

- **Decisão:** gemini e chatgpt
- **Alternativas consideradas:** deepseek  
- **Motivo:** são IA mais conhecidas e que já usamos no dia-dia


## 25/11/2026 — adicionar historico de anuncios criados
- **Decisão:** nós decidimos criar uma tela para o usuario acessar todos os anuncios que já colocou
- **Alternativas consideradas:** não criar por não ser absolutamente necessario
- **Motivo:** deixar mais proximo de uma aplicação real e tambem mais organizado



## 25/11/2026 — criar banco de dados compartilhado no supabase
- **Decisão:** nós decidimos criar um banco de dados compartilhado no supabase
- **Alternativas consideradas:** continuar com o SQLlite que estava sendo usado temporariamente
- **Motivo:** precisavamos de um banco compartilhado para fazer com que o usuario possa ver pessoas que tenham interrese em seus anuncios
   então o professor jeferson nos recomendou o uso do supabase

## 25/11/2026 — criar tela de login
- **Decisão:** nós decidimos criar uma tela de login
- **Alternativas consideradas:** deixar sem por enqunato para agilizar a entrada na aplicação
- **Motivo:** precisavamos que fosse possivel  criar contas para criar anuncios e mandar mensssagens de interesse em anuncios 













