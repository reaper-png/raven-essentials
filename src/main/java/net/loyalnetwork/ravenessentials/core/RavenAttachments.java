package net.loyalnetwork.ravenessentials.core;

import com.mojang.serialization.Codec;
import net.loyalnetwork.ravenessentials.RavenEssentials;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** Dados salvos no jogador (sobrevivem a relog, morte e restart). */
public final class RavenAttachments {

    public static final DeferredRegister<AttachmentType<?>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RavenEssentials.MODID);

    public static final Supplier<AttachmentType<Boolean>> VANISHED = flag("vanished");
    public static final Supplier<AttachmentType<Boolean>> GOD = flag("god");
    public static final Supplier<AttachmentType<Boolean>> FLY = flag("fly");
    public static final Supplier<AttachmentType<Boolean>> TP_DISABLED = flag("tp_disabled");

    private RavenAttachments() {
    }

    /** Liga/desliga salvo no jogador, falso por padrão e mantido na morte. */
    private static Supplier<AttachmentType<Boolean>> flag(String name) {
        return REGISTER.register(name,
                () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).copyOnDeath().build());
    }
}
