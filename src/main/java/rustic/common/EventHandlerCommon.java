package rustic.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rustic.client.models.LiquidBarrelItemModel;
import rustic.client.renderer.LayerIronSkin;
import rustic.common.blocks.IAdvancedRotationPlacement;
import rustic.common.blocks.ModBlocks;
import rustic.common.items.ModItems;
import rustic.common.network.MessageTEUpdate;
import rustic.common.network.PacketHandler;
import rustic.common.potions.PotionTypesRustic;
import rustic.common.tileentity.ITileEntitySyncable;
import rustic.common.util.FluidTextureUtil;

public class EventHandlerCommon {
	
	public static Map<BlockPos, TileEntity> toUpdate = new HashMap<BlockPos, TileEntity>();

	public static void markTEForUpdate(BlockPos pos, TileEntity tile) {
		if (!toUpdate.containsKey(pos)) {
			toUpdate.put(pos, tile);
		} else {
			toUpdate.replace(pos, tile);
		}
	}

	// @SubscribeEvent
	// public void onLivingUpdateEvent(LivingUpdateEvent event) {
	//
	// }

	@SubscribeEvent
	public void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
		ItemStack originalStack = event.getItem();
		if (!originalStack.isEmpty() && originalStack.getItem() instanceof ItemSoup && originalStack.hasTagCompound() && originalStack.getTagCompound().hasKey("oiled")) {
			EntityLivingBase entityLiving = event.getEntityLiving();
			if (entityLiving instanceof EntityPlayer && event.getDuration() == 1) {
				EntityPlayer entityplayer = (EntityPlayer) entityLiving;
				entityplayer.getFoodStats().addStats(2, 0.3F);
			}
		}
	}

	@SubscribeEvent
	public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
		ItemStack originalStack = event.getItem();
		if (!originalStack.isEmpty() && originalStack.getItem() instanceof ItemFood && originalStack.hasTagCompound() && originalStack.getTagCompound().hasKey("oiled")) {
			EntityLivingBase entityLiving = event.getEntityLiving();
			if (entityLiving instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer) entityLiving;
				entityplayer.getFoodStats().addStats(2, 0.3F);
			}
		}
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (!stack.isEmpty() && stack.getItem() instanceof ItemFood && stack.hasTagCompound() && stack.getTagCompound().hasKey("oiled")) {
			event.getToolTip().add(TextFormatting.DARK_GREEN + "" + TextFormatting.ITALIC + I18n.format("tooltip.rustic.olive_oil"));
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (!event.world.isRemote) {
			List<TileEntity> tiles = event.world.loadedTileEntityList;
			NBTTagList list = new NBTTagList();
			for (TileEntity t : tiles) {
				if (t instanceof ITileEntitySyncable) {
					if (((ITileEntitySyncable) t).needsUpdate()) {
						((ITileEntitySyncable) t).clean();
						if (!event.world.isRemote) {
							list.appendTag(t.getUpdateTag());
						}
					}
				}
			}
			for (TileEntity t : toUpdate.values()) {
				if (!event.world.isRemote) {
					list.appendTag(t.getUpdateTag());
				}
			}
			if (!list.hasNoTags()) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setTag("data", list);
				PacketHandler.INSTANCE.sendToAll(new MessageTEUpdate(tag));
			}
			toUpdate.clear();
		}
	}

}