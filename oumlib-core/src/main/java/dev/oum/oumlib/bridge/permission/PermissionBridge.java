package dev.oum.oumlib.bridge.permission;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PermissionBridge {

    private static Object luckPerms;

    static {
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            luckPerms = provider.getMethod("get").invoke(null);
        } catch (Throwable ignored) {
        }
    }

    private PermissionBridge() {
    }

    public static boolean isAvailable() {
        return luckPerms != null;
    }

    public static @Nullable String getPrimaryGroup(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            return (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getPrefix(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            return (String) metaData.getClass().getMethod("getPrefix").invoke(metaData);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getSuffix(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            return (String) metaData.getClass().getMethod("getSuffix").invoke(metaData);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable String getMetaValue(@NonNull UUID uuid, @NonNull String key) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            return (String) metaData.getClass().getMethod("getMetaValue", String.class).invoke(metaData, key);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static @Nullable List<String> getGroups(@NonNull UUID uuid) {
        if (luckPerms == null) return null;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) return null;
            List<String> groups = new ArrayList<>();
            Collection<?> nodes = (Collection<?>) user.getClass().getMethod("getNodes").invoke(user);
            Class<?> inheritanceNodeClass = Class.forName("net.luckperms.api.node.types.InheritanceNode");
            Method getGroupNameMethod = inheritanceNodeClass.getMethod("getGroupName");
            for (Object node : nodes) {
                if (inheritanceNodeClass.isInstance(node)) {
                    groups.add((String) getGroupNameMethod.invoke(node));
                }
            }
            return groups;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void setGroups(@NonNull UUID uuid, @NonNull List<String> groups, @Nullable String primaryGroup) {
        if (luckPerms == null) return;
        try {
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            CompletableFuture<?> future = (CompletableFuture<?>) userManager.getClass()
                    .getMethod("loadUser", UUID.class)
                    .invoke(userManager, uuid);

            future.thenAcceptAsync(user -> {
                try {
                    Object data = user.getClass().getMethod("data").invoke(user);

                    Class<?> nodeTypeClass = Class.forName("net.luckperms.api.node.NodeType");
                    Object inheritanceType = nodeTypeClass.getField("INHERITANCE").get(null);

                    data.getClass().getMethod("clear", nodeTypeClass).invoke(data, inheritanceType);

                    Class<?> inheritanceNodeClass = Class.forName("net.luckperms.api.node.types.InheritanceNode");
                    Method builderMethod = inheritanceNodeClass.getMethod("builder", String.class);

                    Class<?> nodeClass = Class.forName("net.luckperms.api.node.Node");
                    Method addMethod = data.getClass().getMethod("add", nodeClass);

                    for (String group : groups) {
                        Object builder = builderMethod.invoke(null, group);
                        Object node = builder.getClass().getMethod("build").invoke(builder);
                        addMethod.invoke(data, node);
                    }

                    if (primaryGroup != null) {
                        user.getClass().getMethod("setPrimaryGroup", String.class).invoke(user, primaryGroup);
                    }

                    Class<?> userClass = Class.forName("net.luckperms.api.model.user.User");
                    userManager.getClass().getMethod("saveUser", userClass).invoke(userManager, user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception ignored) {
        }
    }
}
