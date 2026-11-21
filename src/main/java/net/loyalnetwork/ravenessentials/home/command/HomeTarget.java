package net.loyalnetwork.ravenessentials.home.command;

import net.loyalnetwork.ravenessentials.home.domain.HomeName;

/**
 * O que se digitou no {@code /home}: o nome de uma home sua ({@code casa}) ou de outro
 * jogador ({@code dono:casa}; {@code dono:} é a {@code home} dele).
 */
sealed interface HomeTarget {

    char SEPARATOR = ':';

    record Own(HomeName name) implements HomeTarget {
    }

    record Other(String owner, HomeName name) implements HomeTarget {
    }

    /** @throws net.loyalnetwork.ravenessentials.home.domain.HomeException com um nome inválido */
    static HomeTarget parse(String input) {
        int separator = input.indexOf(SEPARATOR);
        if (separator < 0) {
            return new Own(HomeName.parse(input));
        }
        String owner = input.substring(0, separator).trim();
        String name = input.substring(separator + 1);
        return new Other(owner, name.isBlank() ? HomeName.DEFAULT : HomeName.parse(name));
    }
}
