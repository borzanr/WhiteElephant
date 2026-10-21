# Publicação — White Elephant / Amigo Ladrão

Tudo o que a Play Console pede, pronto para colar. Mesmo roteiro do Amigo
Secreto (`SecretSanta/playstore/README.md`); aqui só o que muda.

App criado em 21/10/2026 na conta Borzanti: `com.borzanti.whiteelephant`,
idioma padrão **en-US**, **App**, **Grátis**.

## Arquivos para upload

| Arquivo | Tamanho | Onde vai |
| --- | --- | --- |
| `icon-512.png` | 512×512 | Ícone do app (a Play arredonda sozinha) |
| `feature-graphic-en.png` / `-pt` / `-es` | 1024×500 | Gráfico de destaque de cada ficha |
| `screenshots-en/01…06.png` / `-pt` / `-es` | 1080×1920 | Capturas de cada ficha |
| `data_safety.csv` | — | Segurança dos dados → Importar CSV |

As capturas são reais, do Redmi Note 9S (fontes em `src/shots-<lang>/`), com dados
de exemplo inventados. A moldura corta barra de status, barra do Chrome, banner de
anúncio de teste e barra de navegação: **nenhuma captura mostra anúncio**.
Capturas **aprovadas pelo Rafael em 21/10/2026**. Gráficos de destaque com o
elefante presidiário (Higgsfield, GPT Image 2.5 média, opção 2 de 4, 2 créditos),
escolhido pelo Rafael em 21/10/2026; fonte em `art/feature-art.png`. Para
regerar: `bash playstore/render.sh`.

## Configuração do app

**Preenchida na Play Console em 21/10/2026** (aprovado pelo Rafael), com as
respostas abaixo; categoria e tags copiadas exatamente do Amigo Secreto (App,
Estilo de vida; tags Entretenimento, Estilo de vida, Eventos, Social). A
importação do `data_safety.csv` não preencheu o questionário: as mesmas respostas
foram marcadas à mão. Classificação IARC resultante: Livre (ClassInd), Everyone
(ESRB), PEGI 3, USK 0, 3+ no resto do mundo.

| Item | Resposta |
| --- | --- |
| Política de privacidade | `https://whiteelephant.borzanti.com/privacy/` |
| Acesso ao app | Todo o conteúdo disponível sem restrições (não há login) |
| Anúncios | **Sim, contém anúncios** |
| Público-alvo | **13–15, 16–17 e 18+**. Nada abaixo de 13: anúncios personalizados levariam à política de Famílias |
| Apela a crianças? | Não |
| App de notícias / governo / financeiro / saúde | Não / Não / Nenhum / Não |
| ID de publicidade | **Sim**, para **Publicidade ou marketing** (SDK do AdMob) |
| Categoria | **Estilo de vida** (Lifestyle), a mesma do Amigo Secreto: organizar troca de presentes |
| E-mail de contato | `admin@borzanti.com` |
| Site | `https://whiteelephant.borzanti.com` — onde o AdMob procura o `app-ads.txt` |

### Classificação de conteúdo
Categoria **Utilitário, produtividade, comunicação ou outro**. Tudo **Não**:
- **Violência:** não. "Roubar" é o nome da jogada da brincadeira (pegar um
  presente de outra pessoa), não há conteúdo violento.
- **Jogos de azar:** não. O sorteio é só da ordem de escolha; sem dinheiro,
  prêmio, aposta ou cassino.
- **Usuários interagem / trocam conteúdo:** não. Sem chat e sem conta; o convite
  sai pelo app de mensagens que a pessoa escolher.
- **Localização:** não.

Resultado esperado: Livre / Everyone / PEGI 3.

### Segurança dos dados
**Importar CSV** → `playstore/data_safety.csv` (o mesmo do Amigo Secreto, com a
URL de exclusão trocada). Declara só o **ID do dispositivo**, coletado e
compartilhado com o Google para publicidade. Nomes, festa e partida nunca saem do
aparelho; o convite é enviado pela pessoa pelo app de mensagens que ela escolher.

### Países
**Todos os países.** O consentimento europeu já está publicado no AdMob (20/10).

---

## Fichas da loja

**Aprovadas pelo Rafael em 21/10/2026, nos três idiomas, como estão abaixo.**
Mudança de texto daqui em diante precisa de nova aprovação.

Regras: o título começa pelo termo de busca; nenhum texto cita marca de
terceiros (WhatsApp, Google Agenda) — motivo comum de reprovação de metadados.

