package io.github.sapporo1101.appgen.common.blocks;

import io.github.sapporo1101.appgen.common.blockentities.CreativeFluxCellBlockEntity;

public class CreativeFluxCellBlock extends FluxCellBaseBlock<CreativeFluxCellBlockEntity> {
    public static final long MAX_CAPACITY = Integer.MAX_VALUE;

    public CreativeFluxCellBlock(Properties props) {
        super(props);
    }
}
