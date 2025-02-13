package com.kryeit.missions.config;

import com.kryeit.JSONObject;
import com.kryeit.JSONObject.JSONArray;
import com.kryeit.compat.CompatAddon;
import com.kryeit.missions.MissionType;
import com.kryeit.missions.MissionTypeRegistry;
import com.kryeit.utils.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigReader {
    private final Map<MissionType, Mission> missions;
    private final List<ItemStack> exchange;
    public static double EXCHANGER_DROP_RATE;
    public static int FIRST_REROLL_CURRENCY;
    public static int FREE_REROLLS;
    public static String COMMAND_UPON_MISSION;

    private ConfigReader(Map<MissionType, Mission> missions, List<ItemStack> exchange) {
        this.missions = missions;
        this.exchange = exchange;
    }

    public static ConfigReader readFile(Path path) throws IOException {

        String content = readOrCopyFile(path.resolve("missions.json"), "/missions.json");

        Map<MissionType, Mission> missions = new HashMap<>();
        JSONObject object = new JSONObject(content);

        for (String key : object.keySet()) {
            JSONObject value = object.getObject(key);
            JSONObject reward = value.getObject("reward");
            float weight = value.optFloat("weight").orElse(1f);
            if (weight == 0) continue;

            MissionType missionType = MissionTypeRegistry.INSTANCE.getType(key);
            Mission mission = new Mission(
                    Range.fromString(reward.getString("amount")),
                    reward.getString("item"),
                    missionType,
                    getItems(value.getObject("missions")),
                    value.getArray("titles").asList(JSONArray::getString),
                    weight
            );
            missions.put(missionType, mission);
        }

        String exchange;
        if (CompatAddon.NUMISMATICS.isLoaded()) {
            exchange = readOrCopyFile(path.resolve("currency.json"), "/numismatics/currency.json");
        } else {
            exchange = readOrCopyFile(path.resolve("currency.json"), "/currency.json");
        }

        JSONArray jsonArray = new JSONArray(exchange);
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject itemObj = jsonArray.getObject(i);
            itemObj.keySet().forEach(key -> {
                int quantity = Integer.parseInt(itemObj.getString(key));
                ResourceLocation location = new ResourceLocation(key);
                ItemStack itemStack = Utils.getItem(location, quantity);
                items.add(itemStack);
            });
        }

        String config = readOrCopyFile(path.resolve("config.json"), "/config.json");
        JSONObject configObject = new JSONObject(config);

        EXCHANGER_DROP_RATE = Double.parseDouble(configObject.getString("exchanger-drop-rate"));
        FIRST_REROLL_CURRENCY = Integer.parseInt(configObject.getString("first-reroll-currency"));
        FREE_REROLLS = Integer.parseInt(configObject.getString("free-rerolls"));
        COMMAND_UPON_MISSION = configObject.getString("command-upon-mission");

        return new ConfigReader(missions, items);
    }

    public static String readOrCopyFile(Path path, String exampleFile) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            InputStream stream = ConfigReader.class.getResourceAsStream(exampleFile);
            if (stream == null) throw new NullPointerException("Cannot load example file");

            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            Files.copy(stream, path);
        }
        return Files.readString(path);
    }

    private static Map<String, Range> getItems(JSONObject items) {
        Map<String, Range> itemsMap = new HashMap<>();
        for (String itemKey : items.keySet()) {

            if (itemKey.startsWith("#")) {
                String tagName = itemKey.replace("#", "");
                TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(tagName));
                List<String> taggedItems = getItemsFromTag(tag);
                for (String taggedItem : taggedItems) {
                    itemsMap.put(taggedItem, Range.fromString(items.getString(itemKey)));
                }
            } else {
                itemsMap.put(itemKey, Range.fromString(items.getString(itemKey)));
            }
        }
        return itemsMap;
    }

    private static List<String> getItemsFromTag(TagKey<Item> tag) {

        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item.getDefaultInstance().is(tag))
                .map(item -> BuiltInRegistries.ITEM.getKey(item).toString())
                .toList();
    }

    public Map<MissionType, Mission> getMissions() {
        return missions;
    }

    public List<ItemStack> exchange() {
        return exchange;
    }

    public record Mission(Range rewardAmount, String rewardItem, MissionType missionType, Map<String, Range> items,
                          List<String> titles, float weight) {
    }
}
