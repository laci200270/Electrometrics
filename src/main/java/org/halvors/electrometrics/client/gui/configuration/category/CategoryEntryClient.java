package org.halvors.electrometrics.client.gui.configuration.category;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiConfigEntries.CategoryEntry;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import org.halvors.electrometrics.Electrometrics;

public class CategoryEntryClient extends CategoryEntry {
    public CategoryEntryClient(GuiConfig guiConfig, GuiConfigEntries guiConfigEntries, IConfigElement configElement) {
        super(guiConfig, guiConfigEntries, configElement);
    }

    @Override
    protected GuiScreen buildChildScreen() {
        return new GuiConfig(owningScreen,
                new ConfigElement(Electrometrics.getConfiguration().getCategory("client")).getChildElements(),
                owningScreen.modID, "client", false, false,
                GuiConfig.getAbridgedConfigPath(Electrometrics.getConfiguration().toString()));
    }
}