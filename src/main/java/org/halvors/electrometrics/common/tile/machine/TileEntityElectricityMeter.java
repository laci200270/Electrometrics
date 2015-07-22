package org.halvors.electrometrics.common.tile.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import org.halvors.electrometrics.common.base.MachineType;
import org.halvors.electrometrics.common.base.Tier;
import org.halvors.electrometrics.common.base.tile.ITileActivatable;
import org.halvors.electrometrics.common.base.tile.ITileNetworkable;
import org.halvors.electrometrics.common.network.PacketHandler;
import org.halvors.electrometrics.common.network.PacketRequestData;
import org.halvors.electrometrics.common.tile.component.TileOwnableComponent;
import org.halvors.electrometrics.common.tile.component.TileRedstoneControlComponent;

import java.util.EnumSet;
import java.util.List;

/**
 * This is the TileEntity of the Electricity Meter which provides a simple way to keep count of the electricity you use.
 * Especially suitable for pay-by-use applications where a player buys electricity from another player on multiplayer worlds.
 *
 * @author halvors
 */
public class TileEntityElectricityMeter extends TileEntityElectricityProvider implements ITileNetworkable, ITileActivatable {
    // Whether or not this TileEntity's block is in it's active state.
    private boolean isActive;

    // The client's current active state.
    @SideOnly(Side.CLIENT)
    private boolean clientIsActive;

    // The tier of this TileEntity.
	private Tier.ElectricityMeter tier = Tier.ElectricityMeter.BASIC;

	// The amount of energy that has passed thru.
	private double electricityCount;

	public TileEntityElectricityMeter() {
		this(MachineType.BASIC_ELECTRICITY_METER, Tier.ElectricityMeter.BASIC);
	}

	public TileEntityElectricityMeter(MachineType machineType, Tier.ElectricityMeter tier) {
		super(machineType, tier.getMaxEnergy(), tier.getMaxTransfer());

		this.tier = tier;

        components.add(new TileOwnableComponent(this));
		components.add(new TileRedstoneControlComponent(this));
	}

    @Override
    public void validate() {
        super.validate();

        PacketHandler.sendToServer(new PacketRequestData(this));
    }

	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);

        isActive = nbtTagCompound.getBoolean("isActive");
		tier = Tier.ElectricityMeter.values()[nbtTagCompound.getInteger("tier")];
		electricityCount = nbtTagCompound.getDouble("electricityCount");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);

		nbtTagCompound.setBoolean("isActive", isActive);
		nbtTagCompound.setInteger("tier", tier.ordinal());
		nbtTagCompound.setDouble("electricityCount", electricityCount);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream) throws Exception {
		super.handlePacketData(dataStream);

        isActive = dataStream.readBoolean();

		// Check if client is in sync with the server, if not update it.
		if (worldObj.isRemote && clientIsActive != isActive) {
			clientIsActive = isActive;

			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		tier = Tier.ElectricityMeter.values()[dataStream.readInt()];
		electricityCount = dataStream.readDouble();
	}

	@Override
	public List<Object> getPacketData(List<Object> list) {
		super.getPacketData(list);

		list.add(isActive);
		list.add(tier.ordinal());
		list.add(electricityCount);

		return list;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		if (getExtractingSides().contains(from)) {
			// Add the amount of energy we're extracting to the counter.
			if (!simulate) {
				electricityCount += maxExtract;
			}
		}

		return super.extractEnergy(from, maxExtract, simulate);
	}

	@Override
	EnumSet<ForgeDirection> getExtractingSides() {
		return EnumSet.of(ForgeDirection.getOrientation(facing).getRotation(ForgeDirection.DOWN));
	}

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

	public Tier.ElectricityMeter getTier() {
		return tier;
	}

	public void setTier(Tier.ElectricityMeter electricityMeterTier) {
		this.tier = electricityMeterTier;
	}

	/**
	 * Returns the amount of energy that this block has totally received.
	 */
	public double getElectricityCount() {
		return electricityCount;
	}

	/**
	 * Sets the amount of energy that this block has totally received.
	 */
	public void setElectricityCount(double electricityCount) {
		this.electricityCount = electricityCount;
	}
}
