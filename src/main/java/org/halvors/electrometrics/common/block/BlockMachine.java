package org.halvors.electrometrics.common.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.halvors.electrometrics.Electrometrics;
import org.halvors.electrometrics.client.render.BlockRenderer;
import org.halvors.electrometrics.common.base.MachineType;
import org.halvors.electrometrics.common.base.Tier;
import org.halvors.electrometrics.common.base.tile.ITileOwnable;
import org.halvors.electrometrics.common.base.tile.ITileRedstoneControl;
import org.halvors.electrometrics.common.item.ItemBlockMachine;
import org.halvors.electrometrics.common.tile.TileEntity;
import org.halvors.electrometrics.common.tile.TileEntityComponentContainer;
import org.halvors.electrometrics.common.tile.machine.TileEntityElectricityMeter;
import org.halvors.electrometrics.common.util.MachineUtils;

import java.util.List;

/**
 * Block class for handling multiple machine block IDs.
 *
 * 0: Basic Electricity Meter
 * 1: Advanced Electricity Meter
 * 2: Elite Electricity Meter
 * 3: Ultimate Electricity Meter
 * 4: Creative Electricity Meter
 */
public class BlockMachine extends BlockRotatable {
	public BlockMachine() {
		super("Machine", Material.iron);

		setHardness(2F);
		setResistance(4F);
		setStepSound(soundTypeMetal);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		MachineType machineType = MachineType.getType(metadata);

		return machineType.getTileEntity();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);

		// Adding all icons for the machine types.
		for (MachineType machineType : MachineType.values()) {
			BlockRenderer.loadDynamicTextures(iconRegister, machineType.getUnlocalizedName(), iconList[machineType.getMetadata()], defaultIcon);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list) {
		// Making all MachineTypes available in creative mode.
		for (MachineType machineType : MachineType.values()) {
			switch (machineType) {
				case BASIC_ELECTRICITY_METER:
				case ADVANCED_ELECTRICITY_METER:
				case ELITE_ELECTRICITY_METER:
				case ULTIMATE_ELECTRICITY_METER:
				case CREATIVE_ELECTRICITY_METER:
					ItemStack itemStack = machineType.getItemStack();
					ItemBlockMachine itemBlockMachine = (ItemBlockMachine) itemStack.getItem();
					itemBlockMachine.setElectricityMeterTier(itemStack, Tier.ElectricityMeter.getFromMachineType(machineType));

					list.add(itemStack);
					break;

				default:
					list.add(machineType.getItemStack());
					break;
			}
		}
	}

	@Override
	public int damageDropped (int metadata) {
		return metadata;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (!world.isRemote) {
			TileEntity tileEntity = TileEntity.getTileEntity(world, x, y, z);

			// Display a message the the player clicking this block if not the owner.
			if (tileEntity instanceof TileEntityComponentContainer) {
				TileEntityComponentContainer tileEntityComponentContainer = (TileEntityComponentContainer) tileEntity;

				if (tileEntityComponentContainer.hasComponentImplementing(ITileOwnable.class)) {
					ITileOwnable tileOwnable = (ITileOwnable) tileEntityComponentContainer.getComponentImplementing(ITileOwnable.class);

					if (!tileOwnable.isOwner(player)) {
						player.addChatMessage(new ChatComponentText("This block is owned by " + tileOwnable.getOwnerName() + ", you cannot remove this block."));
					}
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float playerX, float playerY, float playerZ) {
		TileEntity tileEntity = TileEntity.getTileEntity(world, x, y, z);

		if (!MachineUtils.hasUsableWrench(player, x, y, z)) {
			// Check whether or not this ITileOwnable has a owner, if not set the current player as owner.
			if (tileEntity instanceof TileEntityComponentContainer) {
				TileEntityComponentContainer tileEntityComponentContainer = (TileEntityComponentContainer) tileEntity;

				if (tileEntityComponentContainer.hasComponentImplementing(ITileOwnable.class)) {
					ITileOwnable tileOwnable = (ITileOwnable) tileEntityComponentContainer.getComponentImplementing(ITileOwnable.class);

					if (!tileOwnable.hasOwner()) {
						tileOwnable.setOwner(player);
					}
				}
			}

			// Open the GUI.
			player.openGui(Electrometrics.getInstance(), 0, world, x, y, z);

			return true;
		}

		return super.onBlockActivated(world, x, y, z, player, facing, playerX, playerY, playerZ);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
		super.onBlockPlacedBy(world, x, y, z, entity, itemStack);

		TileEntity tileEntity = TileEntity.getTileEntity(world, x, y, z);

		if (tileEntity instanceof TileEntityComponentContainer) {
			TileEntityComponentContainer tileEntityComponentContainer = (TileEntityComponentContainer) tileEntity;

			// If this TileEntity implements ITileRedstoneControl, check if it's getting powered.
			if (tileEntityComponentContainer.hasComponentImplementing(ITileRedstoneControl.class)) {
				ITileRedstoneControl tileRedstoneControl = (ITileRedstoneControl) tileEntityComponentContainer.getComponentImplementing(ITileRedstoneControl.class);

				tileRedstoneControl.setPowered(world.isBlockIndirectlyGettingPowered(x, y, z));
			}

			// Check if this entity is a player.
			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;

				if (tileEntityComponentContainer.hasComponentImplementing(ITileOwnable.class)) {
					ITileOwnable tileOwnable = (ITileOwnable) tileEntityComponentContainer.getComponentImplementing(ITileOwnable.class);

					tileOwnable.setOwner(player);
				}
			}
		}
	}

	@Override
	 public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		if (!world.isRemote) {
			TileEntity tileEntity = TileEntity.getTileEntity(world, x, y, z);

			if (tileEntity instanceof TileEntityComponentContainer) {
				TileEntityComponentContainer tileEntityComponentContainer = (TileEntityComponentContainer) tileEntity;
				tileEntityComponentContainer.onNeighborChange();
			}
		}
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		if (!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z))) {
			dismantleBlock(world, x, y, z, false);
		}

