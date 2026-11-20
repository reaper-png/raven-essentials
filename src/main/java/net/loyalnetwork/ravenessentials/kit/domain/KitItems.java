package net.loyalnetwork.ravenessentials.kit.domain;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * O que um kit entrega, na posição em que o admin montou (a prévia mostra igual).
 * Guarda cópias: nem quem cria nem quem lê altera o kit.
 */
public final class KitItems {

    /** Cinco fileiras de baú: o editor e a prévia têm esse tamanho. */
    public static final int CAPACITY = 45;

    public static final KitItems EMPTY = new KitItems(List.of());

    private final List<ItemStack> slots;

    private KitItems(List<ItemStack> slots) {
        this.slots = slots;
    }

    /** Os itens por posição; vazios são posições livres. */
    public static KitItems of(List<ItemStack> slots) {
        if (slots.size() > CAPACITY) {
            throw new KitException("Um kit cabe no máximo " + CAPACITY + " itens.");
        }
        List<ItemStack> copies = new ArrayList<>(slots.size());
        for (ItemStack stack : slots) {
            copies.add(stack.copy());
        }
        trimTrailingEmpty(copies);
        return new KitItems(Collections.unmodifiableList(copies));
    }

    private static void trimTrailingEmpty(List<ItemStack> slots) {
        while (!slots.isEmpty() && slots.getLast().isEmpty()) {
            slots.removeLast();
        }
    }

    /** Cópia da posição {@code slot}; vazio fora dos itens. */
    public ItemStack at(int slot) {
        return slot < slots.size() ? slots.get(slot).copy() : ItemStack.EMPTY;
    }

    /** Quantas posições o kit ocupa no editor (inclui buracos no meio). */
    public int span() {
        return slots.size();
    }

    /** Cópias só dos itens, sem os buracos: o que vai para o inventário. */
    public List<ItemStack> stacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : slots) {
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
    }

    public int count() {
        return stacks().size();
    }

    public boolean isEmpty() {
        return count() == 0;
    }
}
