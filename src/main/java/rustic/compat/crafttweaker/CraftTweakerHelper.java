package rustic.compat.crafttweaker;

import mezz.jei.api.recipe.IRecipeWrapper;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.oredict.IOreDictEntry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import rustic.compat.jei.RusticJEIPlugin;

public class CraftTweakerHelper {

	public static void preInit() {
		CraftTweakerAPI.registerClass(CrushingTub.class);
		CraftTweakerAPI.registerClass(EvaporatingBasin.class);
		CraftTweakerAPI.registerClass(Condenser.class);
	}

	public static ItemStack toStack(IItemStack iStack) {
		if (iStack == null)
			return ItemStack.EMPTY;
		return (ItemStack) iStack.getInternal();
	}
	
	public static ItemStack[] toStacks(IItemStack[] iStacks) {
		if (iStacks == null) {
			return new ItemStack[0];
		}
		ItemStack[] ret = new ItemStack[iStacks.length];
		for (int i = 0; i < iStacks.length; i++) {
			ret[i] = toStack(iStacks[i]);
		}
		return ret;
	}

	public static Object toObject(IIngredient iStack) {
		if (iStack == null)
			return null;
		else {
			if (iStack instanceof IOreDictEntry)
				return ((IOreDictEntry) iStack).getName();
			else if (iStack instanceof IItemStack)
				return toStack((IItemStack) iStack);
			else if (iStack instanceof IngredientStack) {
				IIngredient ingr = ReflectionHelper.getPrivateValue(IngredientStack.class, (IngredientStack) iStack, "ingredient");
				return toObject(ingr);
			} else
				return null;
		}
	}

	public static Object[] toObjects(IIngredient[] iStacks) {
		Object[] oA = new Object[iStacks.length];
		for (int i = 0; i < iStacks.length; i++)
			oA[i] = toObject(iStacks[i]);
		return oA;
	}

	public static FluidStack toFluidStack(ILiquidStack iStack) {
		if (iStack == null) {
			return null;
		}
		return (FluidStack) iStack.getInternal();
	}
	
}
