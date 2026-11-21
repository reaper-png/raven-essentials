package net.loyalnetwork.ravenessentials.home.infrastructure;

import net.loyalnetwork.coffeelib.api.scheduler.Scheduler;
import net.loyalnetwork.ravenessentials.database.PlayerEntity;
import net.loyalnetwork.ravenessentials.database.PlayerRepository;
import net.loyalnetwork.ravenessentials.home.application.PlayerLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** {@link PlayerLookup}: quem está online pelo nome; senão, o último nome salvo em {@code players}. */
public final class StoredPlayerLookup implements PlayerLookup {

    private final PlayerRepository players;
    private final Scheduler scheduler;

    public StoredPlayerLookup(PlayerRepository players, Scheduler scheduler) {
        this.players = players;
        this.scheduler = scheduler;
    }

    @Override
    public CompletableFuture<Optional<UUID>> uuidOf(String name) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayer online = server == null ? null : server.getPlayerList().getPlayerByName(name);
        if (online != null) {
            return CompletableFuture.completedFuture(Optional.of(online.getUUID()));
        }
        return scheduler.onMain(players.findByName(name))
                .thenApply(found -> found.map(PlayerEntity::getId).map(UUID::fromString));
    }
}
