package net.loyalnetwork.ravenessentials.database;


import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "players")
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEntity {
    @Id
    private String id;
    private String name;
    private String lastIp;

    // Um Set de valores simples não cabe numa coluna: vira uma tabela própria,
    // player_used_ips (player_id, ip), uma linha por IP.
    // EAGER: as entidades saem do repository já sem sessão, e uma coleção LAZY
    // lida depois disso lançaria LazyInitializationException.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_used_ips", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "ip", nullable = false)
    private Set<String> usedIps = new HashSet<>();

    /** Marca {@code ip} como o último usado e o guarda no histórico (o Set ignora repetidos). */
    public void recordIp(String ip) {
        this.lastIp = ip;
        this.usedIps.add(ip);
    }
}
