package dev.oum.oumlib.bridge.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public final class RegionBridge {

    private static boolean worldGuardChecked = false;
    private static boolean worldGuardAvailable = false;
    private static boolean townyChecked = false;
    private static boolean townyAvailable = false;
    private static boolean griefPreventionChecked = false;
    private static boolean griefPreventionAvailable = false;

    private RegionBridge() {
    }

    public static boolean isAvailable() {
        return hasWorldGuard() || hasTowny() || hasGriefPrevention();
    }

    public static boolean isPvPAllowed(@NonNull Location location) {
        return isPvPAllowed(location, null);
    }

    public static boolean isPvPAllowed(@NonNull Location location, @Nullable Player player) {
        if (hasWorldGuard()) {
            try {
                if (!checkWorldGuardPvP(location, player)) {
                    return false;
                }
            } catch (Throwable ignored) {
            }
        }

        if (hasTowny()) {
            try {
                if (!checkTownyPvP(location)) {
                    return false;
                }
            } catch (Throwable ignored) {
            }
        }

        if (hasGriefPrevention()) {
            try {
                if (!checkGriefPreventionPvP(location)) {
                    return false;
                }
            } catch (Throwable ignored) {
            }
        }

        return true;
    }

    public static boolean isSafeZone(@NonNull Location location) {
        return !isPvPAllowed(location, null);
    }

    /**
     * Gets all applicable WorldGuard region IDs at the specified location.
     *
     * @param location The location to query.
     * @return Set of WorldGuard region IDs, or an empty set if WorldGuard is unavailable or no regions exist.
     */
    public static @NonNull Set<String> getWorldGuardRegions(@NonNull Location location) {
        if (!hasWorldGuard()) {
            return Collections.emptySet();
        }
        try {
            WorldGuardContext ctx = createWorldGuardContext(location);
            Method getApplicable = ctx.query().getClass().getMethod("getApplicableRegions",
                    Class.forName("com.sk89q.worldedit.util.Location"));
            Object set = getApplicable.invoke(ctx.query(), ctx.adaptedLocation());

            if (set instanceof Iterable<?> iterable) {
                Set<String> regionNames = new HashSet<>();
                for (Object region : iterable) {
                    Method getId = region.getClass().getMethod("getId");
                    String id = (String) getId.invoke(region);
                    if (id != null) {
                        regionNames.add(id);
                    }
                }
                return Collections.unmodifiableSet(regionNames);
            }
        } catch (Throwable ignored) {
        }
        return Collections.emptySet();
    }

    /**
     * Gets the Towny town name at the specified location if one exists.
     *
     * @param location The location to query.
     * @return The town name, or null if no town exists at this location or Towny is not present.
     */
    public static @Nullable String getTownyTownName(@NonNull Location location) {
        if (!hasTowny()) {
            return null;
        }
        try {
            Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
            Object apiInstance = townyApiClass.getMethod("getInstance").invoke(null);
            Object town = townyApiClass.getMethod("getTown", Location.class).invoke(apiInstance, location);
            if (town != null) {
                Method getName = town.getClass().getMethod("getName");
                return (String) getName.invoke(town);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Gets all region and claim names (WorldGuard region IDs, Towny town names) at the specified location.
     *
     * @param location The location to query.
     * @return Set of all matching region names.
     */
    public static @NonNull Set<String> getRegionNames(@NonNull Location location) {
        Set<String> names = new HashSet<>();
        if (hasWorldGuard()) {
            names.addAll(getWorldGuardRegions(location));
        }
        if (hasTowny()) {
            String town = getTownyTownName(location);
            if (town != null) {
                names.add(town);
            }
        }
        return names;
    }

    /**
     * Checks if the specified location is within a given region name (case-insensitive).
     *
     * @param location   The location to check.
     * @param regionName The target region name.
     * @return True if the location is inside the region.
     */
    public static boolean isInRegion(@NonNull Location location, @NonNull String regionName) {
        for (String name : getRegionNames(location)) {
            if (name.equalsIgnoreCase(regionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the specified location is within any of the provided region names (case-insensitive).
     *
     * @param location    The location to check.
     * @param regionNames Collection of region names to check against.
     * @return True if the location matches any of the given regions.
     */
    public static boolean isInAnyRegion(@NonNull Location location, @NonNull Collection<String> regionNames) {
        if (regionNames.isEmpty()) {
            return false;
        }
        Set<String> current = getRegionNames(location);
        if (current.isEmpty()) {
            return false;
        }
        for (String target : regionNames) {
            for (String active : current) {
                if (active.equalsIgnoreCase(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasWorldGuard() {
        if (!worldGuardChecked) {
            worldGuardChecked = true;
            worldGuardAvailable = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
        }
        return worldGuardAvailable;
    }

    public static boolean hasTowny() {
        if (!townyChecked) {
            townyChecked = true;
            townyAvailable = Bukkit.getPluginManager().getPlugin("Towny") != null;
        }
        return townyAvailable;
    }

    public static boolean hasGriefPrevention() {
        if (!griefPreventionChecked) {
            griefPreventionChecked = true;
            griefPreventionAvailable = Bukkit.getPluginManager().getPlugin("GriefPrevention") != null;
        }
        return griefPreventionAvailable;
    }

    private static @NonNull WorldGuardContext createWorldGuardContext(@NonNull Location location) throws Exception {
        Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
        Object wgInstance = wgClass.getMethod("getInstance").invoke(null);
        Object platform = wgClass.getMethod("getPlatform").invoke(wgInstance);
        Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
        Object query = container.getClass().getMethod("createQuery").invoke(container);

        Class<?> adapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Method adaptLoc = adapterClass.getMethod("adapt", Location.class);
        Object adaptedLoc = adaptLoc.invoke(null, location);

        return new WorldGuardContext(query, adaptedLoc);
    }

    private static boolean checkWorldGuardPvP(@NonNull Location location, @Nullable Player player) throws Exception {
        WorldGuardContext ctx = createWorldGuardContext(location);

        Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.Flags");
        Object pvpFlag = flagsClass.getField("PVP").get(null);

        Object subject = null;
        if (player != null) {
            try {
                Class<?> wgPluginClass = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
                Object wgPluginInstance = wgPluginClass.getMethod("inst").invoke(null);
                subject = wgPluginClass.getMethod("wrapPlayer", Player.class).invoke(wgPluginInstance, player);
            } catch (Throwable ignored) {
                Class<?> adapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                Method adaptPlayer = adapterClass.getMethod("adapt", Player.class);
                subject = adaptPlayer.invoke(null, player);
            }
        }

        Method testState = ctx.query().getClass().getMethod("testState",
                Class.forName("com.sk89q.worldedit.util.Location"),
                Class.forName("com.sk89q.worldguard.protection.association.RegionAssociable"),
                Class.forName("com.sk89q.worldguard.protection.flags.StateFlag[]"));

        Class<?> stateFlagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag");
        Object flagArray = Array.newInstance(stateFlagClass, 1);
        Array.set(flagArray, 0, pvpFlag);

        Object result = testState.invoke(ctx.query(), ctx.adaptedLocation(), subject, flagArray);
        return Boolean.TRUE.equals(result);
    }

    private static boolean checkTownyPvP(@NonNull Location location) throws Exception {
        Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
        Object apiInstance = townyApiClass.getMethod("getInstance").invoke(null);
        try {
            Method isPvPMethod = townyApiClass.getMethod("isPvP", Location.class);
            return (boolean) isPvPMethod.invoke(apiInstance, location);
        } catch (NoSuchMethodException e) {
            Object town = townyApiClass.getMethod("getTown", Location.class).invoke(apiInstance, location);
            if (town == null) {
                return true;
            }
            Method isPvp = town.getClass().getMethod("isPVP");
            return (boolean) isPvp.invoke(town);
        }
    }

    private static boolean checkGriefPreventionPvP(@NonNull Location location) throws Exception {
        Class<?> gpClass = Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
        Object gpInstance = gpClass.getField("instance").get(null);
        Object dataStore = gpClass.getField("dataStore").get(gpInstance);
        Method getClaim = dataStore.getClass().getMethod("getClaimAt", Location.class,
                boolean.class, Class.forName("me.ryanhamshire.GriefPrevention.Claim"));
        Object claim = getClaim.invoke(dataStore, location, false, null);
        if (claim == null) {
            return true;
        }
        try {
            Method pvpMethod = claim.getClass().getMethod("isPvpEnabled");
            return (boolean) pvpMethod.invoke(claim);
        } catch (NoSuchMethodException e) {
            return true;
        }
    }

    private record WorldGuardContext(@NonNull Object query, @NonNull Object adaptedLocation) {
    }
}
