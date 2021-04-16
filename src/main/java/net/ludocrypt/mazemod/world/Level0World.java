package net.ludocrypt.mazemod.world;

import java.util.Map;
import java.util.OptionalLong;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.ludocrypt.exdimapi.api.ExtraDimension;
import net.ludocrypt.exdimapi.mixin.DimensionTypeAccessor;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.SkyProperties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource.NoiseParameters;
import net.minecraft.world.biome.source.VoronoiBiomeAccessType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public class Level0World extends ExtraDimension {

	public static final Identifier LEVEL_0_ID = new Identifier("mazemod", "level_0");
	public static final Map<RegistryKey<Biome>, Biome.MixedNoisePoint> NOISE_POINTS = Maps.newHashMap();
	public static final NoiseParameters DEFAULT = new NoiseParameters(7, ImmutableList.of(1.0D));

	public Level0World() {
		super(LEVEL_0_ID, DimensionTypeAccessor.createDimensionType(OptionalLong.empty(), true, false, false, false, 1, false, false, false, false, false, 256, VoronoiBiomeAccessType.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getId(), new Identifier("overworld"), 1.0F), new SkyProperties.Overworld(), (dim, client, ci) -> {
		}, DEFAULT, DEFAULT, DEFAULT, DEFAULT, null, NOISE_POINTS);
	}

	@Override
	public ChunkGenerator createGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
		return new MazeChunkGenerator(4, 8, 100, Blocks.YELLOW_CONCRETE.getDefaultState(), Blocks.YELLOW_WOOL.getDefaultState(), ImmutableList.of(false, true, true, true, false), ImmutableList.of(false, true, false, false, true), ImmutableList.of(true, false, true, true, false), ImmutableList.of(false, true, false, true, true), BIOME_SOURCE_PRESET.getBiomeSource(biomeRegistry, seed), seed);
	}

	@Override
	public void init() {

	}

	static {
		NOISE_POINTS.put(BiomeKeys.THE_VOID, new Biome.MixedNoisePoint(0, 0, 0, 0, 0));
	}

}
