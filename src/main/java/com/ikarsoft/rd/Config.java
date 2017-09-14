package com.ikarsoft.rd;

import net.minecraft.util.text.TextFormatting;

public class Config {

    private static final String configKeyPrefix = "config.rd";

    private static final ConfigValues defaultValues = new ConfigValues();
    private static final ConfigValues values = new ConfigValues();

    public static int getMaxSubtypes() {
        return values.maxSubtypes;
    }

    public static String parseFriendlyModNameFormat(String formatWithEnumNames) {
        if (formatWithEnumNames.isEmpty()) {
            return "";
        }
        StringBuilder format = new StringBuilder();
        String[] strings = formatWithEnumNames.split(" ");
        for (String string : strings) {
            TextFormatting valueByName = TextFormatting.getValueByName(string);
            if (valueByName != null) {
                format.append(valueByName.toString());
            } else {
                Log.get().error("Invalid format: {}", string);
            }
        }
        return format.toString();
    }


    public static class ConfigValues {
        // advanced
        public boolean debugModeEnabled = false;
        public boolean centerSearchBarEnabled = false;
        public final String modNameFormatFriendly = "blue italic";
        public String modNameFormat = Config.parseFriendlyModNameFormat(modNameFormatFriendly);
        public int maxSubtypes = 300;

        // per-world
        public boolean overlayEnabled = true;
        public boolean cheatItemsEnabled = false;
        public boolean editModeEnabled = false;
        public String filterText = "";
    }
}
