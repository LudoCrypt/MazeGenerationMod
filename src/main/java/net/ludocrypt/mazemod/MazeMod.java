package net.ludocrypt.mazemod;

import java.util.Map;

import net.fabricmc.api.ModInitializer;
import net.ludocrypt.exdimapi.api.ExtraDimApi;
import net.ludocrypt.exdimapi.api.ExtraDimension;
import net.ludocrypt.mazemod.world.Level0World;
import net.ludocrypt.mazemod.world.Level1World;
import net.ludocrypt.mazemod.world.Level2World;
import net.ludocrypt.mazemod.world.MazeChunkGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MazeMod implements ModInitializer, ExtraDimApi {

	@Override
	public void onInitialize() {
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier("mazemod", "maze_chunk_generator"), MazeChunkGenerator.CODEC);
	}

	@Override
	public void registerModDimensions(Map<Identifier, ExtraDimension> registry) {
		registry.put(Level0World.LEVEL_0_ID, new Level0World());
		registry.put(Level1World.LEVEL_1_ID, new Level1World());
		registry.put(Level2World.LEVEL_2_ID, new Level2World());
	}

}
