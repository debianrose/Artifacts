package org.debianrose.DebSit;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockSlab;
import cn.nukkit.block.BlockStairs;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityArmorStand;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerToggleSneakEvent;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends PluginBase implements Listener {

    private final Map<UUID, EntityArmorStand> sittingPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SitPlugin enabled!");
    }

    @Override
    public void onDisable() {
        for (EntityArmorStand stand : sittingPlayers.values()) {
            if (stand != null) {
                stand.kill();
            }
        }
        sittingPlayers.clear();
        getLogger().info("SitPlugin disabled");
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking() || sittingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        if (player.getPitch() > 45) {
            Location loc = player.getLocation();
            Block blockBelow = player.getLevel().getBlock(
                (int) loc.x, 
                (int) (loc.y - 0.5), 
                (int) loc.z
            );
            
            if (isSittable(blockBelow)) {
                sitPlayer(player, blockBelow);
            }
        }
    }

    private boolean isSittable(Block block) {
        return block instanceof BlockSlab || block instanceof BlockStairs;
    }

    private void sitPlayer(Player player, Block block) {
        if (sittingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        Location sitLocation = new Location(
            block.x + 0.5,
            block.y + getSitHeight(block),
            block.z + 0.5,
            player.getYaw(),
            0,
            player.getLevel()
        );

        EntityArmorStand armorStand = new EntityArmorStand(sitLocation.getChunk(), 
            Entity.getDefaultNBT(sitLocation));
        armorStand.setNameTag("");
        armorStand.setNameTagVisible(false);
        armorStand.spawnToAll();
        
        player.teleport(sitLocation);
        sittingPlayers.put(player.getUniqueId(), armorStand);
        
        // Schedule task to check if player should stand up
        getServer().getScheduler().scheduleRepeatingTask(new Task() {
            @Override
            public void onRun(int currentTick) {
                if (!player.isSneaking() || !player.isOnline()) {
                    standUp(player);
                    this.cancel();
                }
            }
        }, 20);
    }

    private double getSitHeight(Block block) {
        if (block instanceof BlockSlab) {
            return 0.25;
        } else if (block instanceof BlockStairs) {
            return 0.5;
        }
        return 0.5;
    }

    private void standUp(Player player) {
        EntityArmorStand armorStand = sittingPlayers.remove(player.getUniqueId());
        if (armorStand != null) {
            armorStand.kill();
            player.teleport(player.getLocation().add(0, 0.3, 0));
        }
    }
}