package net.loyalnetwork.ravenessentials.database;


import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import net.loyalnetwork.coffeelib.database.location.CoffeeLocation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name = "players")
@NoArgsConstructor
public class PlayerEntity {
    @Id
    private String id;
    private String name;
    private String lastIp;

    // ---- histórico de sessões ----

    private Instant firstJoin;
    private Instant lastJoin;
    private Instant lastQuit;
    /** Tempo total jogado, em segundos. Soma a sessão em cada saída. */
    private long playTime;
    private int joinCount;

    // ---- histórico de IPs e nomes ----
    // Map de valor -> Usage: vira uma tabela (player_id, chave, first_seen, last_seen), com a chave
    // única por jogador. EAGER: as entidades saem do repository sem sessão, e uma coleção LAZY lida
    // depois disso lançaria LazyInitializationException.

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_ips", joinColumns = @JoinColumn(name = "player_id"))
    @MapKeyColumn(name = "ip")
    private Map<String, Usage> ips = new HashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_names", joinColumns = @JoinColumn(name = "player_id"))
    @MapKeyColumn(name = "name")
    private Map<String, Usage> names = new HashMap<>();

    // ---- localizações (para o /back) ----
    // O CoffeeLocation da lib, duas vezes na mesma tabela: cada campo precisa de um nome de
    // coluna próprio. Os nomes são os de antes (last_dimension...), então o banco não muda.

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "world", column = @Column(name = "last_dimension")),
            @AttributeOverride(name = "x", column = @Column(name = "last_x")),
            @AttributeOverride(name = "y", column = @Column(name = "last_y")),
            @AttributeOverride(name = "z", column = @Column(name = "last_z")),
            @AttributeOverride(name = "yaw", column = @Column(name = "last_yaw")),
            @AttributeOverride(name = "pitch", column = @Column(name = "last_pitch")),
            @AttributeOverride(name = "savedAt", column = @Column(name = "last_saved_at")),
    })
    private CoffeeLocation lastLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "world", column = @Column(name = "death_dimension")),
            @AttributeOverride(name = "x", column = @Column(name = "death_x")),
            @AttributeOverride(name = "y", column = @Column(name = "death_y")),
            @AttributeOverride(name = "z", column = @Column(name = "death_z")),
            @AttributeOverride(name = "yaw", column = @Column(name = "death_yaw")),
            @AttributeOverride(name = "pitch", column = @Column(name = "death_pitch")),
            @AttributeOverride(name = "savedAt", column = @Column(name = "death_saved_at")),
    })
    private CoffeeLocation deathLocation;

    public PlayerEntity(String id) {
        this.id = id;
    }

    /** Uma entrada: conta a sessão e guarda nome e IP no histórico. */
    public void onLogin(String name, String ip, Instant now) {
        if (firstJoin == null) firstJoin = now;
        lastJoin = now;
        joinCount++;

        this.name = name;
        this.lastIp = ip;
        names.computeIfAbsent(name, key -> new Usage(now)).seen(now);
        ips.computeIfAbsent(ip, key -> new Usage(now)).seen(now);
    }

    /** Para onde o /back leva: o mais recente entre o último teleporte e a última morte, ou null. */
    public CoffeeLocation backLocation() {
        if (lastLocation == null) return deathLocation;
        if (deathLocation == null) return lastLocation;
        return deathLocation.savedAt().isAfter(lastLocation.savedAt()) ? deathLocation : lastLocation;
    }

    /** Uma saída: soma o tempo da sessão. Ignora uma saída repetida (sem entrada no meio). */
    public void onLogout(Instant now) {
        if (lastJoin != null && (lastQuit == null || lastQuit.isBefore(lastJoin))) {
            playTime += Math.max(0, Duration.between(lastJoin, now).getSeconds());
        }
        lastQuit = now;
    }
}
