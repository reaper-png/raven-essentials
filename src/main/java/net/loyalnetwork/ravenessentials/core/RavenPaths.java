package net.loyalnetwork.ravenessentials.core;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/** Onde o mod guarda seus arquivos (database.yml, kits.yml, o banco SQLite). */
public final class RavenPaths {

    public static final Path ROOT = FMLPaths.GAMEDIR.get().resolve("raven/essentials");

    private RavenPaths() {
    }
}