**Separação de palavras-chave com o Amigo Secreto (decidida em 19/10/2026):** este
app é dono de *white elephant / dirty santa / yankee swap / amigo ladrão*; o Amigo
Secreto, de *secret santa / gift exchange / amigo secreto / amigo invisible /
intercambio de regalos*. Nenhuma das fichas usa o termo da outra — nem no título,
nem nas descrições. A única ponte permitida é uma linha sem o termo ("prefere
sortear em segredo? procure nosso outro app").

**Por que "Amigo Ladrão" e não "Amigo da Onça" no título pt-BR** (Trends Brasil,
5 anos, 21/10/2026): "amigo secreto" 100, "amigo da onça" 15, "amigo ladrão" 5,
"amigo secreto ladrão" 1. "Amigo da onça" busca mais, mas na maioria dos grupos é
amigo secreto com presente de zoeira, sem roubo — e ainda é expressão do dia a
dia. No título traria quem quer um sorteio de amigo secreto, que é o trabalho do
outro app. Fica só na descrição longa, como "amigo da onça com roubo".

### en-US (padrão)

**Title** (30)
```
White Elephant & Dirty Santa
```

**Short description** (80)
```
Invite your group, set the rules and track every steal. No signup, no email.
```

**Full description**
```
Host a White Elephant party without the chaos. Send one invite with the date, the budget and your rules, then let the app draw the order and keep track of every steal at the party.

ONE INVITE FOR THE GROUP CHAT
Pick the date, time and gift budget, choose your house rules and send a single link to the group. Guests open it in the browser — nothing to install — and see the details, how the game works and the rules you agreed on. They can add it to their calendar in one tap.

RULES SETTLED BEFORE THE PARTY
Most White Elephant arguments are about stealing. Decide ahead of time:
• Gifts freeze after 3 steals
• No stealing back a gift that was just taken
• A final swap for whoever went first
Classic rules come switched on. Change any of them in one place.

AT THE PARTY (OPTIONAL)
• Paste the guest list and draw a random order, shown big enough for the whole room
• Open or steal with one tap — the app knows whose turn it is, even after a steal
• Steal counter for every gift, frozen gifts locked automatically
• Undo for the wrong tap
• Game saved after every move, even if the screen locks
• End-of-night summary: who took what home and the most wanted gift

WHITE ELEPHANT, DIRTY SANTA, YANKEE SWAP
Same game, many names: everyone brings one wrapped gift, and anyone can steal. Prefer to draw names in secret instead? Look for our other app.

PRIVATE BY DESIGN
No account, no email. Names stay on your phone and never go into the invite. The invite's details travel inside the link itself.

English, Spanish and Portuguese. Free, supported by ads.
```

### pt-BR

**Título** (30)
```
Amigo Ladrão: Rouba Presente
```

**Descrição curta** (80)
```
Convide o grupo, combine as regras e controle cada roubo. Sem cadastro.
```

**Descrição completa**
```
Organize o amigo ladrão sem confusão. Mande um convite com data, valor e as regras do grupo, e na festa deixe o app sortear a ordem e acompanhar cada roubo.

UM CONVITE PARA O GRUPO
Escolha data, hora e valor do presente, defina as regras da casa e mande um link só para o grupo. Quem recebe abre no navegador — sem instalar nada — e vê os detalhes, como a brincadeira funciona e as regras combinadas. Dá para salvar na agenda com um toque.

REGRAS COMBINADAS ANTES DA FESTA
Quase toda discussão no amigo ladrão é sobre roubo. Combine antes:
• O presente congela no 3º roubo
• Não vale roubar de volta o presente que acabaram de tirar de você
• Troca final para quem jogou primeiro
As regras clássicas já vêm ligadas. Mude qualquer uma num lugar só.

NA HORA DA BRINCADEIRA (OPCIONAL)
• Cole a lista de convidados e sorteie a ordem, em letra grande para a sala toda ver
• Abrir ou roubar com um toque — o app sabe de quem é a vez, mesmo depois de um roubo
• Contador de roubos em cada presente, congelado automaticamente
• Desfazer para o toque errado
• Partida salva a cada jogada, mesmo com a tela apagada
• Resumo do fim da noite: quem levou o quê e o presente mais disputado

AMIGO LADRÃO, AMIGO DA ONÇA COM ROUBO, WHITE ELEPHANT
Mesma brincadeira, vários nomes: cada um leva um presente embrulhado e qualquer um pode roubar. Prefere sortear em segredo quem tira quem? Procure nosso outro app.

PRIVADO DE VERDADE
Sem conta, sem e-mail. Os nomes ficam no seu celular e nunca entram no convite. Os dados do convite viajam dentro do próprio link.

Em português, inglês e espanhol. Grátis, mantido por anúncios.
```

### es-419 e es-ES

**Título** (30)
```
White Elephant: Roba Regalos
```

**Descripción breve** (80)
```
Invita al grupo, acuerda las reglas y controla cada robo. Sin registro.
```

**Descripción completa**
```
Organiza un White Elephant, el juego de robar regalos, sin caos. Envía una invitación con la fecha, el monto y sus reglas, y en la fiesta deja que la app sortee el orden y lleve la cuenta de cada robo.

UNA INVITACIÓN PARA EL GRUPO
Elige fecha, hora y monto del regalo, define las reglas de la casa y envía un solo enlace al grupo. Quien la recibe la abre en el navegador — sin instalar nada — y ve los detalles, cómo se juega y las reglas acordadas. Puede guardarla en su calendario con un toque.

REGLAS ACORDADAS ANTES DE LA FIESTA
Casi todas las discusiones son por los robos. Acuérdenlo antes:
• El regalo se congela al 3er robo
• No se vale robar de vuelta el regalo que te acaban de quitar
• Cambio final para quien jugó primero
Las reglas clásicas vienen activadas. Cambia cualquiera en un solo lugar.

EN EL JUEGO (OPCIONAL)
• Pega la lista de invitados y sortea el orden, en letra grande para que todos lo vean
• Abrir o robar con un toque — la app sabe de quién es el turno, incluso después de un robo
• Contador de robos en cada regalo, congelado automáticamente
• Deshacer para el toque equivocado
• Juego guardado en cada jugada, aunque se apague la pantalla
• Resumen del final de la noche: quién se llevó qué y el regalo más disputado

WHITE ELEPHANT, DIRTY SANTA, YANKEE SWAP
El mismo juego con muchos nombres: cada uno lleva un regalo envuelto y cualquiera puede robar. ¿Prefieres sortear en secreto quién le regala a quién? Busca nuestra otra app.

PRIVADO DE VERDAD
Sin cuenta, sin correo. Los nombres se quedan en tu teléfono y nunca van en la invitación. Los datos de la invitación viajan dentro del propio enlace.

En español, inglés y portugués. Gratis, con anuncios.
```
