package org.royaldev.royalcommands.wrappers;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.configuration.PConfManager;
import org.royaldev.royalcommands.rcommands.CmdBack;

public class Teleporter {

    private final Entity teleportee;

    public Teleporter(final Entity teleportee) {
        this.teleportee = teleportee;
    }

    private void addBackLocation(final Entity entity) {
        if (!(entity instanceof Player)) return;
        final Player p = (Player) entity;
        CmdBack.addBackLocation(p, p.getLocation());
    }

    private void cancelWarmupTask() {
        final int warmupTaskID = this.getWarmupTaskID();
        if (warmupTaskID == -1) return;
        this.getTeleportee().getServer().getScheduler().cancelTask(warmupTaskID);
        this.setWarmupTaskID(-1);
    }

    private Location getSafeLocation(final Location location) {
        if (!Config.safeTeleport) return location;
        return RUtils.getSafeLocation(location);
    }

    private Sound getTeleportSound() {
        if (!Config.teleportSoundEnabled) return null;
        try {
            return Sound.valueOf(Config.teleportSoundName);
        } catch (final IllegalArgumentException e) {
            RoyalCommands.getInstance().getLogger().warning("A teleport sound was attempted, but teleport_sound.name was not a valid sound name!");
            return null;
        }
    }

    private Entity getVehicleToTeleport() {
        if (!Config.vehicleTeleportEnabled) return null;
        final Entity vehicle = this.getTeleportee().getVehicle();
        if (vehicle == null) return null;
        if (Config.vehicleTeleportVehicles && vehicle instanceof Vehicle) return vehicle;
        if (Config.vehicleTeleportAnimals && vehicle instanceof Animals) return vehicle;
        if (Config.vehicleTeleportPlayers && vehicle instanceof Player) return vehicle;
        return null;
    }

    private int getWarmupTaskID() {
        if (!(this.getTeleportee() instanceof Player)) return -1;
        final Player p = (Player) this.getTeleportee();
        final RPlayer rp = RPlayer.getRPlayer(p);
        return rp.getPConfManager().getInt("teleport_warmup_taskid", -1);
    }

    private void setWarmupTaskID(final int id) {
        if (!(this.getTeleportee() instanceof Player)) return;
        final Player p = (Player) this.getTeleportee();
        final RPlayer rp = RPlayer.getRPlayer(p);
        rp.getPConfManager().set("teleport_warmup_taskid", id);
    }

    private boolean handleWarmup(final Location location, final boolean silent) {
        if (Config.teleportWarmup <= 0 || !(this.getTeleportee() instanceof Player)) return false;
        final Player p = (Player) this.getTeleportee();
        if (RoyalCommands.getInstance().ah.isAuthorized(p, "rcmds.exempt.teleportwarmup")) return false;
        if (this.getWarmupTaskID() != -1) {
            this.cancelWarmupTask();
            if (this.warmupExpired()) return false;
        }
        return this.makeWarmupTask(p, location, silent);
    }

    private boolean isVanished(final Entity entity) {
        return entity instanceof Player && RoyalCommands.getInstance().isVanished((Player) entity);
    }

    private void loadChunk(final Chunk chunk) {
        if (!chunk.isLoaded()) chunk.load(true);
    }

    private boolean makeWarmupTask(final Player p, final Location location, final boolean silent) {
        if (Config.teleportWarmup <= 0) return false;
        final RPlayer rp = RPlayer.getRPlayer(p);
        final PConfManager pcm = rp.getPConfManager();
        final long time = System.currentTimeMillis();
        pcm.set("teleport_warmup", time);
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (!p.isOnline()) return;
                final RPlayer rp = RPlayer.getRPlayer(p);
                if (rp.getPConfManager().getInt("teleport_warmup", -1) == -1) {
                    Teleporter.this.cancelWarmupTask();
                    return;
                }
                if (!Teleporter.this.warmupExpired()) return;
                p.sendMessage(MessageColor.POSITIVE + "Teleporting...");
                final String error = Teleporter.this.teleport(location, silent);
                if (!error.isEmpty()) p.sendMessage(MessageColor.NEGATIVE + error);
                Teleporter.this.cancelWarmupTask();
            }
        };
        final int warmupTaskID = p.getServer().getScheduler().scheduleSyncRepeatingTask(RoyalCommands.getInstance(), r, 0, 10);
        this.setWarmupTaskID(warmupTaskID);
        p.sendMessage(MessageColor.POSITIVE + "Please wait " + MessageColor.NEUTRAL + Config.teleportWarmup + MessageColor.POSITIVE + " seconds for your teleport.");
        return warmupTaskID > 0;
    }

    private void mitigateFallDamage(final Entity e) {
        e.setFallDistance(0F);
        e.setVelocity(new Vector(0, 0, 0));
    }

    /**
     * Plays the configured teleport sound at a location.
     *
     * @param location Location to play sound at
     */
    private void playTeleportSound(final Location location) {
        if (location == null) throw new IllegalArgumentException("Location cannot be null!");
        final Sound sound = this.getTeleportSound();
        if (sound == null) return;
        location.getWorld().playSound(location, sound, Config.teleportSoundVolume, Config.teleportSoundPitch);
    }

    private boolean warmupExpired() {
        if (!(this.getTeleportee() instanceof Player)) return false;
        final RPlayer rp = RPlayer.getRPlayer((Player) this.getTeleportee());
        final long l = rp.getPConfManager().getLong("teleport_warmup", -1L);
        return l != -1 && l + Config.teleportWarmup * 1000L < System.currentTimeMillis();
    }

    public Entity getTeleportee() {
        return this.teleportee;
    }

    public String teleport(final Block block) {
        return this.teleport(block.getLocation(), false);
    }

    public String teleport(final Block block, final boolean silent) {
        return this.teleport(block.getLocation(), silent);
    }

    public String teleport(final Entity entity) {
        return this.teleport(entity.getLocation(), false);
    }

    public String teleport(final Entity entity, final boolean silent) {
        return this.teleport(entity.getLocation(), silent);
    }

    public String teleport(final Location location) {
        return this.teleport(location, false);
    }

    public String teleport(final Location location, final boolean silent) {
        if (this.handleWarmup(location, silent)) return "";
        final Entity vehicle = this.getVehicleToTeleport();
        final Entity teleportee = vehicle == null ? this.getTeleportee() : vehicle;
        final Location teleportTo = this.getSafeLocation(location);
        if (teleportTo == null) return "There is no ground below.";
        if (!silent) {
            this.addBackLocation(teleportee);
            if (!this.isVanished(this.getTeleportee())) { // use getTeleportee() because teleportee may not be Player
                this.playTeleportSound(teleportTo);
            }
        }
        this.loadChunk(teleportTo.getChunk());
        this.mitigateFallDamage(teleportee);
        teleportee.teleport(teleportTo);
        return "";
    }

}
