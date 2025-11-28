package me.kall.savethehorse;

import me.kall.duplicationless.util.Executor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(value = SaveTheHorse.MOD_ID)
@Mod.EventBusSubscriber(modid = SaveTheHorse.MOD_ID)
public final class SaveTheHorse {
    public static final String MOD_ID = "savethehorse";
    public static final Logger LOGGER = LogManager.getLogger(SaveTheHorse.class);

    private static final double[] OFFSETS = {
            0, 0,
            -0.4, 0,
            0.4, 0,
            0, -0.4,
            0, 0.4,
            -0.4, -0.4,
            -0.4, 0.4,
            0.4, -0.4,
            0.4, 0.4,
            -0.3, -0.2,
            0.2, 0.3,
            -0.25, 0.25
    };

    @SubscribeEvent
    public static void onHorseSpawn(LivingSpawnEvent.@NotNull CheckSpawn event) {
        if (!(event.getEntity() instanceof AbstractHorse horse)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        double x = event.getX();
        double y = event.getY();
        double z = event.getZ();

        Runnable task = new Runnable() {
            private int tries;
            public void run() {
                if (tries >= 20) return;
                if (level.isLoaded(horse.blockPosition()) && level.getEntity(horse.getId()) != null) {
                    if (isSafe(horse, level, x, y, z)) return;

                    for (int i = 0; i < OFFSETS.length; i += 2) {
                        double nextX = x + OFFSETS[i];
                        double nextZ = z + OFFSETS[i + 1];

                        if (isSafe(horse, level, nextX, y, nextZ)) {
                            horse.setPos(nextX, y, nextZ);
                            LOGGER.warn("Horse at [{}, {}, {}] is saved from inWall damage~", nextX, y, nextZ);
                            return;
                        }
                    }
                } else {
                    tries++;
                    Executor.runAfter(1, this);
                }
            }
        };

        Executor.runAfter(1, task);
    }

    private static boolean isSafe(@NotNull AbstractHorse horse, @NotNull Level level, double x, double y, double z) {
        horse.setPos(x, y, z);
        AABB box = horse.getBoundingBox();
        return level.noCollision(horse, box);
    }
}