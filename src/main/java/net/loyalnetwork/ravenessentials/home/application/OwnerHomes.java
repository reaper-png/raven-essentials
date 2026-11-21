package net.loyalnetwork.ravenessentials.home.application;

import net.loyalnetwork.ravenessentials.home.domain.Home;

import java.util.List;
import java.util.UUID;

/** As homes de um jogador que quem pediu pode ver. */
public record OwnerHomes(UUID owner, String name, List<Home> homes) {
}
