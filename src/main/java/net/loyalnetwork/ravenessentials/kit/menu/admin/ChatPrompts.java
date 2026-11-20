package net.loyalnetwork.ravenessentials.kit.menu.admin;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.coffeelib.api.scheduler.Task;
import net.loyalnetwork.ravenessentials.util.Messages;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Pergunta algo pelo chat: fecha o menu, a próxima mensagem do jogador vira a resposta e
 * não aparece para ninguém. {@code cancelar}, sair do servidor ou um minuto sem responder
 * desistem. Registre a instância no barramento do NeoForge.
 */
public final class ChatPrompts {

    private static final String CANCEL = "cancelar";
    private static final long TIMEOUT_TICKS = 60 * 20;

    private final Scheduler scheduler;
    private final Map<UUID, Prompt> pending = new ConcurrentHashMap<>();

    public ChatPrompts(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /** Pergunta {@code question}; {@code answer} recebe o texto, {@code cancelled} roda se desistir. */
    public void ask(ServerPlayer player, String question, Consumer<String> answer, Runnable cancelled) {
        player.closeContainer();
        UUID id = player.getUUID();
        Prompt prompt = new Prompt(answer, cancelled);
        Prompt replaced = pending.put(id, prompt);
        if (replaced != null) {
            replaced.expiry().cancel();
        }
        prompt.expireWith(scheduler.later(TIMEOUT_TICKS, () -> expire(player, prompt)));
        player.sendSystemMessage(Messages.info(question));
        player.sendSystemMessage(Messages.info("Digite \"" + CANCEL + "\" para desistir."));
    }

    private void expire(ServerPlayer player, Prompt prompt) {
        if (pending.remove(player.getUUID(), prompt) && !player.hasDisconnected()) {
            player.sendSystemMessage(Messages.error("Tempo esgotado."));
            prompt.cancelled().run();
        }
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        Prompt prompt = pending.remove(event.getPlayer().getUUID());
        if (prompt == null) {
            return;
        }
        event.setCanceled(true);
        prompt.expiry().cancel();
        String text = event.getRawText().trim();
        scheduler.onMain(() -> respond(prompt, text));
    }

    private static void respond(Prompt prompt, String text) {
        if (text.equalsIgnoreCase(CANCEL)) {
            prompt.cancelled().run();
            return;
        }
        prompt.answer().accept(text);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Prompt prompt = pending.remove(event.getEntity().getUUID());
        if (prompt != null) {
            prompt.expiry().cancel();
        }
    }

    private static final class Prompt {
        private final Consumer<String> answer;
        private final Runnable cancelled;
        private Task expiry;

        private Prompt(Consumer<String> answer, Runnable cancelled) {
            this.answer = answer;
            this.cancelled = cancelled;
        }

        void expireWith(Task task) {
            this.expiry = task;
        }

        Consumer<String> answer() {
            return answer;
        }

        Runnable cancelled() {
            return cancelled;
        }

        Task expiry() {
            return expiry;
        }
    }
}
