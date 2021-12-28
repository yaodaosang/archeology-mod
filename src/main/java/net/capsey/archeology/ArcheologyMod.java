package net.capsey.archeology;

import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.capsey.archeology.blocks.clay_pot.ClayPotBlock;
import net.capsey.archeology.blocks.clay_pot.ClayPotBlockEntity;
import net.capsey.archeology.blocks.clay_pot.RawClayPotBlock;
import net.capsey.archeology.blocks.clay_pot.RawClayPotBlockEntity;
import net.capsey.archeology.blocks.excavation_block.ExcavationBlock;
import net.capsey.archeology.blocks.excavation_block.ExcavationBlockEntity;
import net.capsey.archeology.blocks.excavation_block.FallingExcavationBlock;
import net.capsey.archeology.items.CeramicShards;
import net.capsey.archeology.items.CopperBrushItem;
import net.capsey.archeology.world.gen.AncientRuinsFeature;
import net.capsey.archeology.world.gen.AncientRuinsGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePieceType.ManagerAware;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class ArcheologyMod implements ModInitializer {

    public static final String MODID = "archeology";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static class Items {

        public static final Item COPPER_BRUSH = new CopperBrushItem(new Item.Settings().maxDamage(64).group(ItemGroup.TOOLS));

        public static final Item EXCAVATION_DIRT = new BlockItem(Blocks.EXCAVATION_DIRT, new FabricItemSettings().group(ItemGroup.DECORATIONS));
        public static final Item EXCAVATION_GRAVEL = new BlockItem(Blocks.EXCAVATION_GRAVEL, new FabricItemSettings().group(ItemGroup.DECORATIONS));
    
        public static final Item RAW_CLAY_POT = new BlockItem(Blocks.RAW_CLAY_POT, new FabricItemSettings().group(ItemGroup.DECORATIONS));
        public static final Item CLAY_POT = new BlockItem(Blocks.CLAY_POT, new FabricItemSettings().maxCount(1).group(ItemGroup.DECORATIONS));

        public static void onInitialize() {
            Registry.register(Registry.ITEM, new Identifier(MODID, "copper_brush"), COPPER_BRUSH);

            Registry.register(Registry.ITEM, new Identifier(MODID, "excavation_dirt"), EXCAVATION_DIRT);
            Registry.register(Registry.ITEM, new Identifier(MODID, "excavation_gravel"), EXCAVATION_GRAVEL);

            Registry.register(Registry.ITEM, new Identifier(MODID, "raw_clay_pot"), RAW_CLAY_POT);
            Registry.register(Registry.ITEM, new Identifier(MODID, "clay_pot"), CLAY_POT);
        }

    }

    public static class Blocks {

        public static final Block EXCAVATION_DIRT = new ExcavationBlock(FabricBlockSettings.copyOf(net.minecraft.block.Blocks.DIRT).hardness(1.0F));
        public static final Block EXCAVATION_GRAVEL = new FallingExcavationBlock(FabricBlockSettings.copyOf(net.minecraft.block.Blocks.GRAVEL).hardness(1.2F), (FallingBlock) net.minecraft.block.Blocks.GRAVEL);
    
        public static final Block RAW_CLAY_POT = new RawClayPotBlock(FabricBlockSettings.copyOf(net.minecraft.block.Blocks.CLAY).nonOpaque());
        public static final Block CLAY_POT = new ClayPotBlock(FabricBlockSettings.copyOf(net.minecraft.block.Blocks.TERRACOTTA).nonOpaque().strength(0.6F).sounds(ClayPotBlock.SOUND_GROUP));

        public static void onInitialize() {
            Registry.register(Registry.BLOCK, new Identifier(MODID, "excavation_dirt"), EXCAVATION_DIRT);
            Registry.register(Registry.BLOCK, new Identifier(MODID, "excavation_gravel"), EXCAVATION_GRAVEL);
    
            Registry.register(Registry.BLOCK, new Identifier(MODID, "raw_clay_pot"), RAW_CLAY_POT);
            Registry.register(Registry.BLOCK, new Identifier(MODID, "clay_pot"), CLAY_POT);
        }

    }

    public static class BlockEntities {

        public static BlockEntityType<ExcavationBlockEntity> EXCAVATION_BLOCK_ENTITY;
        public static BlockEntityType<RawClayPotBlockEntity> RAW_CLAY_POT_BLOCK_ENTITY;
        public static BlockEntityType<ClayPotBlockEntity> CLAY_POT_BLOCK_ENTITY;

        public static void onInitialize() {
            EXCAVATION_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":excavation_block_entity", FabricBlockEntityTypeBuilder.create(ExcavationBlockEntity::new, Blocks.EXCAVATION_DIRT, Blocks.EXCAVATION_GRAVEL).build(null));
            RAW_CLAY_POT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":raw_clay_pot_block_entity", FabricBlockEntityTypeBuilder.create(RawClayPotBlockEntity::new, Blocks.RAW_CLAY_POT).build(null));
            CLAY_POT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, MODID + ":clay_pot_block_entity", FabricBlockEntityTypeBuilder.create(ClayPotBlockEntity::new, Blocks.CLAY_POT).build(null));
        }

    }

    public static class Sounds {

        public static final Identifier BRUSHING_SOUND_ID = new Identifier(MODID, "item.copper_brush.brushing");
        public static final SoundEvent BRUSHING_SOUND_EVENT = new SoundEvent(BRUSHING_SOUND_ID);

    }

    public static class Features {

        public static final StructurePieceType ANCIENT_RUINS_PIECE = Registry.register(Registry.STRUCTURE_PIECE, "aruins", (ManagerAware) AncientRuinsGenerator.Piece::new);

        private static final StructureFeature<DefaultFeatureConfig> ANCIENT_RUINS = new AncientRuinsFeature(DefaultFeatureConfig.CODEC);
        private static final ConfiguredStructureFeature<?, ?> CONFIGURED_ANCIENT_RUINS = ANCIENT_RUINS.configure(FeatureConfig.DEFAULT);

        private static final List<RegistryKey<Biome>> ANCIENT_RUINS_BIOMES = List.of(BiomeKeys.PLAINS, BiomeKeys.SAVANNA, BiomeKeys.DESERT, BiomeKeys.FOREST, BiomeKeys.BIRCH_FOREST, BiomeKeys.TAIGA);

        public static void onInitialize() {
            FabricStructureBuilder.create(new Identifier(MODID, "ancient_ruins"), ANCIENT_RUINS)
                    .step(GenerationStep.Feature.SURFACE_STRUCTURES)
                    .defaultConfig(new StructureConfig(20, 8, 14357621))
                    .adjustsSurface()
                    .register();

            RegistryKey<ConfiguredStructureFeature<?, ?>> ancientRuins = RegistryKey.of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, new Identifier(MODID, "ancient_ruins"));
            Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, ancientRuins.getValue(), CONFIGURED_ANCIENT_RUINS);

            BiomeModifications.create(new Identifier(MODID, "ancient_ruins_addition")).add(ModificationPhase.ADDITIONS,
                context -> ANCIENT_RUINS_BIOMES.contains(context.getBiomeKey()),
                context -> context.getGenerationSettings().addBuiltInStructure(CONFIGURED_ANCIENT_RUINS)
            );
        }

    }

    // Block Tags
    public static final Tag<Block> EXCAVATION_BLOCKS_TAG = TagFactory.BLOCK.create(new Identifier(MODID, "excavation_blocks"));

    public static final Tag<Block> CLAY_POTS_TAG = TagFactory.BLOCK.create(new Identifier(MODID, "clay_pots"));
    public static final Tag<Block> CLAY_POT_PLANTABLE_TAG = TagFactory.BLOCK.create(new Identifier(MODID, "clay_pot_plantable"));

    // Loot context type for Fossil Container
    public static final LootContextType EXCAVATION_LOOT_CONTEXT_TYPE = createLootContextType(builder -> {
		builder.require(LootContextParameters.TOOL)
                .allow(LootContextParameters.THIS_ENTITY)
                .allow(LootContextParameters.BLOCK_ENTITY);
	});

    // Player Statistics
    public static final Identifier EXCAVATED = new Identifier(MODID, "excavated");

    // S2C Network Packet ID
    public static final Identifier START_BRUSHING = new Identifier(MODID, "start_brushing");

    @Override
    public void onInitialize() {
        Items.onInitialize();
        CeramicShards.registerDefaultShards();
        Blocks.onInitialize();
        BlockEntities.onInitialize();
        Features.onInitialize();
        
        Registry.register(Registry.SOUND_EVENT, Sounds.BRUSHING_SOUND_ID, Sounds.BRUSHING_SOUND_EVENT);
        Registry.register(Registry.CUSTOM_STAT, "excavated", EXCAVATED);
        Stats.CUSTOM.getOrCreateStat(EXCAVATED, StatFormatter.DEFAULT);
    }

    private static LootContextType createLootContextType(Consumer<LootContextType.Builder> type) {
		LootContextType.Builder builder = new LootContextType.Builder();
		type.accept(builder);
		return builder.build();
	}
    
}
