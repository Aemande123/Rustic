package rustic.common.blocks;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rustic.core.Rustic;

public class BlockChandelier extends BlockFallingBase {

	public BlockChandelier() {
		super(Material.IRON, "chandelier");
		this.setHardness(2F);
		setSoundType(SoundType.ANVIL);
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess ba, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.UP || side == EnumFacing.DOWN) {
			return false;
		}
		return true;
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}
	
	@Override
	protected void onStartFalling(EntityFallingBlock fallingEntity) {
		fallingEntity.setHurtEntities(true);
		for (Field field : fallingEntity.getClass().getDeclaredFields()) {
			if (field.getName().equals("fallHurtMax") || field.getName().equals("field_145815_h")) {
				field.setAccessible(true);
				try {
					field.setInt(fallingEntity, 400);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Rustic.logger.warn("Error setting falling chandelier maximum damage with reflection", e);
				}
			} else if (field.getName().equals("fallHurtAmount") || field.getName().equals("field_145816_i")) {
				field.setAccessible(true);
				try {
					field.setFloat(fallingEntity, 6F);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Rustic.logger.warn("Error setting falling chandelier damage with reflection", e);
				}
			}
		}
	}

	@Override
	public void onEndFalling(World worldIn, BlockPos pos, IBlockState p_176502_3_, IBlockState p_176502_4_) {
		worldIn.playEvent(1031, pos, 0);
	}

	@Override
	public void onBroken(World worldIn, BlockPos pos) {
		worldIn.playEvent(1029, pos, 0);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!worldIn.isRemote) {
			this.checkFallable(worldIn, pos);
		}
	}
	
	private boolean suspended(World worldIn, BlockPos pos) {
		IBlockState state = worldIn.getBlockState(pos.up());
		if (worldIn.isSideSolid(pos.up(), EnumFacing.DOWN, false)) {
			return true;
		}
		if (state.getBlock() == ModBlocks.ROPE && state.getValue(BlockRope.AXIS) == EnumFacing.Axis.Y) {
			return true;
		}
		if (state.getBlock() == ModBlocks.CHAIN && state.getValue(BlockChain.AXIS) == EnumFacing.Axis.Y) {
			return true;
		}
		
		return false;
	}

	private void checkFallable(World worldIn, BlockPos pos) {
		if ((worldIn.isAirBlock(pos.down()) || canFallThrough(worldIn.getBlockState(pos.down()))) && pos.getY() >= 0 && !suspended(worldIn, pos)) { // improve logic
			int i = 32;

			if (!fallInstantly && worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
				if (!worldIn.isRemote) {
					EntityFallingBlock entityfallingblock = new EntityFallingBlock(worldIn, (double) pos.getX() + 0.5D,
							(double) pos.getY(), (double) pos.getZ() + 0.5D, worldIn.getBlockState(pos));
					this.onStartFalling(entityfallingblock);
					worldIn.spawnEntity(entityfallingblock);
				}
			} else {
				IBlockState state = worldIn.getBlockState(pos);
				worldIn.setBlockToAir(pos);
				BlockPos blockpos;

				for (blockpos = pos
						.down(); (worldIn.isAirBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos)))
								&& blockpos.getY() > 0; blockpos = blockpos.down()) {
					;
				}

				if (blockpos.getY() > 0) {
					worldIn.setBlockState(blockpos.up(), state);
				}
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		
    }
	
	public static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 0.25, 1);
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB;
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.DOWN) {
			return BlockFaceShape.BOWL;
		}
		if (side == EnumFacing.UP) {
			return BlockFaceShape.CENTER;
		}
		return BlockFaceShape.SOLID;
	}

}
