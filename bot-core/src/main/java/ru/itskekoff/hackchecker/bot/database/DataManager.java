package ru.itskekoff.hackchecker.bot.database;

import com.google.gson.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import ru.itskekoff.hackchecker.bot.configuration.Settings;
import ru.itskekoff.hackchecker.bot.listeners.slash.ChannelSlashCommandsListener;
import ru.itskekoff.hackchecker.bot.utils.OtherUtils;
import ru.itskekoff.hackchecker.bot.utils.types.BannedUser;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.itskekoff.hackchecker.bot.PluginScanBot.jda;
import static ru.itskekoff.hackchecker.bot.PluginScanBot.logger;

public class DataManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    private File databaseFile;
    private JsonObject database;

    public void init() {
        this.databaseFile = new File("./database.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!databaseFile.exists() || databaseFile.length() == 0) {
            try (FileWriter writer = new FileWriter(databaseFile)) {
                databaseFile.createNewFile();
                JsonObject initialDatabase = new JsonObject();
                initialDatabase.add("banned", new JsonArray());
                initialDatabase.add("users", new JsonArray());
                writer.write(gson.toJson(initialDatabase));
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.database = parseDatabase();
    }

    public void createTasks() {
        scheduler.scheduleAtFixedRate(this::clearUserPlugins, 0, OtherUtils.convertTime(
                Settings.IMP.LIMITS.PLUGINS_LIMIT_RESET_TIME).longValue(), TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::clearUserDuplicates, 0, OtherUtils.convertTime(
                Settings.IMP.LIMITS.DUPLICATES_LIMIT_RESET_TIME).longValue(), TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> {
            this.resetCheckTimer();
            this.checkChannelsAndClear();
            this.checkUsersAndClear();
        }, 0, 15, TimeUnit.SECONDS);
    }

    private JsonObject parseDatabase() {
        try (FileReader reader = new FileReader(databaseFile)) {
            return (JsonObject) JsonParser.parseReader(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveDatabase() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(databaseFile)) {
            writer.write(gson.toJson(database));
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUserExists(long id) {
        JsonArray users = (JsonArray) database.get("users");
        boolean userFound = false;
        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            long userId = user.get("id").getAsLong();
            if (userId == id) {
                userFound = true;
            }
        }
        return userFound;
    }

    private void addUserIfNotExists(long id) {
        if (!isUserExists(id)) {
            JsonArray users = (JsonArray) database.get("users");
            JsonObject user = new JsonObject();
            user.addProperty("id", id);
            user.addProperty("channel_id", 0);
            user.addProperty("latest_user_check", 0);
            user.addProperty("next_plugin_check", 0);
            user.addProperty("checked_plugins", 0);
            user.add("plugins", new JsonArray());
            users.add(user);
            saveDatabase();
            logger.info("Created user with id {}", id);
        }
    }

    public int getCheckedPlugins(User member) {
        JsonArray users = (JsonArray) database.get("users");

        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            long userId = user.get("id").getAsLong();
            if (userId == member.getIdLong()) {
                return user.get("checked_plugins").getAsInt();
            }
        }
        return 0;
    }

    public void updateCheckedPlugins(User member, int times) {
        JsonArray users = (JsonArray) database.get("users");

        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            long userId = user.get("id").getAsLong();
            if (userId == member.getIdLong()) {
                user.addProperty("checked_plugins", user.get("checked_plugins").getAsInt() + times);
                break;
            }
        }
        saveDatabase();
    }

    public boolean isUserAndChannelCorrect(User member, Channel channel) {
        JsonArray users = (JsonArray) database.get("users");
        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            long userId = user.get("id").getAsLong();
            if (userId == member.getIdLong()) {
                return user.get("channel_id").getAsLong() == channel.getIdLong();
            }
        }
        return false;
    }


    public boolean isUserWithChannelExists(long id) {
        JsonArray users = (JsonArray) database.get("users");
        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            if (user.get("channel_id").getAsLong() == id) {
                return true;
            }
        }
        return false;
    }


    public boolean createChannelForUser(Member member, MessageEmbed channelEmbed) {
        JsonArray users = (JsonArray) database.get("users");
        Category category = member.getGuild().getCategoryById(Settings.IMP.MAIN.CATEGORY_ID);
        if (category != null) {
            for (Object obj : users) {
                JsonObject user = (JsonObject) obj;
                long userId = user.get("id").getAsLong();
                if (userId == member.getIdLong()) {
                    if (user.get("channel_id").getAsLong() > 0) {
                        return false;
                    }
                }
            }
        }

        ChannelAction<TextChannel> action = member.getGuild().createTextChannel(
                        OtherUtils.formatUser(member.getUser().getName()) + "-check",
                        member.getGuild().getCategoryById(Settings.IMP.MAIN.CATEGORY_ID))
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(member.getGuild().getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));

        RestAction<?> success = action.onSuccess(createdChannel -> {
            createdChannel.sendMessage(member.getAsMention())
                    .setEmbeds(channelEmbed)
                    .queue();
            addUserIfNotExists(member.getIdLong());
            for (Object obj : users) {
                JsonObject user = (JsonObject) obj;
                long userId = user.get("id").getAsLong();
                if (userId == member.getIdLong()) {
                    user.addProperty("latest_user_check", System.currentTimeMillis());
                    user.addProperty("channel_id", createdChannel.getIdLong());
                }
                saveDatabase();
            }
        });
        success.queue();
        return true;
    }


    public void removeChannel(TextChannel channel, long memberId) {
        channel.delete().queue();
        JsonArray users = (JsonArray) database.get("users");
        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            long userId = user.get("id").getAsLong();
            if (userId == memberId) {
                user.addProperty("latest_user_check", 0);
                user.addProperty("channel_id", 0);
            }
        }
        saveDatabase();
        logger.info("Deleted channel for user {}", channel.getName().split(ChannelSlashCommandsListener.CHECK_SUFFIX)[0]);
    }


    public void removeChannel(TextChannel channel) {
        channel.delete().queue();
        JsonArray users = (JsonArray) database.get("users");
        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            long channelId = user.get("channel_id").getAsLong();
            if (channelId == channel.getIdLong()) {
                user.addProperty("latest_user_check", 0);
                user.addProperty("channel_id", 0);
            }
        }
        saveDatabase();
        logger.info("Deleted channel for user {}", channel.getName().split(ChannelSlashCommandsListener.CHECK_SUFFIX)[0]);
    }


    public List<String> getPlugins(long id) {
        List<String> queue = new ArrayList<>();
        JsonArray dbUsers = (JsonArray) database.get("users");

        for (Object obj : dbUsers) {
            JsonObject object = (JsonObject) obj;
            if (object.get("id").getAsLong() == id) {
                JsonArray plugins = object.getAsJsonArray("plugins");
                plugins.forEach(jsonElement -> queue.add(jsonElement.getAsString()));
            }
        }
        return queue;
    }

    public void addPlugin(long id, String fileName) {
        addUserIfNotExists(id);
        JsonArray dbUsers = (JsonArray) database.get("users");

        for (Object obj : dbUsers) {
            JsonObject object = (JsonObject) obj;
            if (object.get("id").getAsLong() == id) {
                JsonArray plugins = object.getAsJsonArray("plugins");
                plugins.add(fileName);
            }
        }
        saveDatabase();
    }

    public void clearPlugins(long id) {
        JsonArray dbUsers = (JsonArray) database.get("users");

        for (Object obj : dbUsers) {
            JsonObject object = (JsonObject) obj;
            if (object.get("id").getAsLong() == id) {
                object.add("plugins", new JsonArray());
            }
        }
        saveDatabase();
    }

    @Nullable
    public BannedUser getBannedUser(long id) {
        JsonArray dbQueue = (JsonArray) database.get("banned");

        for (Object obj : dbQueue) {
            JsonObject object = (JsonObject) obj;
            if (object.get("id").getAsLong() == id) {
                return new BannedUser(id, object.get("expiration").getAsLong(), object.get("reason").getAsString());
            }
        }
        return null;
    }

    public void addBanned(long id, String reason, long expirationTimeInSeconds) {
        JsonArray dbQueue = (JsonArray) database.get("banned");
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("reason", reason);
        long expirationTimeInMillis = System.currentTimeMillis() + expirationTimeInSeconds * 1000;
        obj.addProperty("expiration", expirationTimeInMillis);
        dbQueue.add(obj);
        saveDatabase();
    }

    public void removeBanned(long id) {
        JsonArray dbQueue = (JsonArray) database.get("banned");

        for (Object obj : dbQueue) {
            JsonObject object = (JsonObject) obj;
            if (object.get("id").getAsLong() == id) {
                dbQueue.remove(object);
                break;
            }
        }
        saveDatabase();
    }

    public void updateLatestCheck(long id) {
        JsonArray dbQueue = (JsonArray) database.get("users");

        for (Object obj : dbQueue) {
            JsonObject object = (JsonObject) obj;
            if (object.get("id").getAsLong() == id) {
                object.addProperty("latest_user_check", System.currentTimeMillis());
            }
        }
        saveDatabase();
    }

    private void checkUsersAndClear() {
        JsonArray users = (JsonArray) database.get("users");
        List<JsonObject> toRemoveObjects = new CopyOnWriteArrayList<>();
        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            JsonArray plugins = user.get("plugins").getAsJsonArray();
            long channelId = user.get("channel_id").getAsLong();
            int checkedPlugins = user.get("checked_plugins").getAsInt();
            if (channelId == 0 && checkedPlugins == 0 && plugins.isEmpty()) {
                toRemoveObjects.add(user);
            }
        }
        toRemoveObjects.forEach(users::remove);
        saveDatabase();
        if (!toRemoveObjects.isEmpty()) {
            logger.info("Automatically removed {} {} from database", toRemoveObjects.size(),
                    (toRemoveObjects.size() == 1) ? "user" : "users");
        }
        toRemoveObjects.clear();
        JsonArray bannedUsers = (JsonArray) database.get("banned");
        for (Object obj : bannedUsers) {
            JsonObject bannedUser = (JsonObject) obj;
            long expiration = bannedUser.get("expiration").getAsLong();
            if (System.currentTimeMillis() >= expiration) {
                toRemoveObjects.add(bannedUser);
            }
        }
        toRemoveObjects.forEach(obj -> removeBanned(obj.get("id").getAsLong()));
        if (!toRemoveObjects.isEmpty()) {
            logger.info("Automatically unbanned {} {} from database", toRemoveObjects.size(),
                    (toRemoveObjects.size() == 1) ? "user" : "users");
        }
        saveDatabase();
    }

    private void checkChannelsAndClear() {
        JsonArray users = (JsonArray) database.get("users");
        for (Object obj : users) {
            JsonObject user = (JsonObject) obj;
            long channelId = user.get("channel_id").getAsLong();
            if (channelId == 0) {
                continue;
            }
            long lastMeasureTime = user.get("latest_user_check").getAsLong();
            long currentTime = System.currentTimeMillis();
            long difference = currentTime - lastMeasureTime;
            long minutesPassed = difference / (1000 * 60);
            if (minutesPassed >= Settings.IMP.MAIN.DELETE_TIME) {
                Category category = jda.getCategoryById(Settings.IMP.MAIN.CATEGORY_ID);
                if (category != null) {
                    Optional<TextChannel> optionalChannel = category.getTextChannels().stream().filter(textChannel ->
                                    textChannel.getIdLong() == channelId)
                            .findFirst();
                    user.addProperty("latest_user_check", 0);
                    user.addProperty("channel_id", 0);
                    saveDatabase();
                    long userId = user.get("id").getAsLong();
                    if (optionalChannel.isEmpty()) {
                        logger.error("Can't delete channel for user {}", userId);
                        continue;
                    }
                    optionalChannel.get().delete().queue();
                    logger.info("Deleted channel for user {}", userId);
                }
            }
        }
    }


    private void clearUserPlugins() {
        JsonArray dbQueue = database.getAsJsonArray("users");

        for (JsonElement userElement : dbQueue) {
            JsonObject userObject = userElement.getAsJsonObject();
            JsonArray plugins = userObject.getAsJsonArray("plugins");
            JsonArray duplicatePlugins = getDuplicates(plugins);
            userObject.add("plugins", duplicatePlugins);
        }
        saveDatabase();
    }

    private void clearUserDuplicates() {
        JsonArray dbQueue = database.getAsJsonArray("users");

        for (JsonElement userElement : dbQueue) {
            JsonObject userObject = userElement.getAsJsonObject();
            JsonArray plugins = userObject.getAsJsonArray("plugins");
            JsonArray uniquePlugins = removeDuplicates(plugins);
            userObject.add("plugins", uniquePlugins);
        }
        saveDatabase();
    }

    private JsonArray removeDuplicates(JsonArray plugins) {
        Set<JsonElement> uniqueElements = new HashSet<>();
        plugins.forEach(uniqueElements::add);
        JsonArray uniquePlugins = new JsonArray();
        for (JsonElement plugin : uniqueElements) {
            uniquePlugins.add(plugin);
        }
        return uniquePlugins;
    }

    private JsonArray getDuplicates(JsonArray plugins) {
        Set<JsonElement> uniqueElements = new HashSet<>();
        JsonArray duplicates = new JsonArray();
        for (JsonElement plugin : plugins) {
            if (!uniqueElements.add(plugin)) {
                duplicates.add(plugin);
            }
        }
        return duplicates;
    }



    private void resetCheckTimer() {
        JsonArray dbQueue = (JsonArray) database.get("users");
        for (Object obj : dbQueue) {
            JsonObject object = (JsonObject) obj;
            long checkTimer = object.get("next_plugin_check").getAsLong();
            if (System.currentTimeMillis() >= checkTimer) {
                object.addProperty("checked_plugins", 0);
                long newCheckTimer = System.currentTimeMillis() + OtherUtils.convertTime(Settings.IMP.LIMITS.PLUGIN_RESET_TIME).longValue();
                object.addProperty("next_plugin_check", newCheckTimer);
            }
        }
        saveDatabase();
    }
}