		return world.setBlockToAir(x, y, z);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		TileEntity tileEntity = TileEntity.getTileEntity(world, x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);
		ItemStack itemStack = new ItemStack(this, 1, metadata);
		ItemBlockMachine itemBlockMachine = (ItemBlockMachine) itemStack.getItem();

		if (tileEntity instanceof TileEntityComponentContainer) {
			TileEntityComponentContainer tileEntityComponentContainer = (TileEntityComponentContainer) tileEntity;

			if (tileEntityComponentContainer.hasComponentImplementing(ITileRedstoneControl.class)) {
				ITileRedstoneControl tileRedstoneControl = (ITileRedstoneControl) tileEntityComponentContainer.getComponentImplementing(ITileRedstoneControl.class);

				itemBlockMachine.setRedstoneControlType(itemStack, tileRedstoneControl.getControlType());
			}
		}

		if (tileEntity instanceof TileEntityElectricityMeter) {
			TileEntityElectricityMeter tileEntityElectricityMeter = (TileEntityElectricityMeter) tileEntity;

			itemBlockMachine.setElectricityMeterTier(itemStack, tileEntityElectricityMeter.getTier());
			itemBlockMachine.setElectricityCount(itemStack, tileEntityElectricityMeter.getElectricityCount());
			itemBlockMachine.setElectricityStored(itemStack, tileEntityElectricityMeter.getStorage().getEnergyStored());
		}

		return itemStack;
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		TileEntity tileEntity = TileEntity.getTileEntity(world, x, y, z);

		// If this TileEntity implements ITileOwnable, we check if there is a owner.
		if (tileEntity instanceof TileEntityComponentContainer) {
			TileEntityComponentContainer tileEntityComponentContainer = (TileEntityComponentContainer) tileEntity;

			if (tileEntityComponentContainer.hasComponentImplementing(ITileOwnable.class)) {
				ITileOwnable tileOwnable = (ITileOwnable) tileEntityComponentContainer.getComponentImplementing(ITileOwnable.class);
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;

				return tileOwnable.isOwner(player) ? blockHardness : -1;
			}
		}

		return blockHardness;
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		TileEntity tileEntity = TileEntity.getTileEntity(world, x, y, z);

		if (tileEntity instanceof TileEntityComponentContainer) {
			TileEntityComponentContainer tileEntityComponentContainer = (TileEntityComponentContainer) tileEntity;

			if (tileEntityComponentContainer.hasComponentImplementing(ITileOwnable.class)) {
				ITileOwnable tileOwnable = (ITileOwnable) tileEntityComponentContainer.getComponentImplementing(ITileOwnable.class);


				EntityPlayer player = Minecraft.getMinecraft().thePlayer;

				return tileOwnable.isOwner(player) ? blockResistance : -1;
			}
		}

		return blockResistance;
	}
}