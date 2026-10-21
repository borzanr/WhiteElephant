# White Elephant / Amigo Ladrão

Organizador de troca de presentes com roubo (White Elephant, Dirty Santa, Yankee
Swap): **antes da festa** manda um convite com data, valor e regras; **na hora**,
opcionalmente, sorteia a ordem e acompanha os roubos. Offline, sem cadastro.
Quarto app da família `com.borzanti.*`: build, anúncios, consentimento e tema
vieram do Amigo Secreto.

| | |
|---|---|
| `applicationId` | `com.borzanti.whiteelephant` |
| Pacote de código | `com.whiteelephant.app` |
| Versão | 1.0 (`versionCode` 1) |
| Envio à revisão | **feito em 21/10/2026** (prazo era 31/10; pico de busca: 7–21/12) |

## Por que um app separado

Regra "um app, um trabalho": o Amigo Secreto sorteia quem dá presente para quem,
em segredo; este conduz uma brincadeira pública, na sala. Na Play o termo
"white elephant" está vazio, e "white elephant rules" tem mais busca nos EUA do
que "secret santa generator". Palavras-chave separadas: este app é dono de white
elephant / dirty santa / yankee swap; o Amigo Secreto, de secret santa / gift
exchange.

## Antes, não durante (decidido em 20/10/2026)

O Amigo Secreto é usado **antes** da brincadeira, pelo organizador, e cada
sorteio chega ao celular do grupo inteiro pelos links. A primeira versão deste
app era só um condutor da partida — e app na mão durante a troca compete com a
diversão; numa família de 8 ninguém abre. Por isso a tela principal virou o
**convite**, e o condutor ficou como apoio opcional, sem nenhuma digitação no
meio da partida (o diálogo "o que tinha dentro?" foi removido).

## Escopo da v1

**A festa (principal):**
- Nome, data, hora, valor e as três regras, que viram um **link de convite**
  (`share/InviteLink.kt` ↔ `site/i/index.html`). A página mostra tudo isso,
  explica como funciona e tem "Adicionar ao Google Agenda" e arquivo `.ics`.
- As três regras vêm ligadas e mudam numa linha "Mudar regras": congela no 3º
  roubo, sem roubo de volta na mesma rodada, troca final para quem abriu primeiro.

**Na hora da brincadeira (opcional):**
- Lista de pessoas, aceitando lista colada do WhatsApp (vírgula ou uma por linha).
- Ordem de escolha sorteada, em letra grande.
- Controle de roubos com um toque por jogada e "desfazer" para toque errado.
- Partida salva a cada jogada: tela apagada ou app morto não perdem a noite.

## Idiomas

App e convite em **inglês, português e espanhol**, com seletor no menu. Os outros
idiomas do Amigo Secreto ficaram de fora depois de olhar o Google Trends (5 anos,
20/10/2026), comparando o termo da brincadeira com roubo com o do sorteio comum:

| País | Brincadeira com roubo | Sorteio comum | Veredito |
|---|---|---|---|
| EUA | "white elephant" 100 | "secret santa generator" 8 | o nicho do app |
| Alemanha | "Schrottwichteln" 100 | "Wichteln online" 17 | muita busca, mas **outro jogo** (dado e cronômetro) |
| México | "intercambio ladrón" 3 | "intercambio de regalos" 100 | quase nada |
| Espanha | "amigo invisible ladrón" 0 | "amigo invisible" 100 | nada |
| França | "cadeau volé jeu" 0 | "secret santa" 100 | nada |
| Itália | "babbo natale ladro" 0 | "secret santa" 100 | nada |

O espanhol entrou pelo público hispânico dos EUA, não pela busca em espanhol — por
isso não há página de regras em espanhol, e o nome do app em espanhol continua
"White Elephant" (no México, "intercambio de regalos" é o amigo secreto comum).

## Site

Fonte em `site/`, publicado por `site/publish.sh` num repositório
`whiteelephant-site` (GitHub Pages + `CNAME`), igual ao Amigo Secreto:

| Caminho | O quê |
|---|---|
| `/` | Regras públicas em inglês, indexáveis — mira "white elephant rules" |
| `/regras/` | As mesmas regras em português |
| `/i/#…` | Convite (dados no fragmento, nunca vão ao servidor) |
| `/privacy/` | Política de privacidade, EN e PT |

O recap do fim da noite (terceira página do plano original) ficou para depois.

**Fora da v1, de propósito:** grupos salvos, histórico, link secreto, sugestões
de presente, som, temporizador e sincronizar celulares (exigiria servidor — e o
diferencial é não ter como falhar).

## A garantia do jogo

`game/Game.kt` é imutável e sem Android. O teste
`partidas aleatorias sempre terminam com um presente por pessoa` joga 2.400
partidas com jogadas aleatórias nas 8 combinações de regras e confere que toda
partida termina, que cada pessoa sai com exatamente um presente e que quem está
na vez nunca tem presente durante as rodadas.

```bash
./gradlew testDebugUnitTest
```

## Anúncios e avaliação

- Banner na preparação; intersticial antes do resumo, **nunca na primeira
  partida do aparelho** e depois em partidas alternadas.
- In-App Review na primeira vez que acontecer uma destas: voltar ao app depois
  de enviar o convite, ou o resumo de uma partida terminada. Só o resumo não
  bastaria — o condutor é opcional, e o Amigo Secreto passou meses sem nenhuma
  avaliação por esperar um passo que quase ninguém completava.
- **Falha fechada:** sem IDs de produção em `ads/AdIds.kt`, o release não pede
  anúncio nenhum.

## Pendências antes do envio

Do Rafael:
- [x] Criar o app no AdMob e 2 unidades (banner, intersticial) — 20/10/2026.
- [ ] Depois de publicado: AdMob → Apps → White Elephant → Vincular à loja (sem
      isso o formulário de consentimento europeu não aparece).
- [x] Keystore de upload em `C:\Users\Rafael\keystores\whiteelephant-upload.jks`
      (alias `upload`, válida até 2054) e `keystore.properties` — 20/10/2026.
- [x] Criar o app na Play Console — 21/10/2026, conta Borzanti, "White Elephant &
      Dirty Santa", en-US, app grátis.
- [ ] Aprovar textos da ficha e o ícone.

Do código:
- [x] App ID real no `AndroidManifest.xml` e IDs de produção em `ads/AdIds.kt`.
- [x] Ícone definitivo — 21/10/2026: elefante mago do Higgsfield (GPT Image 2.5,
      opção 2), fonte em `playstore/art/icon-art.png`; `playstore/icon-512.png` para a Play.
- [ ] Publicar o site: criar o repositório `whiteelephant-site`, o DNS de
      `whiteelephant.borzanti.com` e ligar o GitHub Pages (Rafael), depois
      `site/publish.sh` e push.
- [ ] Ficha da loja e capturas — o primeiro print mostra o convite.
