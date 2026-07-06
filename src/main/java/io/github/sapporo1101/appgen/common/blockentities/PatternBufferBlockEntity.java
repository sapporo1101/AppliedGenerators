package io.github.sapporo1101.appgen.common.blockentities;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.MachineUpgradesChanged;
import appeng.api.upgrades.Upgrades;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import com.glodblock.github.appflux.common.AFSingletons;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.xmod.ExternalTypes;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.sapporo1101.appgen.common.blocks.PatternBufferBlock.POWERED;

public class PatternBufferBlockEntity extends AEBaseBlockEntity implements InternalInventoryHost, IUpgradeableObject {

    private final PatternBufferInv storageInv = new PatternBufferInv(this::onStorageChanged, 36);
    private final AppEngInternalInventory patternInv = new AppEngInternalInventory(this, 1);
    private final IUpgradeInventory upgrades = new PatternBufferUpgradeInventory(AGSingletons.PATTERN_BUFFER, 5, this::onUpgradeChanged);

    private final PatternInput patternInput = new PatternInput();

    public PatternBufferBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    private void onStorageChanged() {
        if (this.level == null || this.level.isClientSide()) return;
        this.updateRedstoneState();
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        this.saveChanges();
    }

    private void onUpgradeChanged() {
        if (this.level == null || this.level.isClientSide()) return;
        this.updateRedstoneState();
        this.saveChanges();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        this.updatePattern();
        this.updateCapacity();
        this.updateRedstoneState();
        this.saveChanges();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (int index = 0; index < this.storageInv.size(); index++) {
            var stack = this.storageInv.getStack(index);
            if (stack != null) {
                stack.what().addDrops(stack.amount(), drops, level, pos);
            }
        }
        drops.add(patternInv.getStackInSlot(0));
        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < this.storageInv.size(); i++) {
            GenericStackInv inv = this.storageInv.getInv(i);
            inv.writeToChildTag(output, "buffer_" + i);
        }
        this.patternInv.writeToNBT(output, "pattern_inv");
        this.upgrades.writeToNBT(output, "upgrades");
        this.patternInput.writeToChildTag(output, "pattern");
    }

    @Override
    public void loadTag(ValueInput input) {
        super.loadTag(input);
        for (int i = 0; i < this.storageInv.size(); i++) {
            GenericStackInv inv = this.storageInv.getInv(i);
            inv.readFromChildTag(input, "buffer_" + i);
        }
        this.patternInv.readFromNBT(input, "pattern_inv");
        this.upgrades.readFromNBT(input, "upgrades");
        this.patternInput.readFromChildTag(input, "pattern");
        this.updateCapacity(); // update capacity after loading
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.storageInv.clear();
        this.patternInv.clear();
        this.upgrades.clear();
        this.patternInput.getInputs().clear();
    }

    private void updatePattern() {
        ItemStack patternStack = patternInv.getStackInSlot(0);
        List<List<GenericStack>> newInputs = new ArrayList<>();
        setPattern:
        if (patternStack != null) {
            IPatternDetails patternDetails = PatternDetailsHelper.decodePattern(patternStack, this.level);

            if (patternDetails == null) break setPattern;
            for (IPatternDetails.IInput input : patternDetails.getInputs()) {
                List<GenericStack> inputStack = Arrays.stream(input.getPossibleInputs()).map(s -> new GenericStack(s.what(), s.amount() * input.getMultiplier())).toList();
                newInputs.add(inputStack);
            }
        }
        this.patternInput.setInputs(newInputs);
    }

    private boolean isValidKey(int slot, AEKey key) {
        if (slot < 0 || slot >= this.patternInput.getInputs().size()) return false;
        List<GenericStack> inputs = this.patternInput.getInputs().get(slot);
        for (GenericStack input : inputs) {
            if (key.matches(input)) return true;
        }
        return false;
    }

    private void updateCapacity() {
        for (int i = 0; i < storageInv.size(); i++) {
            GenericStackInv inv = storageInv.getInv(i);
            inv.setCapacity(AEKeyType.items(), 0);
            inv.setCapacity(AEKeyType.fluids(), 0);
            inv.setCapacity(ExternalTypes.FLUX, 0);
            inv.setCapacity(ExternalTypes.GAS, 0);
            inv.setCapacity(ExternalTypes.MANA, 0);
            inv.setCapacity(ExternalTypes.SOURCE, 0);
            List<GenericStack> possibleInputs;
            try {
                possibleInputs = this.patternInput.getInputs().get(i);
            } catch (Exception e) {
                continue;
            }
            for (GenericStack input : possibleInputs) {
                AEKeyType keyType = input.what().getType();
                this.storageInv.setCapacity(i, keyType, Math.max(inv.getCapacity(keyType), input.amount()));
            }
        }
    }

    private void updateRedstoneState() {
        boolean powered = this.getBlockState().getValue(POWERED);
        boolean newPowered = this.upgrades.isInstalled(AEItems.REDSTONE_CARD) && this.patternInput.isRecipeSatisfied();
        if (this.level != null && powered != newPowered) {
            this.level.setBlockAndUpdate(this.getBlockPos(), this.level.getBlockState(this.getBlockPos()).setValue(POWERED, newPowered));
            this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }
    }

    public PatternBufferInv getStorageInv() {
        return storageInv;
    }

    public InternalInventory getPatternInv() {
        return patternInv;
    }

    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    @Nullable
    public AEKey getPrimaryOutputKey() {
        ItemStack patternStack = patternInv.getStackInSlot(0);
        if (patternStack.isEmpty()) return null;
        IPatternDetails patternDetails = PatternDetailsHelper.decodePattern(patternStack, this.level);
        if (patternDetails == null || patternDetails.getOutputs().isEmpty()) return null;
        return patternDetails.getPrimaryOutput().what();
    }

    @SuppressWarnings("UnstableApiUsage")
    public class PatternBufferInv implements GenericInternalInventory {
        final ArrayList<GenericStackInv> invs = new ArrayList<>();

        public PatternBufferInv(Runnable runnable, int size) {
            for (int i = 0; i < size; i++) invs.add(new PatternBufferSlotInv(runnable, i));
        }

        public GenericStackInv getInv(int slot) {
            return this.invs.get(slot);
        }

        @Override
        public int size() {
            return this.invs.size();
        }

        @Override
        public @Nullable GenericStack getStack(int slot) {
            return this.invs.get(slot) != null ? this.invs.get(slot).getStack(0) : null;
        }

        @Override
        public @Nullable AEKey getKey(int slot) {
            return this.invs.get(slot) != null ? this.invs.get(slot).getKey(0) : null;
        }

        @Override
        public long getAmount(int slot) {
            return this.invs.get(slot) != null ? this.invs.get(slot).getAmount(0) : 0;
        }

        @Override
        public long getMaxAmount(AEKey aeKey) {
            return (long) Math.ceil((double) this.invs.stream().mapToLong(inv -> inv.getCapacity(aeKey.getType()) * this.isValidKey(inv, aeKey)).sum() / this.invs.size());
        }

        private int isValidKey(GenericStackInv inv, AEKey aeKey) {
            int i = this.invs.indexOf(inv);
            return PatternBufferBlockEntity.this.isValidKey(i, aeKey) ? 1 : 0;
        }

        private long getMaxCapacity(AEKeyType aeKeyType) {
            if (patternInput.getInputs().isEmpty()) return 0;
            long baseCapacity = 0;
            if (aeKeyType == AEKeyType.items()) baseCapacity = 1024;
            if (aeKeyType == AEKeyType.fluids()) baseCapacity = 1024000;
            if (aeKeyType == ExternalTypes.FLUX) baseCapacity = 1048576;
            if (aeKeyType == ExternalTypes.GAS) baseCapacity = 1024;
            if (aeKeyType == ExternalTypes.MANA) baseCapacity = 1000;
            if (aeKeyType == ExternalTypes.SOURCE) baseCapacity = 1000;
            int upgradeMultiplier = (int) Math.pow(2, PatternBufferBlockEntity.this.upgrades.getInstalledUpgrades(AEItems.CAPACITY_CARD));
            return baseCapacity * upgradeMultiplier;
        }

        @Override
        public long getCapacity(AEKeyType aeKeyType) {
            return (long) Math.ceil((double) this.invs.stream().mapToLong(inv -> inv.getCapacity(aeKeyType)).sum() / this.invs.size());
        }

        public void setCapacity(int slot, AEKeyType aeKeyType, long capacity) {
            if (slot < 0 || slot >= this.invs.size()) return;
            capacity = Math.min(capacity, this.getMaxCapacity(aeKeyType));
            this.invs.get(slot).setCapacity(aeKeyType, capacity);
        }

        @Override
        public boolean canInsert() {
            return true;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public void setStack(int slot, @Nullable GenericStack genericStack) {
            if (slot < 0 || slot >= this.invs.size()) return;
            if (genericStack != null && !isAllowedIn(slot, genericStack.what())) return;
            this.invs.get(slot).setStack(0, genericStack);
        }

        @Override
        public boolean isSupportedType(AEKeyType aeKeyType) {
            return invs.stream().anyMatch(inv -> inv.isSupportedType(aeKeyType));
        }

        @Override
        public boolean isAllowedIn(int slot, AEKey aeKey) {
            return PatternBufferBlockEntity.this.isValidKey(slot, aeKey);
        }

        @Override
        public long insert(int slot, AEKey aeKey, long l, Actionable actionable) {
            if (!this.isAllowedIn(slot, aeKey)) return 0;
            return this.invs.get(slot) != null ? this.invs.get(slot).insert(0, aeKey, l, actionable) : 0;
        }

        @Override
        public long extract(int slot, AEKey aeKey, long l, Actionable actionable) {
            return this.invs.get(slot) != null ? this.invs.get(slot).extract(0, aeKey, l, actionable) : 0;
        }

        @Override
        public void beginBatch() {
            for (GenericStackInv inv : this.invs) inv.beginBatch();
        }

        @Override
        public void endBatch() {
            for (GenericStackInv inv : this.invs) inv.endBatch();
        }

        @Override
        public void endBatchSuppressed() {
            for (GenericStackInv inv : this.invs) inv.endBatchSuppressed();
        }

        @Override
        public void onChange() {
            for (GenericStackInv inv : this.invs) inv.onChange();
        }

        @Override
        public void updateSnapshots(TransactionContext transaction) {
            this.invs.forEach(inv -> inv.updateSnapshots(transaction));
        }

        public void clear() {
            for (GenericStackInv inv : this.invs) inv.clear();
        }
    }

    private class PatternBufferSlotInv extends GenericStackInv {
        private final int index;

        public PatternBufferSlotInv(Runnable runnable, int index) {
            super(runnable, 1);
            this.index = index;
        }

        @Override
        public boolean isAllowedIn(int slot, AEKey what) {
            return isValidKey(this.index, what);
        }
    }

    private class PatternInput {
        private List<List<GenericStack>> inputs = new ArrayList<>();

        public PatternInput() {
        }

        public void setInputs(List<List<GenericStack>> inputs) {
            this.inputs = inputs.subList(0, Math.min(inputs.size(), 36));
        }

        public List<List<GenericStack>> getInputs() {
            return inputs;
        }

        public boolean isRecipeSatisfied() {
            if (PatternBufferBlockEntity.this.patternInv.isEmpty()) return false;
            if (this.inputs.isEmpty()) return false;
            forInput:
            for (int i = 0; i < this.inputs.size(); i++) {
                List<GenericStack> possibleInputs = this.inputs.get(i);
                if (possibleInputs.isEmpty()) continue;
                for (GenericStack input : possibleInputs) {
                    if (input == null || input.what() == null) continue;
                    if (input.what().matches(storageInv.getStack(i)) && storageInv.getAmount(i) >= input.amount())
                        continue forInput;
                }
                return false;
            }
            return true;
        }

        public void writeToChildTag(ValueOutput output, String name) {
            if (!this.inputs.isEmpty()) {
                for (int i = 0; i < this.inputs.size(); i++) {
                    this.writeToTag(output.childrenList(name + "_" + i), i);
                }
            }
        }

        private void writeToTag(ValueOutput.ValueOutputList output, int index) {
            for (var stack : this.inputs.get(index)) {
                GenericStack.writeTag(output.addChild(), stack);
            }
        }

        public void readFromChildTag(ValueInput input, String name) {
            for (int i = 0; i < 36; i++) {
                ValueInput.ValueInputList content = input.childrenListOrEmpty(name + "_" + i);
                if (content.isEmpty()) break;
                readFromTag(content, i);
            }
        }

        private void readFromTag(ValueInput.ValueInputList input, int index) {
            this.inputs.add(new ArrayList<>());
            for (ValueInput inputElement : input) {
                GenericStack stack = GenericStack.readTag(inputElement);
                this.inputs.get(index).add(stack);
            }
        }
    }

    private static abstract class UpgradeInventory extends AppEngInternalInventory implements InternalInventoryHost, IUpgradeInventory {
        private final Item item;

        // Cache of which upgrades are installed
        @Nullable
        private Reference2IntMap<Item> installed = null;

        public UpgradeInventory(Item item, int slots) {
            super(null, slots, 1);
            this.item = item;
            this.setHost(this);
            this.setFilter(new UpgradeInvFilter());
        }

        @Override
        public boolean isClientSide() {
            return false;
        }

        @Override
        protected boolean eventsEnabled() {
            return true;
        }

        @Override
        public int getMaxInstalled(ItemLike upgradeCard) {
            return Upgrades.getMaxInstallable(upgradeCard, item);
        }

        @Override
        public ItemLike getUpgradableItem() {
            return item;
        }

        @Override
        public int getInstalledUpgrades(ItemLike upgradeCard) {
            if (installed == null) {
                this.updateUpgradeInfo();
            }

            return installed.getOrDefault(upgradeCard.asItem(), 0);
        }

        private void updateUpgradeInfo() {
            this.installed = new Reference2IntArrayMap<>(size());

            for (var is : this) {
                var maxInstalled = getMaxInstalled(is.getItem());
                if (maxInstalled > 0) {
                    this.installed.merge(is.getItem(), 1, (a, b) -> Math.min(maxInstalled, a + b));
                }
            }
        }

        @Override
        public void readFromNBT(ValueInput input, String name) {
            super.readFromNBT(input, name);
            this.updateUpgradeInfo();
        }

        @Override
        public void saveChangedInventory(AppEngInternalInventory inv) {
        }

        @Override
        public void onChangeInventory(AppEngInternalInventory inv, int slot) {
            this.installed = null;
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public void sendChangeNotification(int slot) {
            this.installed = null;
            super.sendChangeNotification(slot);
        }

        private class UpgradeInvFilter implements IAEItemFilter {

            @Override
            public boolean allowInsert(InternalInventory inv, int slot, ItemStack itemstack) {
                var cardItem = itemstack.getItem();
                return getInstalledUpgrades(cardItem) < getMaxInstalled(cardItem);
            }
        }
    }

    private static class PatternBufferUpgradeInventory extends UpgradeInventory {
        @Nullable
        private final MachineUpgradesChanged changeCallback;

        public PatternBufferUpgradeInventory(ItemLike item, int slots, @Nullable MachineUpgradesChanged changeCallback) {
            super(item.asItem(), slots);
            this.changeCallback = changeCallback;
        }

        @Override
        public void onChangeInventory(AppEngInternalInventory inv, int slot) {
            super.onChangeInventory(inv, slot);

            if (changeCallback != null) {
                changeCallback.onUpgradesChanged();
            }
        }

        @Override
        public boolean isInstalled(ItemLike upgradeCard) {
            if (upgradeCard == AFSingletons.INDUCTION_CARD) return true;
            return super.isInstalled(upgradeCard);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag data = super.getUpdateTag(registries);
        InternalInventory pattern = this.getPatternInv();
        if (pattern != InternalInventory.empty()) {
            final CompoundTag opt = new CompoundTag();
            ItemStack patternStack = pattern.getStackInSlot(0);
            ItemStack.OPTIONAL_CODEC.encodeStart(
                            registries.createSerializationContext(NbtOps.INSTANCE), patternStack)
                    .ifSuccess(tag -> opt.put("item", tag)
                    );
            data.put("inv", opt);
        }
        return data;
    }


}