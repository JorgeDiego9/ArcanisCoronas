package net.arcanis.coronas;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ArcanisCoronas extends JavaPlugin {

    private CoronaManager coronaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        coronaManager = new CoronaManager(this);
        coronaManager.iniciar();
        getLogger().info("✦ ArcanisCoronas activado correctamente!");
    }

    @Override
    public void onDisable() {
        if (coronaManager != null) {
            coronaManager.guardarDatos();
        }
        getLogger().info("✦ ArcanisCoronas desactivado.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("coronas")) return false;

        if (!sender.hasPermission("arcanis.coronas.admin")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6=== ArcanisCoronas ===");
            sender.sendMessage("§e/coronas reload §7- Recargar config");
            sender.sendMessage("§e/coronas check §7- Forzar revisión de tops");
            sender.sendMessage("§e/coronas status §7- Ver coronas actuales");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig();
                coronaManager.recargar();
                sender.sendMessage("§aConfiguración recargada.");
                break;
            case "check":
                coronaManager.verificarTops();
                sender.sendMessage("§aRevisión forzada completada.");
                break;
            case "status":
                coronaManager.mostrarStatus(sender);
                break;
            default:
                sender.sendMessage("§cComando desconocido.");
        }
        return true;
    }

    public CoronaManager getCoronaManager() {
        return coronaManager;
    }
}
