package net.ludocrypt.mazemod.world;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

public class MazeChunkGenerator extends ChunkGenerator {

	private BiomeSource biomeSource;
	private long seed;
	private int height;
	private int width;
	private int seaLevel;
	private BlockState walls;
	private BlockState base;
	private BlockState[] states;
	private ImmutableList<Pair<Boolean, OctavePerlinNoiseSampler>> noisemapOne = ImmutableList.of();
	private ImmutableList<Pair<Boolean, OctavePerlinNoiseSampler>> noisemapTwo = ImmutableList.of();
	private ImmutableList<Pair<Boolean, OctavePerlinNoiseSampler>> noisemapThree = ImmutableList.of();
	private ImmutableList<Pair<Boolean, OctavePerlinNoiseSampler>> noisemapFour = ImmutableList.of();
	private List<Boolean> northNoisemap;
	private List<Boolean> eastNoisemap;
	private List<Boolean> southNoisemap;
	private List<Boolean> westNoisemap;

	public static final Codec<MazeChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
		return instance.group(Codec.INT.fieldOf("height").forGetter((chunkGenerator) -> {
			return chunkGenerator.height;
		}), Codec.INT.fieldOf("width").forGetter((chunkGenerator) -> {
			return chunkGenerator.width;
		}), Codec.INT.fieldOf("sea_level").forGetter((chunkGenerator) -> {
			return chunkGenerator.seaLevel;
		}), BlockState.CODEC.fieldOf("wall_block").forGetter((chunkGenerator) -> {
			return chunkGenerator.walls;
		}), BlockState.CODEC.fieldOf("base_block").forGetter((chunkGenerator) -> {
			return chunkGenerator.base;
		}), Codec.list(Codec.BOOL).fieldOf("north_noisemap").forGetter((chunkGenerator) -> {
			return chunkGenerator.northNoisemap;
		}), Codec.list(Codec.BOOL).fieldOf("east_noisemap").forGetter((chunkGenerator) -> {
			return chunkGenerator.eastNoisemap;
		}), Codec.list(Codec.BOOL).fieldOf("south_noisemap").forGetter((chunkGenerator) -> {
			return chunkGenerator.southNoisemap;
		}), Codec.list(Codec.BOOL).fieldOf("west_noisemap").forGetter((chunkGenerator) -> {
			return chunkGenerator.westNoisemap;
		}), BiomeSource.CODEC.fieldOf("biome_source").forGetter((chunkGenerator) -> {
			return chunkGenerator.biomeSource;
		}), Codec.LONG.fieldOf("seed").forGetter((chunkGenerator) -> {
			return chunkGenerator.seed;
		})).apply(instance, instance.stable(MazeChunkGenerator::new));
	});

	public MazeChunkGenerator(int height, int width, int seaLevel, BlockState walls, BlockState base, List<Boolean> northNoisemap, List<Boolean> eastNoisemap, List<Boolean> southNoisemap, List<Boolean> westNoisemap, BiomeSource biomeSource, long seed) {
		super(biomeSource, biomeSource, new StructuresConfig(false), seed);
		this.height = height;
		this.width = width;
		this.walls = walls;
		this.base = base;
		this.states = new BlockState[] { base, walls, Blocks.AIR.getDefaultState() };
		this.northNoisemap = northNoisemap;
		this.eastNoisemap = eastNoisemap;
		this.southNoisemap = southNoisemap;
		this.westNoisemap = westNoisemap;
		this.seaLevel = seaLevel;
		this.biomeSource = biomeSource;
		this.seed = seed;
		this.noisemapOne = createNoise(northNoisemap, seed);
		this.noisemapTwo = createNoise(eastNoisemap, seed);
		this.noisemapThree = createNoise(southNoisemap, seed);
		this.noisemapFour = createNoise(westNoisemap, seed);
	}

	@Override
	protected Codec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long seed) {
		return new MazeChunkGenerator(this.height, this.width, this.seaLevel, this.walls, this.base, this.northNoisemap, this.eastNoisemap, this.southNoisemap, this.westNoisemap, this.biomeSource, seed);
	}

	@Override
	public void buildSurface(ChunkRegion world, Chunk chunk) {

	}

	@Override
	public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
		for (int y = 0; y < 255; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					BlockPos pos = chunk.getPos().getStartPos().add(x, y, z);
					if (pos.getY() < this.getSeaLevel() || pos.getY() >= this.getSeaLevel() + this.height) {
						world.setBlockState(pos, states[0], 3);
					} else {
						Random random = new Random(seed ^ pos.getX() * pos.getZ() ^ chunk.getPos().toLong());
						// Rooms
						boolean north = this.noisemapOne.stream().max(Comparator.comparing((entry) -> {
							return ((Pair<Boolean, OctavePerlinNoiseSampler>) entry).getRight().sample(pos.getX() / 5, 0, pos.getZ() / 5);
						})).get().getLeft();
						boolean east = this.noisemapTwo.stream().max(Comparator.comparing((entry) -> {
							return ((Pair<Boolean, OctavePerlinNoiseSampler>) entry).getRight().sample(pos.getX() / 5, 0, pos.getZ() / 5);
						})).get().getLeft();
						boolean south = this.noisemapThree.stream().max(Comparator.comparing((entry) -> {
							return ((Pair<Boolean, OctavePerlinNoiseSampler>) entry).getRight().sample(pos.getX() / 5, 0, pos.getZ() / 5);
						})).get().getLeft();
						boolean west = this.noisemapFour.stream().max(Comparator.comparing((entry) -> {
							return ((Pair<Boolean, OctavePerlinNoiseSampler>) entry).getRight().sample(pos.getX() / 5, 0, pos.getZ() / 5);
						})).get().getLeft();

						int size = width;
						if (pos.getX() % size == 0 && pos.getZ() % size == 0) {
							buildRoom(world, pos, size, random.nextBoolean() && random.nextBoolean() && random.nextBoolean() ? north : !north, random.nextBoolean() && random.nextBoolean() && random.nextBoolean() ? east : !east, random.nextBoolean() && random.nextBoolean() && random.nextBoolean() ? south : !south, random.nextBoolean() && random.nextBoolean() && random.nextBoolean() ? west : !west, states[1]);
							// Cleanup
							if (world.getBlockState(pos).isOf(states[1].getBlock())) {
								if (world.getBlockState(pos.north()).isOf(states[1].getBlock()) && world.getBlockState(pos.east()).isOf(states[1].getBlock()) && world.getBlockState(pos.south()).isOf(states[1].getBlock()) && world.getBlockState(pos.west()).isOf(states[1].getBlock())) {
									buildWalls(world, pos, size, random.nextBoolean() && random.nextBoolean(), random.nextBoolean() && random.nextBoolean(), random.nextBoolean() && random.nextBoolean(), random.nextBoolean() && random.nextBoolean(), states[2]);
									world.setBlockState(pos, states[1], 3);
								}
							}
						}
					}
				}
			}
		}
	}

	private void buildRoom(WorldAccess world, BlockPos pos, int size, boolean north, boolean east, boolean south, boolean west, BlockState state) {
		world.setBlockState(pos.add(0, 0, 0), state, 3);
		world.setBlockState(pos.add(size, 0, 0), state, 3);
		world.setBlockState(pos.add(0, 0, size), state, 3);
		world.setBlockState(pos.add(size, 0, size), state, 3);

		buildWalls(world, pos, size, north, east, south, west, state);
	}

	private void buildWalls(WorldAccess world, BlockPos pos, int size, boolean north, boolean east, boolean south, boolean west, BlockState state) {
		if (north) {
			BlockPos.iterate(pos, pos.add(size, 0, 0)).forEach((blockPos) -> {
				world.setBlockState(blockPos, state, 3);
			});
		}
		if (east) {
			BlockPos.iterate(pos.add(size, 0, 0), pos.add(size, 0, size)).forEach((blockPos) -> {
				world.setBlockState(blockPos, state, 3);
			});
		}
		if (south) {
			BlockPos.iterate(pos.add(0, 0, size), pos.add(size, 0, size)).forEach((blockPos) -> {
				world.setBlockState(blockPos, state, 3);
			});
		}
		if (west) {
			BlockPos.iterate(pos, pos.add(0, 0, size)).forEach((blockPos) -> {
				world.setBlockState(blockPos, state, 3);
			});
		}
	}

	@Override
	public int getHeight(int x, int z, Type heightmapType) {
		return this.getSeaLevel() + this.height;
	}

	@Override
	public BlockView getColumnSample(int x, int z) {
		return new VerticalBlockSample(states);
	}

	@Override
	public int getSeaLevel() {
		return this.seaLevel;
	}

	private static ImmutableList<Pair<Boolean, OctavePerlinNoiseSampler>> createNoise(List<Boolean> aspects, long seed) {
		Builder<Pair<Boolean, OctavePerlinNoiseSampler>> builder = new Builder<Pair<Boolean, OctavePerlinNoiseSampler>>();

		for (Iterator<Boolean> var4 = aspects.iterator(); var4.hasNext(); ++seed) {
			Boolean layer = var4.next();
			builder.add(new Pair<Boolean, OctavePerlinNoiseSampler>(layer, new OctavePerlinNoiseSampler(new ChunkRandom(seed), ImmutableList.of(-4))));
		}

		return builder.build();
	}

}
