package net.ludocrypt.mazemod;

import java.util.Map;

import net.fabricmc.api.ModInitializer;
import net.ludocrypt.exdimapi.api.ExtraDimApi;
import net.ludocrypt.exdimapi.api.ExtraDimension;
import net.ludocrypt.mazemod.world.MazeWorld;
import net.minecraft.util.Identifier;

public class MazeMod implements ModInitializer, ExtraDimApi {

	@Override
	public void onInitialize() {

	}

	@Override
	public void registerModDimensions(Map<Identifier, ExtraDimension> registry) {
		registry.put(new Identifier("mazemod", "mazeworld"), new MazeWorld());
	}

}
