package net.loyalnetwork.ravenessentials.mixin.vanish;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.loyalnetwork.ravenessentials.vanish.Vanish;
import net.minecraft.Util;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Lista de servidores (ping): o número de online e a amostra de nomes não contam quem está em vanish. */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    @Final
    private RandomSource random;

    @Shadow
    public abstract PlayerList getPlayerList();

    @Shadow
    public abstract int getMaxPlayers();

    @Shadow
    public abstract boolean hidesOnlinePlayers();

    @Inject(method = "buildPlayerStatus", at = @At("HEAD"), cancellable = true)
    private void ravenessentials$hideVanishedFromStatus(CallbackInfoReturnable<ServerStatus.Players> cir) {
        List<ServerPlayer> list = Vanish.visiblePlayers(getPlayerList());

        if (list.size() == getPlayerList().getPlayerCount()) return; // ninguém em vanish: segue o vanilla

        int max = getMaxPlayers();
        if (hidesOnlinePlayers()) {
            cir.setReturnValue(new ServerStatus.Players(max, list.size(), List.of()));
            return;
        }

        int count = Math.min(list.size(), 12);
        ObjectArrayList<GameProfile> sample = new ObjectArrayList<>(count);
        int start = Mth.nextInt(random, 0, list.size() - count);

        for (int i = 0; i < count; i++) {
            ServerPlayer player = list.get(start + i);
            sample.add(player.allowsListing() ? player.getGameProfile() : MinecraftServer.ANONYMOUS_PLAYER_PROFILE);
        }

        Util.shuffle(sample, random);
        cir.setReturnValue(new ServerStatus.Players(max, list.size(), sample));
    }
}
