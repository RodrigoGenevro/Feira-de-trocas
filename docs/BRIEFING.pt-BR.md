# Briefing do tema: Feira de trocas

[English](BRIEFING.md) · **Português (Brasil)**

**Hack com IA: Codando com Inteligência**  
Senac na Prática · Técnico em Desenvolvimento de Sistemas  
Área: vida no campus e comunidade  
Imersão de 23/11 a 29/11 · apresentações em 30/11 e 01/12

Este briefing é o ponto de partida do **seu** tema. Leiam com calma, recortem um MVP que caiba em uma semana e registrem as decisões. Vocês vão transformar este problema em um produto real, com front-end e back-end, repositório organizado e uso documentado de IA. Esta é uma imersão colaborativa: a banca avalia o que o time construiu, decidiu e aprendeu.

## Tema e contexto

**Feira de trocas** — Marketplace para trocar ou doar livros, materiais e equipamentos usados.

Livros, kits, calculadoras, cabos e até um mouse em bom estado entram e saem da vida do estudante o tempo todo. Quando a disciplina acaba, o material fica na estante — enquanto outra pessoa da turma seguinte precisa do mesmo item e não sabe a quem pedir. As trocas e doações já acontecem no corredor e nas redes, mas de forma irregular: o anúncio some, o combinado é frágil, e nem sempre fica claro se o item está disponível, reservado ou já foi entregue.

Vocês vão criar um mural confiável para a comunidade do campus trocar ou doar o que ainda tem uso, com anúncios claros e um fluxo simples até o encontro.

## Público e personas

- **Quem quer doar ou trocar:** precisa descrever o item com facilidade e marcar quando ele não está mais disponível.
- **Quem está procurando um material:** quer buscar por categoria ou palavra e demonstrar interesse sem ruído.
- **Pessoas da comunidade do campus:** o recorte é o Senac e o entorno da vida estudantil, não uma loja aberta a qualquer comprador.

## Objetivo

Entregar um MVP de mural de trocas e doações em que dá para publicar um item, encontrá-lo na busca, demonstrar interesse e concluir o combinado — com a IA ajudando a descrever e a organizar os anúncios.

## Requisitos mínimos (MVP)

O recorte abaixo é realista para uma semana. Fechem-no no primeiro dia e registrem no `DECISIONS.md` o que entra, o que fica de fora e por quê. É melhor um fluxo completo e demonstrável do que uma lista longa pela metade.

1. Publicar um anúncio com título, descrição, foto, categoria e modalidade (troca ou doação).
2. Listar, buscar e filtrar anúncios por categoria ou palavra-chave.
3. Demonstrar interesse por um item (um pedido ou mensagem curta registrada no sistema).
4. Atualizar o status do anúncio (disponível, reservado, concluído).
5. Um perfil simples de quem anuncia (nome e, se fizer sentido, turma), sem dados desnecessários.
6. A pessoa que anunciou vê os interesses recebidos e pode aceitar ou encerrar o anúncio.

## Onde a IA entra

O uso de IA é liberado e incentivado — no produto e no processo — e precisa aparecer no `AI_LOG.md`. Algumas frentes concretas neste tema:

- Gerar ou melhorar a descrição a partir da foto e de algumas notas curtas, para a pessoa revisar.
- Sugerir categoria e estado de conservação (por exemplo, usado, em bom estado).
- Ajudar na busca com linguagem natural (“procurando livro de banco de dados”).
- Sinalizar anúncios pouco claros, para quem publicou completar a informação antes de finalizar.

Não publiquem, sem revisão de vocês, um texto ou uma decisão automática da IA como se já fosse a versão final.

## Extras opcionais

Só depois que o MVP estiver em pé. São ideias, não obrigações:

- Sugestão de ponto de encontro no campus.
- Lista de favoritos.
- Um mural de “estou procurando” além dos itens já anunciados.
- Aviso quando entrar um item parecido com o que a pessoa busca.

## Fora de escopo

Para proteger o prazo e o foco do produto:

- Pagamento, checkout ou carrinho de compras — o recorte é trocar ou doar, não vender como loja.
- Envio pelos Correios ou logística de entrega em casa.
- Nota fiscal, garantia e devolução como em um comércio eletrônico.
- Anúncios abertos ao comércio em geral, fora da comunidade do campus.

## Perguntas para guiar o time

Usem estas perguntas no primeiro dia, ao definir o MVP, e de novo antes da demo:

- Como vocês lidam com um anúncio inadequado ou com um item que não deveria estar ali?
- O que acontece se o item for reservado e ninguém o retirar?
- Como as duas pessoas combinam o encontro com segurança, sem expor telefone em uma página pública?
- Como a busca se comporta quando a descrição é curta ou a foto é ruim?
- Quem pode editar ou tirar do ar um anúncio, além de quem o criou?

## Entregáveis e como a banca avalia

Esta é uma **imersão colaborativa** de uma semana, com times de 3 ou 4 estudantes do TDS. A banca avalia o trabalho de **vocês**, no próprio projeto — um retorno formativo, não uma comparação com outros times.

**O que precisa estar pronto no repositório** (raiz), em **inglês** (versão principal) e em **português** (`.pt-BR`), com o link entre os idiomas no topo de cada arquivo:

- `README.md` e `README.pt-BR.md`
- `DECISIONS.md` e `DECISIONS.pt-BR.md`
- `AI_LOG.md` e `AI_LOG.pt-BR.md`

A aplicação precisa ter **front-end e back-end**. O back-end pode ser **Java**, **Python** ou **Node.js** (isolados ou combinados, desde que integrados). O front-end é obrigatório (web, desktop ou mobile). Justifiquem a stack no `DECISIONS.md`. Também são obrigatórios o histórico com Conventional Commits, o trabalho via Pull Request com revisão, a proteção da `main` e uma pipeline de **GitHub Actions** verde (build, lint e testes) em PRs e na `main`. Não commitem segredos. No primeiro dia, adicionem o professor como colaborador: **adrianoanschau@gmail.com**.

O **congelamento do código** é no domingo, **29/11, às 23h59**. As apresentações são em **15 minutos + 5 minutos de perguntas**, nos dias 30/11 e 01/12, no Senac de Canoas, Sala 103. Ensaiem a demo e o roteiro (problema e solução, demo ao vivo, fluxo de trabalho, decisões, IA na prática e CI/CD).

A avaliação tem **duas bancas, com o mesmo peso**:

- **Banca Técnica (50%):** qualidade de código e arquitetura; Git, Conventional Commits e PRs; CI/CD; domínio do código nas perguntas.
- **Banca de Inovação e Impacto (50%):** originalidade; impacto (o problema real e para quem); experiência do usuário; clareza do pitch; uso criativo da IA.

Qualquer pessoa do time pode ser perguntada sobre qualquer parte do projeto. Usem a IA com inteligência, registrem o que ela gerou e o que vocês corrigiram, e assumam o código que forem apresentar.

Bom trabalho — e que a semana seja intensa, colaborativa e bem documentada.

