package io.github.mooy1.infinitylib.common;

import javax.annotation.ParametersAreNonnullByDefault;

import com.tcoded.folialib.FoliaLib;

import lombok.experimental.UtilityClass;

import org.bukkit.Bukkit;

import io.github.mooy1.infinitylib.core.AbstractAddon;

/**
 * A class for scheduling tasks
 *
 * @author Mooy1
 */
@UtilityClass
@ParametersAreNonnullByDefault
public final class Scheduler {
    public static FoliaLib getFoliaLib() {
        return new FoliaLib(AbstractAddon.instance());
    }

    public static void run(Runnable runnable) {
        getFoliaLib().getScheduler().runNextTick(wrappedTask -> runnable.run());
    }

    public static void runAsync(Runnable runnable) {
        getFoliaLib().getScheduler().runAsync(wrappedTask -> runnable.run());
    }

    public static void run(int delayTicks, Runnable runnable) {
        getFoliaLib().getScheduler().runLater(runnable, delayTicks);
    }

    public static void runAsync(int delayTicks, Runnable runnable) {
        getFoliaLib().getScheduler().runLaterAsync(runnable, delayTicks);
    }

    public static void repeat(int intervalTicks, Runnable runnable) {
        repeat(intervalTicks, 1, runnable);
    }

    public static void repeatAsync(int intervalTicks, Runnable runnable) {
        repeatAsync(intervalTicks, 1, runnable);
    }

    public static void repeat(int intervalTicks, int delayTicks, Runnable runnable) {
        getFoliaLib().getScheduler().runTimer(runnable, delayTicks, Math.max(1, intervalTicks));
    }

    public static void repeatAsync(int intervalTicks, int delayTicks, Runnable runnable) {
        getFoliaLib().getScheduler().runTimerAsync(runnable, delayTicks, Math.max(1, intervalTicks));
    }

}
