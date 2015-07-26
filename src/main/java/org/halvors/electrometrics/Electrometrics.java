package org.halvors.electrometrics;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import mekanism.api.ItemRetriever;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.halvors.electrometrics.common.CommonProxy;
import org.halvors.electrometrics.common.ConfigurationManager;
import org.halvors.electrometrics.common.ConfigurationManager.General;
import org.halvors.electrometrics.common.Reference;
import org.halvors.electrometrics.common.Tab;
import org.halvors.electrometrics.common.base.MachineType;
import org.halvors.electrometrics.common.base.Tier;
import org.halvors.electrometrics.common.block.BlockMachine;
import org.halvors.electrometrics.common.event.PlayerEventHandler;
import org.halvors.electrometrics.common.item.ItemBlockMachine;
import org.halvors.electrometrics.common.item.ItemMultimeter;
import org.halvors.electrometrics.common.tile.machine.TileEntityElectricityMeter;

/**
 * This is the Electrometrics class, which is the main class of this mod.
 *
 * @author halvors
 */
@Mod(modid = Reference.ID,
     name = Reference.NAME,
     version = Reference.VERSION,
     dependencies = "after:CoFHCore;" +
				    "after:Mekanism",
     guiFactory = "org.halvors." + Reference.ID + ".client.gui.configuration.GuiConfiguationFactory")
public class Electrometrics {
	// The instance of your mod that Forge uses.
	@Instance(value = Reference.ID)
	public static Electrometrics instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide = "org.halvors.electrometrics.client.ClientProxy", serverSide = "org.halvors.electrometrics.common.CommonProxy")
	public static CommonProxy proxy;

	// Logger instance.
	private static final Logger logger = LogManager.getLogger(Reference.ID);

	// Creative tab.
	private static final Tab tab = new Tab();

	// Items.
	public static final Item itemMultimeter = new ItemMultimeter();

	// Blocks.
	public static final BlockMachine blockMachine = new BlockMachine();

	// ConfigurationManager.
	private static Configuration configuration;


	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// Initialize configuration.
        configuration = new Configuration(event.getSuggestedConfigurationFile());

        // Load the configuration.
        ConfigurationManager.loadConfiguration(configuration);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Register the our EventHandler.
		FMLCommonHandler.instance().bus().register(new PlayerEventHandler());

		// Register the proxy as our GuiHandler to NetworkRegistry.
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

		// Call functions for adding blocks, items, etc.
		addItems();
		addBlocks();
		addTileEntities();
		addRecipes();

		// Mod integration.
		logger.log(Level.INFO, "Mekanism integration is " + (General.isMekanismIntegrationEnabled ? "enabled" : "disabled") + ".");
	}

	private void addItems() {
		// Register items.
		GameRegistry.registerItem(itemMultimeter, "itemMultimeter");
	}

	private void addBlocks() {
		// Register blocks.
		GameRegistry.registerBlock(blockMachine, ItemBlockMachine.class, "blockMachine");
	}

	private void addTileEntities() {
		// Register tile entities.
		GameRegistry.registerTileEntity(TileEntityElectricityMeter.class, "tileEntityElectricityMeter");
	}

	private void addRecipes() {
		// Register recipes.

        // Multimeter
        ItemStack copperIngot = new ItemStack(Items.iron_ingot);
        Item circuit = Items.repeater;
        ItemStack battery = new ItemStack(Items.diamond);
        ItemStack cable = new ItemStack(Items.gold_ingot);
        ItemStack casing = new ItemStack(Blocks.iron_block);

        if (General.isMekanismIntegrationEnabled) {
            // Multimeter
            copperIngot = new ItemStack(ItemRetriever.getItem("Ingot").getItem(), 1, 5); // Copper ingot.
            circuit = ItemRetriever.getItem("ControlCircuit").getItem(); // Basic control circuit.
            battery = new ItemStack(ItemRetriever.getItem("EnergyTablet").getItem(), 1, 100); // Uncharged battery.
            cable = new ItemStack(ItemRetriever.getItem("PartTransmitter").getItem(), 8); // Basic universal cable.
            casing = new ItemStack(ItemRetriever.getBlock("BasicBlock").getItem(), 1, 8); // Steel casing.
        }

        // Multimeter
        GameRegistry.addRecipe(new ItemStack(itemMultimeter),
                "RGR",
                "IMI",
                "CBC",
                'R', Items.redstone,
                'G', Blocks.glass_pane,
                'I', copperIngot,
                'M', Items.clock,
                'C', circuit,
                'B', battery);

        // Electricity Meter
        for (Tier.ElectricityMeter tier : Tier.ElectricityMeter.values()) {
            if (General.isMekanismIntegrationEnabled) {
                cable = new ItemStack(ItemRetriever.getItem("PartTransmitter").getItem(), 8, tier.ordinal()); // Tier matching universal cable.
            }

            MachineType machineType = tier.getMachineType();
            ItemStack itemStackMachine = machineType.getItemStack();
            ItemBlockMachine itemBlockMachine = (ItemBlockMachine) itemStackMachine.getItem();
            itemBlockMachine.setElectricityMeterTier(itemStackMachine, tier);

            GameRegistry.addRecipe(itemStackMachine,
                    "RMR",
                    "C#C",
                    "RBR",
                    'R', Items.redstone,
                    'M', itemMultimeter,
                    'C', cable,
                    '#', casing,
                    'B', battery);
        }
	}

	public static CommonProxy getProxy() {
		return proxy;
	}

	public static Electrometrics getInstance() {
		return instance;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static Tab getTab() {
		return tab;
	}

	public static Configuration getConfiguration() {
		return configuration;
	}
}