package net.capsey.archeology.items;

import net.capsey.archeology.ArcheologyMod;
import net.capsey.archeology.PlayerEntityMixinInterface;
import net.capsey.archeology.blocks.excavation_block.ExcavationBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable.OxidizationLevel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CopperBrushItem extends Item {

	public static OxidizationLevel getOxidizationLevel(ItemStack item) {
		int index = (int) Math.floor(4 * item.getDamage() / item.getMaxDamage());

        switch (index) {
            case 0: return OxidizationLevel.UNAFFECTED;
            case 1: return OxidizationLevel.EXPOSED;
            case 2: return OxidizationLevel.WEATHERED;
            case 3: return OxidizationLevel.OXIDIZED;
        }

		return OxidizationLevel.OXIDIZED;
	}

	private static final int[] BRUSH_TICKS = { 8, 7, 6, 5 };

	public static int getBrushTicks(OxidizationLevel level) {
		switch (level) {
            case UNAFFECTED: return BRUSH_TICKS[0];
            case EXPOSED: return BRUSH_TICKS[0];
            case WEATHERED: return BRUSH_TICKS[0];
            case OXIDIZED: return BRUSH_TICKS[0];
            default: throw new IllegalArgumentException();
        }
	}

    public CopperBrushItem(Settings settings) {
        super(settings);
    }

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();

		if (!world.isClient) {
			BlockPos pos = context.getBlockPos();
			Block block = world.getBlockState(pos).getBlock();

			if (block instanceof ExcavationBlock) {
				PlayerEntity player = context.getPlayer();
				float cooldown = ((PlayerEntityMixinInterface) player).getBrushCooldownProgress();
	
				if (cooldown >= 1) {
					ItemStack stack = context.getStack();

					if (((ExcavationBlock) block).startBrushing(world, pos, player, stack)) {
						player.setCurrentHand(context.getHand());
						return ActionResult.CONSUME;
					}
				}
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		// TODO: Remove hardcoded player reach value
		HitResult raycast = user.raycast(4.5F, 1, false);

		if (raycast instanceof BlockHitResult) {
			BlockPos pos = ((BlockHitResult) raycast).getBlockPos();
			int brushTicks = CopperBrushItem.getBrushTicks(getOxidizationLevel(user.getActiveItem()));

			if ((remainingUseTicks + 1) % brushTicks == 0) {
				BlockState state = world.getBlockState(pos);

				if (!world.isClient) {
					if ((remainingUseTicks + 1) % brushTicks * ExcavationBlock.getBrushTicksPerLayer(world.getDifficulty()) == 0) {
						int damage = world.getRandom().nextInt(2);
						stack.damage(damage, user, p -> p.sendEquipmentBreakStatus(user.getActiveHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));					
					}

					BlockSoundGroup soundGroup = state.getSoundGroup();
					world.playSound(null, pos, soundGroup.getBreakSound(), SoundCategory.BLOCKS, 0.3F * soundGroup.getVolume(), soundGroup.getPitch());
					world.playSound(null, user.getBlockPos(), ArcheologyMod.BRUSHING_SOUND_EVENT, SoundCategory.PLAYERS, 1f, 1f);
				}

				world.addBlockBreakParticles(pos, state);
			}
		}
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 6000;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return CustomUseAction.BRUSH;
	}

	@Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
		return ingredient.getItem() == Items.COPPER_INGOT;
	}

}
