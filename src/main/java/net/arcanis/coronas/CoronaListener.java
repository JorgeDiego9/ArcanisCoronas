package net.arcanis.coronas;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CoronaListener implements Listener {

    private final ArcanisCoronas plugin;

    public CoronaListener(ArcanisCoronas plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Forzar revisión cuando alguien entra al servidor
        // por si cambió el top mientras estaba offline
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getCoronaManager().verificarTops();
        }, 100L); // 5 segundos después de entrar
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Revisar tops cuando alguien sale
        // por si era el único online y ahora hay otro
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getCoronaManager().verificarTops();
        }, 20L); // 1 segundo después de salir
    }
}
