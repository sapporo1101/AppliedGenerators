package io.github.sapporo1101.appgen.common.blockentities.interfaces;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.me.helpers.MachineSource;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Set;

public interface IEnergyExtractor {
    AEKey FE_KEY = FluxKey.of(EnergyType.FE);

    default long sendFEToNetwork(long amount, IGridNode node, MachineSource source) {
        if (node == null) return 0;

        IGrid grid = node.getGrid();
        IStorageService storage = grid.getStorageService();

        return storage.getInventory().insert(FE_KEY, amount, Actionable.MODULATE, source);
    }

    default long sendFEToAdjacentBlock(long amount, Set<Direction> outputSides, BlockPos pos, Level level) {
        if (level == null) return 0;

        long sending = amount;
        sides:
        for (Direction dir : outputSides) {
            if (sending <= 0) break;
            BlockPos targetPos = pos.relative(dir);
            EnergyHandler storage = level.getCapability(Capabilities.Energy.BLOCK, targetPos, dir.getOpposite());
            if (storage != null) {
                while (sending > 0) {
                    int canInsert;
                    try (Transaction simulation = Transaction.openRoot()) {
                        int batch = (int) Math.min(sending, Integer.MAX_VALUE);
                        canInsert = storage.insert(batch, simulation);
                    }

                    if (canInsert <= 0) continue sides;

                    try (Transaction transaction = Transaction.openRoot()) {
                        int inserted = storage.insert(canInsert, transaction);
                        if (inserted > 0) {
                            transaction.commit();
                            sending -= inserted;
                        }
                    }
                }
            }
        }
        return amount - sending;
    }


}
