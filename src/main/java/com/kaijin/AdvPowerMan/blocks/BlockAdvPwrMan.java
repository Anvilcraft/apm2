/*******************************************************************************
 * Copyright (c) 2012-2013 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/
package com.kaijin.AdvPowerMan.blocks;

import java.util.List;
import java.util.Random;

import com.kaijin.AdvPowerMan.AdvancedPowerManagement;
import com.kaijin.AdvPowerMan.Info;
import com.kaijin.AdvPowerMan.tileentities.TEAdjustableTransformer;
import com.kaijin.AdvPowerMan.tileentities.TEAdvEmitter;
import com.kaijin.AdvPowerMan.tileentities.TEBatteryStation;
import com.kaijin.AdvPowerMan.tileentities.TEChargingBench;
import com.kaijin.AdvPowerMan.tileentities.TECommon;
import com.kaijin.AdvPowerMan.tileentities.TEStorageMonitor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAdvPwrMan extends BlockContainer{
	static final String[] tierPrefix = {"LV", "MV", "HV", "EV"};
	
	protected IIcon benchBottom;
	protected IIcon smTop;
	protected IIcon smBottom;
	protected IIcon smInvalid;
	protected IIcon emitter;
	protected IIcon atOut;
	protected IIcon atInput;
	protected IIcon[] atOutput;
	protected IIcon[] benchTop;
	protected IIcon[][][] cbSides;
	protected IIcon[][] bsSides;
	protected IIcon[][] smSides;
	
	public BlockAdvPwrMan(Material material) {
		super(material);
		setHardness(0.75F);
		setResistance(5F);
		setStepSound(soundTypeStone);
		// setUnlocalizedName("AdvPwrMan");
		setCreativeTab(CreativeTabs.tabDecorations);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(Item block, CreativeTabs creativetabs, List list){
		for(int i = 0; i <= Info.LAST_META_VALUE; ++i){
			if(i >= 3 && i <= 5)
				continue; // Don't add legacy emitters to creative inventory
			list.add(new ItemStack(block, 1, i));
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9){
		// int currentEquippedItemID = 0; //TODO We're not currently responding
		// to wrenches
		
		// if (entityplayer.getCurrentEquippedItem() != null)
		// {
		// currentEquippedItemID = entityplayer.getCurrentEquippedItem().itemID;
		// }
		
		// if (entityplayer.isSneaking() || currentEquippedItemID ==
		// Info.ic2WrenchID || currentEquippedItemID ==
		// Info.ic2ElectricWrenchID)
		if(entityplayer.isSneaking()){
			// Prevent GUI popup when sneaking - this allows you to place things
			// directly on blocks
			return false;
		}
		
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof TECommon){
			final int id = ((TECommon) tile).getGuiID();
			if(id < 1)
				return false;
			if(AdvancedPowerManagement.proxy.isServer()){
				entityplayer.openGui(AdvancedPowerManagement.instance, id, world, x, y, z);
			}
		}
		
		return true;
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister){
		cbSides = new IIcon[3][2][13];
		bsSides = new IIcon[3][2];
		smSides = new IIcon[2][13];
		benchTop = new IIcon[3];
		atOutput = new IIcon[4];
		
		benchBottom = iconRegister.registerIcon(Info.TITLE_PACKED + ":BenchBottom");
		smTop = iconRegister.registerIcon(Info.TITLE_PACKED + ":StorageMonitorTop");
		smBottom = iconRegister.registerIcon(Info.TITLE_PACKED + ":StorageMonitorBottom");
		smInvalid = iconRegister.registerIcon(Info.TITLE_PACKED + ":StorageMonitorInvalid");
		emitter = iconRegister.registerIcon(Info.TITLE_PACKED + ":Emitter");
		atInput = iconRegister.registerIcon(Info.TITLE_PACKED + ":TransformerInput");
		
		int i, j;
		for(i = 0; i < 13; i++){
			String temp = Integer.toString(i);
			for(j = 0; j < 3; j++){
				cbSides[j][0][i] = iconRegister.registerIcon(Info.TITLE_PACKED + ":" + tierPrefix[j] + "ChargingBenchOff" + temp);
				cbSides[j][1][i] = iconRegister.registerIcon(Info.TITLE_PACKED + ":" + tierPrefix[j] + "ChargingBenchOn" + temp);
			}
			smSides[0][i] = iconRegister.registerIcon(Info.TITLE_PACKED + ":StorageMonitorOff" + temp);
			smSides[1][i] = iconRegister.registerIcon(Info.TITLE_PACKED + ":StorageMonitorOn" + temp);
		}
		for(i = 0; i < 3; i++){
			benchTop[i] = iconRegister.registerIcon(Info.TITLE_PACKED + ":" + tierPrefix[i] + "BenchTop");
			bsSides[i][0] = iconRegister.registerIcon(Info.TITLE_PACKED + ":" + tierPrefix[i] + "BatteryStationOff");
			bsSides[i][1] = iconRegister.registerIcon(Info.TITLE_PACKED + ":" + tierPrefix[i] + "BatteryStationOn");
		}
		for(i = 0; i < 4; i++){
			atOutput[i] = iconRegister.registerIcon(Info.TITLE_PACKED + ":TransformerOutput1" + tierPrefix[i]);
		}
	}
	
	// Textures in the world
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess blocks, int x, int y, int z, int side){
		final int meta = blocks.getBlockMetadata(x, y, z);
		TileEntity tile = blocks.getTileEntity(x, y, z);
		if(tile instanceof TEChargingBench){
			switch(side){
			case 0: // bottom
				return benchBottom;
				
			case 1: // top
				return benchTop[meta - Info.CB_META];
				
			default:
				return cbSides[meta - Info.CB_META][((TEChargingBench) tile).doingWork ? 1 : 0][((TEChargingBench) tile).chargeLevel];
			}
		}else if(tile instanceof TEAdvEmitter){
			return emitter;
		}else if(tile instanceof TEAdjustableTransformer){
			final byte flags = ((TEAdjustableTransformer) tile).sideSettings[side];
			if((flags & 1) == 0)
				return atInput;
			return atOutput[(flags >>> 1) & 3];
		}else if(tile instanceof TEBatteryStation){
			switch(side){
			case 0: // bottom
				return benchBottom;
				
			case 1: // top
				return benchTop[meta - Info.BS_META];
				
			default:
				return bsSides[meta - Info.BS_META][((TEBatteryStation) tile).doingWork ? 1 : 0];
			}
		}else if(tile instanceof TEStorageMonitor){
			switch(side){
			case 0: // bottom
				return smBottom;
				
			case 1: // top
				return smTop;
				
			default:
				if(((TEStorageMonitor) tile).blockState){
					return smSides[((TEStorageMonitor) tile).isPowering ? 1 : 0][((TEStorageMonitor) tile).chargeLevel];
				}else
					return smInvalid;
			}
		}
		
		// If we're here, something is wrong
		return benchBottom;
	}
	
	// Textures in your inventory
	@Override
	public IIcon getIcon(int side, int meta){
		if(meta == Info.AE_META){
			return emitter;
		}
		if(meta == Info.AT_META){
			// TODO: Give transformer better textures
			return atInput;
		}
		switch(side){
		case 0: // bottom
			return meta == Info.SM_META ? smBottom : benchBottom;
			
		case 1: // top
			if(meta < 3) // CB tops
			{
				return benchTop[meta - Info.CB_META];
			}else if(meta < 11) // Battery Station top
			{
				return benchTop[meta - Info.BS_META];
			}else{
				return smTop;
			}
			
		default: // side
			if(meta < 3) // Charging Bench
			{
				return cbSides[meta - Info.CB_META][0][0];
			}else if(meta < 11) // Battery Station
			{
				return bsSides[meta - Info.BS_META][0];
			}else{
				return smInvalid;
			}
		}
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess block, int x, int y, int z, int side){
		System.out.println("Get Redstone Power");
		TileEntity tile = block.getTileEntity(x, y, z);
		return tile instanceof TEStorageMonitor && ((TEStorageMonitor) tile).isPowering ? 15 : 0; // TODO
																									// Verify
																									// this
																									// works
																									// properly
	}
	
	@Override
	public int isProvidingStrongPower(IBlockAccess block, int x, int y, int z, int side){
		return 0;
	}
	
	@Override
	public boolean canProvidePower(){
		return true;
	}
	
	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int direction){
		return true;
	}
	
	@Override
	public boolean isBlockNormalCube(){
		return false;
	}
	
	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side){
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return null;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata){
		// if (ChargingBench.isDebugging)
		// System.out.println("BlockAdvPwrMan.createTileEntity");
		switch(metadata){
		case 0:
			return new TEChargingBench(1);
			
		case 1:
			return new TEChargingBench(2);
			
		case 2:
			return new TEChargingBench(3);
			
		case 3:
			return new TEAdvEmitter(1); // Update old emitter tier 1
			
		case 4:
			return new TEAdvEmitter(2); // Update old emitter tier 2
			
		case 5:
			return new TEAdvEmitter(3); // Update old emitter tier 3
			
		case 6:
			return new TEAdjustableTransformer();
			
		case 7:
			return new TEAdvEmitter();
			
		case 8:
			return new TEBatteryStation(1);
			
		case 9:
			return new TEBatteryStation(2);
			
		case 10:
			return new TEBatteryStation(3);
			
		case 11:
			return new TEStorageMonitor();
			
		default:
			return null;
		}
	}
	
	@Override
	public boolean hasTileEntity(int metadata){
		return metadata >= 0 && metadata <= Info.LAST_META_VALUE;
	}
	
	/*
	 * @Override public Item getItemDropped(int var1, Random var2, int var3) {
	 * //if (ChargingBench.isDebugging)
	 * System.out.println("BlockAdvPwrMan.idDropped"); return blockID; }
	 */
	@Override
	public int damageDropped(int meta){
		// if (ChargingBench.isDebugging)
		// System.out.println("BlockAdvPwrMan.damageDropped");
		return meta;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta){
		preDestroyBlock(world, x, y, z);
	}
	
	public static void preDestroyBlock(World world, int i, int j, int k){
		if(!AdvancedPowerManagement.proxy.isClient()){
			TileEntity tile = world.getTileEntity(i, j, k);
			if(tile == null)
				return;
			try{
				((TECommon) tile).dropContents();
			}catch(ClassCastException e){
				FMLLog.warning("[AdvancedPowerManagement] " + "Attempted to destroy APM block with non-APM tile entity at: " + i + ", " + j + ", "
						+ k);
			}
			tile.invalidate();
		}
	}
}
