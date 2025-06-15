package org.debianrose.TimeA;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockEnchantingTable;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.TextFormat;

public class Main extends PluginBase implements Listener {

    private final Item TIME_ARTIFACT;

    public Main() {
        // Создаем кастомный артефакт
        TIME_ARTIFACT = Item.get(ItemID.NETHER_STAR);
        TIME_ARTIFACT.setCustomName(TextFormat.GOLD + "Артефакт Времени");
        TIME_ARTIFACT.setLore(
            TextFormat.GRAY + "Правый клик - сменить время",
            TextFormat.GRAY + "Шифт+ПКМ - ускорение времени"
        );
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(TextFormat.GREEN + "TimeArtifact активирован!");
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        if (message.equalsIgnoreCase("Averus Craftum")) {
            Block block = player.getTargetBlock(5);
            
            if (block instanceof BlockEnchantingTable) {
                craftArtifact(player);
                event.setCancelled(true);
            }
        }
    }

    private void craftArtifact(Player player) {
        Item hand = player.getInventory().getItemInHand();
        
        // Проверяем что в руке звезда Низира и есть 2 железных слитка в инвентаре
        if (hand.getId() == ItemID.NETHER_STAR) {
            if (player.getInventory().contains(Item.get(ItemID.IRON_INGOT, 0, 2))) {
                // Убираем материалы
                player.getInventory().removeItem(Item.get(ItemID.NETHER_STAR, 1));
                player.getInventory().removeItem(Item.get(ItemID.IRON_INGOT, 2));
                
                // Даем артефакт
                player.getInventory().addItem(TIME_ARTIFACT);
                player.sendMessage(TextFormat.GOLD + "Вы создали Артефакт Времени!");
            } else {
                player.sendMessage(TextFormat.RED + "Нужно иметь 2 железных слитка в инвентаре!");
            }
        } else {
            player.sendMessage(TextFormat.RED + "Держите звезду Низира в руке!");
        }
    }

    @EventHandler
    public void onArtifactUse(PlayerInteractEvent event) {
        Item item = event.getItem();
        Player player = event.getPlayer();
        
        if (item != null && item.equals(TIME_ARTIFACT, true, false)) {
            event.setCancelled(true);
            
            if (player.isSneaking()) {
                accelerateTime(player);
            } else {
                toggleDayNight(player);
            }
        }
    }

    private void toggleDayNight(Player player) {
        long time = player.getLevel().getTime();
        
        if (time < 13000) {
            player.getLevel().setTime(14000); // Ночь
            player.sendMessage(TextFormat.BLUE + "Вы установили ночь");
        } else {
            player.getLevel().setTime(0); // Утро
            player.sendMessage(TextFormat.YELLOW + "Вы установили день");
        }
    }

    private void accelerateTime(Player player) {
        player.sendMessage(TextFormat.LIGHT_PURPLE + "Время ускоряется...");
        
        getServer().getScheduler().scheduleRepeatingTask(this, new Task() {
            int ticks = 0;
            
            @Override
            public void onRun(int currentTick) {
                if (ticks++ >= 100) {
                    player.sendMessage(TextFormat.GREEN + "Время восстановлено");
                    this.cancel();
                    return;
                }
                
                player.getLevel().setTime(player.getLevel().getTime() + 100);
            }
        }, 1);
    }
}