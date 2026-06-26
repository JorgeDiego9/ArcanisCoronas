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

        for (String board : coronas.getKeys(false)) {
            String placeholder = "%ajlb_lb_" + board + "_1_alltime_name%";
            Player jugadorOnline = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            String nuevoLider;
            if (jugadorOnline != null) {
                nuevoLider = PlaceholderAPI.setPlaceholders(jugadorOnline, placeholder);
            } else {
                continue;
            }

            if (nuevoLider == null || nuevoLider.isEmpty() || nuevoLider.equals(placeholder)) continue;

            String liderAnterior = lideresActuales.get(board);

            if (!nuevoLider.equals(liderAnterior)) {
                if (liderAnterior != null && !liderAnterior.isEmpty()) {
                    quitarTag(liderAnterior, coronas.getString(board + ".tag-id"));
                    anunciarCambio(board, nuevoLider,
