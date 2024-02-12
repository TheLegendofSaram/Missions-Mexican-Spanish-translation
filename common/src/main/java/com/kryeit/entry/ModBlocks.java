package com.kryeit.entry;

import com.kryeit.Main;
import com.kryeit.content.exchanger.MechanicalExchangerBlock;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static com.kryeit.Main.REGISTRATE;
import static com.kryeit.entry.ModCreativeTabs.useBaseTab;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class ModBlocks {

    static {
        useBaseTab();
    }

    public static final BlockEntry<MechanicalExchangerBlock> MECHANICAL_EXCHANGER = Main.registrate()
            .block("mechanical_exchanger",MechanicalExchangerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .addLayer(() -> RenderType::translucent)
            .transform(BlockStressDefaults.setImpact(32.0))
            .transform(axeOrPickaxe())
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .item()
            .transform(customItemModel())
            .register();

    public static void register() {}

}
