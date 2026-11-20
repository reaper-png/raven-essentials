# O que falta no RavenEssentials

Levantado do cartão **Raven Essentials** (lista "A fazer" do quadro Raven Network, no Trello),
a partir da página salva em 2026-11-20.

| Checklist | Feito no cartão |
|---|---|
| Comandos | 27/59 |
| Funções | 0/53 |
| Future Ideas | 0/7 |

> **Já existem no mod, mas estão desmarcados no cartão:** `/back`, `/lixeira` (no mod, `/trash`)
> e os sete comandos de kit (veja [Kits](#kits)).
> Os comandos feitos usam nomes em inglês no mod: `/anvil` (bigorna), `/hat` (chapeu),
> `/compact` (compactar), `/colors` (cores), `/smelt` (derreter), `/ec` (echest),
> `/nightvision` (luz), `/repair` (reparar) e `/gm` (gamemode).

---

## Comandos que faltam

### Homes
- [ ] `/sethome` — Define uma home.
- [ ] `/home` — Teleporta até uma home.
- [ ] `/homes` — Mostra a lista de homes salvas.
- [ ] `/delhome` — Deleta uma home.
- [ ] `/publica` — Define uma home como pública.

### Warps
- [ ] `/setwarp` — Define uma warp.
- [ ] `/warp` — Teleporta até uma warp.
- [ ] `/warps` — Lista todas as warps disponíveis.
- [ ] `/delwarp` — Deleta uma warp.

### Kits
Feitos por menu (ainda desmarcados no cartão): `/kit view` (ou `/kit`, `/kits`) lista os kits e
mostra os itens de cada um (`/kit view [jogador] <kit>` abre direto a prévia, também para
outro jogador), `/kit claim [kit]` pega, `/kit give` dá por fora das regras, `/kit reset` zera o
cooldown, e o `/kit admin` cria, edita (itens, ícone, nome, cooldown, acesso) e exclui. Aparência e mensagens em `raven/essentials/kits.yml`.

- [x] `/criarkit` — Cria um kit. (`/kit admin` → Criar kit)
- [x] `/kit` — Pega um kit. (`/kit claim [kit]`)
- [x] `/kits` — Lista todos os kits disponíveis. (`/kit view`)
- [x] `/darkit` — Envia um kit para um player. (`/kit give <jogador> <kit>`)
- [x] `/verkit` — Visualiza os itens de um kit. (`/kit view` → clique no kit)
- [x] `/editarkit` — Edita um kit criado. (`/kit admin` → clique no kit)
- [x] `/delkit` — Deleta um kit. (`/kit admin` → kit → Excluir)

### Pedidos de teleporte
- [ ] `/tpa` — Envia uma solicitação de teleporte.
- [ ] `/tpaccept` — Aceita uma solicitação de teleporte.
- [ ] `/tpdeny` — Rejeita uma solicitação de teleporte.
- [ ] `/tpcancel` — Cancela uma solicitação de teleporte enviada.
- [ ] `/tptoggle` — Desativa o recebimento de solicitações de teleporte.
- [ ] `/tphere` — Teleporta um player até você.
- [ ] `/tpall` — Teleporta todos os players.

### Administração
- [ ] `/setspawn` — Define o spawn do servidor.
- [ ] `/invsee` — Abre o inventário de um player.
- [ ] `/sudo` — Executa um comando para um player.
- [ ] `/editaritem` — Edita o item que está na mão.
- [ ] `/executarsom` — Executa um som para um player ou para todo o servidor.

### Informação
- [ ] `/seen` — Visualiza as informações de um player.
- [ ] `/estatisticas` — Mostra as estatísticas de um player.

---

## Funções que faltam

### Bloqueios
- [ ] Bloquear os players de abrirem certos tipos de contêineres.
- [ ] Bloquear os players de caírem no void.
- [ ] Bloquear a cama.
- [ ] Bloquear comandos específicos.
- [ ] Bloquear a água de congelar.
- [ ] Bloquear o craft de certos itens.
- [ ] Bloquear a criação de portais.
- [ ] Bloquear o gelo e a neve de derreterem.
- [ ] Bloquear os zumbis e esqueletos de pegarem fogo com o sol.
- [ ] Bloquear o uso da NameTag nos mobs.
- [ ] Bloquear o uso de nicks impróprios (bloquear certas palavras no nick).
- [ ] Bloquear o player de passar para o outro lado da borda com ender pearl.
- [ ] Bloquear palavras de serem usadas em placas.
- [ ] Bloquear o SHIFT em certos contêineres.
- [ ] Bloquear a utilização de veículos.
- [ ] Bloquear subir em cima do teto do Nether.
- [ ] Bloquear o teleporte por portais.
- [ ] Bloquear o bug do duplo login.
- [ ] Bloquear as explosões de destruírem itens dropados no chão.
- [ ] Bloquear o dup do money (NaN e null).

### Desativar comportamentos do mundo
- [ ] Desativar a chuva.
- [ ] Desativar o ciclo do dia.
- [ ] Desativar o dano do Ender Dragon.
- [ ] Desativar o dano do Wither (completamente).
- [ ] Desativar o flow Y da água (impede a água de escorrer para baixo).
- [ ] Desativar a fome.
- [ ] Desativar a mensagem de entrada, saída e de morte.
- [ ] Desativar o spawn de mobs naturais (só nasce com ovo, spawner ou comando).
- [ ] Desativar a propagação do fogo.
- [ ] Desativar a queda da areia e do cascalho.
- [ ] Desativar a queda da bigorna.
- [ ] Desativar a queda das folhas das árvores quebradas.
- [ ] Desativar as plantações de serem quebradas quando pisoteadas.
- [ ] Desativar o dano dos cactos.
- [ ] Desativar as bolas de fogo do Blaze e do Ghast.

### Sistemas
- [ ] Executar comandos quando o player entrar no servidor pela primeira vez.
- [ ] Deletar comandos do servidor.
- [ ] Administrar os plugins do servidor (Plugman).
- [ ] Bigornas infinitas (você repara na bigorna e ela não quebra).
- [ ] Cores nas placas.
- [ ] Cores nas bigornas.
- [ ] Cooldown para o uso da ender pearl.
- [ ] Não tomar dano por alguns segundos ao se teleportar.
- [ ] Limitar o número de players do servidor.
- [ ] Slots para VIPs (os sem VIP são kickados, igual ao Sky Survival).
- [ ] Não perder o XP ao morrer.
- [ ] MOTD de boas-vindas ao entrar no servidor.
- [ ] Title de boas-vindas ao entrar no servidor.
- [ ] Sistema de morte completo, com mensagem para quem matou, para quem morreu e broadcast.
- [ ] Dropar a cabeça do player ao morrer.
- [ ] Auto-anúncio que envia mensagens automaticamente no chat.
- [ ] Scoreboard (simples).
- [ ] Tablist.

---

## Future Ideas
- [ ] `/particular` — Define uma home como particular. (As homes já são particulares.)
- [ ] `/setmundovip` — Define o mundo VIP e o não VIP do servidor.
- [ ] `/mundovip` — Teleporta até o mundo VIP.
- [ ] `/system` — Comando geral do plugin, com diversas funções.
- [ ] `/alerta` — Envia um alerta para todos do servidor.
- [ ] `/sgive` — Envia geradores para um player.
- [ ] `/divulgar` — Divulga um link no chat.

---

## O que já ajuda
- **Homes e warps:** o `CoffeeLocation` e o `Locations` da CoffeeLib, mais o banco do mod.
- **`/tpa` e similares:** o argumento `ServerPlayer` e o `@OrSelf`, que já respeitam o vanish.
- **`/seen`:** o `PlayerRepository.findByName` e o histórico do jogador (`firstJoin`, `lastJoin`, `playTime`).
- **Contagem de homes por permissão:** o `permissions.number(...)` da CoffeeLib.
- **Menus de homes, warps e afins:** o pacote `kit/menu` (config de menu em YAML, páginas,
  placeholders, prompt por chat) serve de modelo.
