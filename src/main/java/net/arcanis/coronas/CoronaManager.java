package net.arcanis.coronas;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.*;

public class CoronaManager {

    private final ArcanisCoronas plugin;
    private final Map<String, String> lideresActuales = new HashMap<>();
    private BukkitTask tarea;
    private BukkitTask tareaRecompensa;
    private File archivoData;

    public CoronaManager(ArcanisCoronas plugin) {
        this.plugin = plugin;
        archivoData = new File(plugin.getDataFolder(), "data.yml");
    }

    public void iniciar() {
        cargarDatos();
        long intervalo = plugin.getConfig().getLong("check-interval", 60) * 20L;
        tarea = Bukkit.getScheduler().runTaskTimer(plugin, this::verificarTops, intervalo, intervalo);
        long unDia = 20L * 60 * 60 * 24;
        tareaRecompensa = Bukkit.getScheduler().runTaskTimer(plugin, this::darRecompensasDiarias, unDia, unDia);
    }

    public void verificarTops() {
        ConfigurationSection coronas = plugin.getConfig().getConfigurationSection("coronas");
        if (coronas == null) return;

        Player jugadorOnline = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (jugadorOnline == null) return;

        for (String board : coronas.getKeys(false)) {
            // Si tiene placeholder-custom, usarlo; si no, construir el de ajlb
            String placeholder;
            String customPlaceholder = coronas.getString(board + ".placeholder-custom");
            if (customPlaceholder != null && !customPlaceholder.isEmpty()) {
                placeholder = customPlaceholder;
            } else {
                placeholder = "%ajlb_lb_" + board + "_1_alltime_name%";
            }

            String nuevoLider = PlaceholderAPI.setPlaceholders(jugadorOnline, placeholder);

            if (nuevoLider == null || nuevoLider.isEmpty()
                    || nuevoLider.equals(placeholder)
                    || nuevoLider.equalsIgnoreCase("loading")
                    || nuevoLider.equalsIgnoreCase("Board does not exist")
                    || nuevoLider.equalsIgnoreCase("---")) continue;

            String liderAnterior = lideresActuales.get(board);

            if (!nuevoLider.equals(liderAnterior)) {
                if (liderAnterior != null && !liderAnterior.isEmpty()) {
                    quitarTag(liderAnterior, coronas.getString(board + ".tag-id"));
                    anunciarCambio(board, nuevoLider, liderAnterior, coronas);
                }
                darTag(nuevoLider, coronas.getString(board + ".tag-id"), coronas.getString(board + ".tag"));
                lideresActuales.put(board, nuevoLider);
                guardarDatos();
            }
        }
    }

private void darTag(String jugador, String tagId, String tagTexto) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "alonsotags set " + jugador + " " + tagId + " &f");
    }

    private void quitarTag(String jugador, String tagId) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "alonsotags set " + jugador + " null &f");
    }

    private void anunciarCambio(String board, String nuevo, String anterior, ConfigurationSection coronas) {
        String nombreCorona = coronas.getString(board + ".nombre", board);
        String emoji = coronas.getString(board + ".emoji", "👑");
        String color = coronas.getString(board + ".color", "&6");
        String mensaje = plugin.getConfig().getString("anuncio", "")
            .replace("%nuevo%", nuevo)
            .replace("%anterior%", anterior)
            .replace("%corona%", color + emoji + " " + nombreCorona + "&r");

        mensaje = mensaje.replace("&", "§");

        for (String linea : mensaje.split("\n")) {
            Bukkit.broadcastMessage(linea);
        }

        String sonidoNombre = plugin.getConfig().getString("sonido", "ENTITY_ENDER_DRAGON_GROWL");
        try {
            Sound sonido = Sound.valueOf(sonidoNombre);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), sonido, 1.0f, 1.0f);
            }
        } catch (Exception ignored) {}
    }

    private void darRecompensasDiarias() {
        ConfigurationSection coronas = plugin.getConfig().getConfigurationSection("coronas");
        if (coronas == null) return;

        for (String board : coronas.getKeys(false)) {
            String lider = lideresActuales.get(board);
            if (lider == null || lider.isEmpty()) continue;

            int recompensa = coronas.getInt(board + ".recompensa-diaria", 500);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + lider + " " + recompensa);

            Player p = Bukkit.getPlayerExact(lider);
            if (p != null) {
                String nombreCorona = coronas.getString(board + ".nombre", board);
                String color = coronas.getString(board + ".color", "&6");
                p.sendMessage("§6✦ §fRecibiste §a" + recompensa + " ArCoins §fpor mantener la "
                    + color.replace("&", "§") + nombreCorona + "§f!");
            }
        }
    }

    public void mostrarStatus(CommandSender sender) {
        sender.sendMessage("§6=== Coronas Actuales ===");
        if (lideresActuales.isEmpty()) {
            sender.sendMessage("§7Ninguna corona asignada aún.");
            return;
        }
        for (Map.Entry<String, String> entry : lideresActuales.entrySet()) {
            sender.sendMessage("§e" + entry.getKey() + " §7→ §f" + entry.getValue());
        }
    }

    public void recargar() {
        if (tarea != null) tarea.cancel();
        if (tareaRecompensa != null) tareaRecompensa.cancel();
        iniciar();
    }

    public void guardarDatos() {
        try {
            plugin.getDataFolder().mkdirs();
            Properties props = new Properties();
            props.putAll(lideresActuales);
            try (FileOutputStream fos = new FileOutputStream(archivoData)) {
                props.store(fos, "ArcanisCoronas - lideres actuales");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error guardando datos: " + e.getMessage());
        }
    }

    private void cargarDatos() {
        if (!archivoData.exists()) return;
        try (FileInputStream fis = new FileInputStream(archivoData)) {
            Properties props = new Properties();
            props.load(fis);
            for (String key : props.stringPropertyNames()) {
                lideresActuales.put(key, props.getProperty(key));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error cargando datos: " + e.getMessage());
        }
    }
}
