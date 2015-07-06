package org.halvors.electrometrics.common.tileentity;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import org.halvors.electrometrics.common.util.Utils;

import java.util.EnumSet;

/**
 * When extended, this makes a TileEntity able to provide electricity.
 *
 * @author halvors
 */
public abstract class TileEntityEnergyProvider extends TileEntityEnergyReceiver implements IEnergyProvider {
	public TileEntityEnergyProvider(String name, int maxEnergy) {
		super(name, maxEnergy);
	}

	public TileEntityEnergyProvider(String name, int maxEnergy, int maxReceive) {
		super(name, maxEnergy, maxReceive);
	}

	TileEntityEnergyProvider(String name, int maxEnergy, int maxReceive, int maxExtract) {
		super(name, maxEnergy, maxReceive, maxExtract);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!worldObj.isRemote && Utils.canFunction(this)) {
			if (storage.getEnergyStored() > 0) {
				transferEnergy();
			}
		}
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		if (getExtractingSides().contains(from)) {
			return storage.extractEnergy(maxExtract, simulate);
		}

		return 0;
	}

	@Override
	public EnumSet<ForgeDirection> getReceivingSides() {
		EnumSet<ForgeDirection> directions = EnumSet.allOf(ForgeDirection.class);
		directions.removeAll(getExtractingSides());
		directions.remove(ForgeDirection.UNKNOWN);

		return directions;
	}

	@Override
	public EnumSet<ForgeDirection> getExtractingSides() {
		return EnumSet.of(ForgeDirection.getOrientation(facing));
	}

	/**
	 * Transfer energy to any blocks demanding energy that are connected to
	 * this one.
	 */
	private void transferEnergy() {
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tileEntity = worldObj.getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);

			if (tileEntity instanceof IEnergyReceiver) {
				IEnergyReceiver receiver = (IEnergyReceiver) tileEntity;

				extractEnergy(direction.getOpposite(), receiver.receiveEnergy(direction.getOpposite(), storage.getEnergyStored(), false), false);
			}
		}
	}
}
