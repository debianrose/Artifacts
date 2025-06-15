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
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends PluginBase implements Listener {

    private final Map<UUID, EntityArmorStand> sittingPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        this.getLogger().info("SitPlugin enabled - players can now sit on blocks!");
    }

    @Override
    public void onDisable() {
        for (EntityArmorStand stand : sittingPlayers.values()) {
            if (stand != null) {
                stand.close();
            }
        }
        sittingPlayers.clear();
        this.getLogger().info("SitPlugin disabled");
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking() || sittingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        if (player.getPitch() > 45) {
            Block blockBelow = player.getLevel().getBlock(player.getLocation().subtract(0, 0.5, 0));
            if (isSittable(blockBelow)) {
                sitPlayer(player, blockBelow);
            }
        }
    }

    private boolean isSittable(Block block) {
        return block instanceof BlockSlab || block instanceof BlockStairs;
    }

    private void sitPlayer(Player player, Block block) {
        if (sittingPlayers.containsKey(player.getUniqueId()) || player.isInsideOfWater() || player.isInsideOfLava()) {
            return;
        }

        Location sitLocation = block.getLocation().add(0.5, getSitHeight(block), 0.5);
        sitLocation.yaw = player.getYaw();
        sitLocation.pitch = 0;

        EntityArmorStand armorStand = new EntityArmorStand(sitLocation.getChunk(), 
                Entity.getDefaultNBT(sitLocation));
        armorStand.setMarker(true);
        armorStand.setInvisible(true);
        armorStand.setNameTagVisible(false);
        armorStand.spawnToAll();
        
        player.teleport(sitLocation);
        player.ride(armorStand);
        sittingPlayers.put(player.getUniqueId(), armorStand);
        
        this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, () -> {
            if (!player.isSneaking() || !player.isOnline() || player.getVehicle() == null) {
                standUp(player);
            }
        }, 20, 20);
    }

    private float getSitHeight(Block block) {
        if (block instanceof BlockSlab) {
            return 0.25f;
        } else if (block instanceof BlockStairs) {
            return 0.5f;
        }
        return 0.5f;
    }

    private void standUp(Player player) {
        EntityArmorStand armorStand = sittingPlayers.remove(player.getUniqueId());
        if (armorStand != null) {
            player.dismount();
            armorStand.close();
            Location standLocation = player.getLocation().add(0, 0.3, 0);
            player.teleport(standLocation);
        }
    }
}