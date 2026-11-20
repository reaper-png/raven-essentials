package net.loyalnetwork.ravenessentials.kit.infrastructure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.loyalnetwork.ravenessentials.kit.domain.KitItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Itens em texto (SNBT, o NBT que se lê), para o banco. Precisa dos registros do servidor
 * (encantamentos, componentes): use na thread do servidor, com ele rodando.
 * <p>
 * Um item que não existe mais (mod removido) volta vazio, com aviso no log, em vez de
 * derrubar o kit inteiro.
 */
public final class ItemCodec {

    private static final Logger LOGGER = Logger.getLogger("RavenEssentials.kits");
    private static final String ITEMS = "items";
    private static final String SLOT = "Slot";

    private final Supplier<HolderLookup.Provider> registries;

    public ItemCodec(Supplier<HolderLookup.Provider> registries) {
        this.registries = registries;
    }

    public String encode(ItemStack stack) {
        return stack.save(registries.get()).toString();
    }

    public ItemStack decode(String snbt) {
        return parse(read(snbt));
    }

    public String encode(KitItems items) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < items.span(); slot++) {
            ItemStack stack = items.at(slot);
            if (!stack.isEmpty()) {
                CompoundTag tag = (CompoundTag) stack.save(registries.get(), new CompoundTag());
                tag.putByte(SLOT, (byte) slot);
                list.add(tag);
            }
        }
        CompoundTag root = new CompoundTag();
        root.put(ITEMS, list);
        return root.toString();
    }

    public KitItems decode(String snbt, String owner) {
        ListTag list = read(snbt).getList(ITEMS, Tag.TAG_COMPOUND);
        List<ItemStack> slots = new ArrayList<>(KitItems.CAPACITY);
        for (int i = 0; i < KitItems.CAPACITY; i++) {
            slots.add(ItemStack.EMPTY);
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            int slot = tag.getByte(SLOT);
            if (slot >= 0 && slot < KitItems.CAPACITY) {
                slots.set(slot, parseOrWarn(tag, owner));
            }
        }
        return KitItems.of(slots);
    }

    private ItemStack parseOrWarn(CompoundTag tag, String owner) {
        ItemStack stack = parse(tag);
        if (stack.isEmpty()) {
            LOGGER.warning("Item ignorado no kit " + owner + " (não existe mais?): " + tag);
        }
        return stack;
    }

    private ItemStack parse(CompoundTag tag) {
        return ItemStack.parse(registries.get(), tag).orElse(ItemStack.EMPTY);
    }

    private static CompoundTag read(String snbt) {
        try {
            return TagParser.parseTag(snbt);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("SNBT inválido no banco: " + e.getMessage(), e);
        }
    }
}
