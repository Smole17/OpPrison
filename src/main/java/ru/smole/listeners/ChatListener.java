package ru.smole.listeners;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.smole.OpPrison;
import ru.smole.data.PlayerData;
import ru.smole.utils.StringUtils;
import ru.xfenilafs.core.ApiManager;
import ru.xfenilafs.core.util.ChatUtil;

import java.util.List;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String msg = e.getMessage();

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null) {
            if (msg.startsWith("!")) {
                Bukkit.getOnlinePlayers().forEach(players -> players.spigot().sendMessage(getComponent(player, getHand(msg, item))));
                return;
            }

            player.getLocation().getNearbyPlayers(200).forEach(players -> players.spigot().sendMessage(getComponent(player, getHand(msg, item))));
        }

        e.setCancelled(true);
    }

    public BaseComponent[] getComponent(Player player, String msg) {
        String name = player.getName();
        PlayerData playerData = OpPrison.getInstance().getPlayerDataManager().getPlayerDataMap().get(name);

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(name);
        QueryOptions userCtx = api.getContextManager().getQueryOptions(player);

        String prefix = user.getCachedData().getMetaData(userCtx).getPrefix();
        String chat = String.format("§7[§b%s§7] §7[%s§7] %s §f%s§7: §f%s",
                StringUtils.formatDouble(playerData.getPrestige()), prefix, playerData.getRank().getName(), name, msg);

        return ChatUtil.newBuilder()
                .setText(chat)
                .setHoverEvent(HoverEvent.Action.SHOW_TEXT, "§fНажмите, чтобы узнать кол-во токенов")
                .setClickEvent(ClickEvent.Action.RUN_COMMAND, "/token " + name)
                .build();
    }

    public String getHand(String msg, ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return msg;

        List<String> lore = meta.getLore();

        if (msg.contains("#рука")) {
            int amount = item.getAmount();
            String text_peace = amount == 1 ? "" : " §fx" + amount;
            String text =
                    item.getType() == Material.AIR
                            ? "null" : meta.getDisplayName()
                            + text_peace;

            String show = "null";

            for (String s : lore) {
                show = show + "\n" + s;
            }

            BaseComponent[] itemComponent = ChatUtil.newBuilder()
                    .setText(item.getType() == Material.AIR ? "null" : meta.getDisplayName() + text)
                    .setHoverEvent(HoverEvent.Action.SHOW_TEXT, show)
                    .build();

            msg = msg.replace("#рука", new TextComponent(itemComponent).toLegacyText());
        }

        return msg;
    }
}
