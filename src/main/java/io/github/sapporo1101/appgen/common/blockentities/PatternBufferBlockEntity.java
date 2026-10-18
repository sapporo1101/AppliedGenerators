package io.github.sapporo1101.appgen.common.blockentities;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.MachineUpgradesChanged;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import com.glodblock.github.appflux.common.AFSingletons;
import io.github.sapporo1101.appgen.api.AGSettings;
import io.github.sapporo1101.appgen.api.KeepSignalMode;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.xmod.ExternalTypes;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.sapporo1101.appgen.common.blocks.PatternBufferBlock.POWERED;

public class PatternBufferBlockEntity extends AEBaseBlockEntity implements InternalInventoryHost, IUpgradeableObject, IConfigurableObject {
    private final PatternBufferInv storageInv = new PatternBufferInv(this::onStorageChanged, 36);
    private final GenericStackInv patternInv = new PatternInv(this::onPatternChanged, 1);
    private final IUpgradeInventory upgrades = new PatternBufferUpgradeInventory(AGSingletons.PATTERN_BUFFER, 5, this::onUpgradeChanged);
    private final IConfigManager configManager = IConfigManager.builder(this::onConfigChanged).registerSetting(AGSettings.KEEP_SIGNAL, KeepSignalMode.OFF).build();
    private final PatternInput patternInput = new PatternInput();

    private boolean lastSatisfied = false;

    public PatternBufferBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    private void onStorageChanged() {
        if (this.level == null || this.level.isClientSide()) return;
        this.updateRedstoneState();
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        this.saveChanges();
    }

    private void onPatternChanged() {
        if (this.level == null || this.level.isClientSide()) return;
        this.updatePattern();
        this.updateRedstoneState();
        this.saveChanges();
        this.markForClientUpdate();
    }

    private void onUpgradeChanged() {
        if (this.level == null || this.level.isClientSide()) return;
        this.updateRedstoneState();
        this.saveChanges();
        this.markForClientUpdate();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        this.updatePattern();
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
        GenericStack patternStack = this.patternInv.getStack(0);
        if (patternStack != null && patternStack.what() instanceof AEItemKey key) {
            drops.add(key.toStack((int) patternStack.amount()));
        }
        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.storageInv.writeToChildTag(output, "buffer");
        this.patternInv.writeToChildTag(output, "pattern_inv");
        this.upgrades.writeToNBT(output, "upgrades");
        this.patternInput.writeToChildTag(output, "pattern");
    }

    @Override
    public void loadTag(ValueInput input) {
        super.loadTag(input);
        this.patternInput.readFromChildTag(input, "pattern");
        this.storageInv.readFromChildTag(input, "buffer");
        this.patternInv.readFromChildTag(input, "pattern_inv");
        this.upgrades.readFromNBT(input, "upgrades");
    }

    @Override
    public void onLoad() {
        super.onLoad();
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
        GenericStack patternStack = patternInv.getStack(0);
        AEItemKey patternKey = patternStack != null && patternStack.what() instanceof AEItemKey key ? key : null;
        List<List<GenericStack>> newInputs = new ArrayList<>();
        setPattern:
        if (patternStack != null) {
            IPatternDetails patternDetails = PatternDetailsHelper.decodePattern(patternKey, this.level);

            if (patternDetails == null) break setPattern;
            for (IPatternDetails.IInput input : patternDetails.getInputs()) {
                List<GenericStack> inputStack = new ArrayList<>(Arrays.stream(input.getPossibleInputs())
                        .map(s -> new GenericStack(s.what(), s.amount() * input.getMultiplier()))
                        .toList());
                newInputs.add(inputStack);
            }
        }
        this.patternInput.setInputs(newInputs);
    }

    private void onConfigChanged() {
        this.lastSatisfied = false;
        this.updateRedstoneState();
        this.saveChanges();
    }

    private void updateRedstoneState() {
        this.updateLastSatisfied();
        boolean keepSignal = this.configManager.getSetting(AGSettings.KEEP_SIGNAL) != KeepSignalMode.OFF;
        boolean powered = this.getBlockState().getValue(POWERED);
        boolean newPowered = this.upgrades.isInstalled(AEItems.REDSTONE_CARD)
                && (!keepSignal && this.patternInput.isRecipeSatisfied() || keepSignal && this.lastSatisfied);
        if (this.level != null && powered != newPowered) {
            this.level.setBlockAndUpdate(this.getBlockPos(), this.level.getBlockState(this.getBlockPos()).setValue(POWERED, newPowered));
            this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        }
    }

    private void updateLastSatisfied() {
        boolean storageEmpty = this.storageInv.isEmpty();
        boolean redstoneCardInstalled = this.upgrades.isInstalled(AEItems.REDSTONE_CARD);
        if (storageEmpty || !redstoneCardInstalled) this.lastSatisfied = false;
        boolean currentSatisfied = this.patternInput.isRecipeSatisfied();
        if (currentSatisfied) this.lastSatisfied = true;
    }

    public boolean isInsertionLocked() {
        return this.configManager.getSetting(AGSettings.KEEP_SIGNAL) == KeepSignalMode.LOCKED && this.lastSatisfied;
    }

    public boolean isExtractionLocked() {
        return this.configManager.getSetting(AGSettings.KEEP_SIGNAL) == KeepSignalMode.LOCKED && !this.lastSatisfied;
    }

    public boolean isPatternSlotLocked() {
        return !this.getStorageInv().isEmpty();
    }

    public static long canInsert(int slot, AEKey key, long amount, PatternInput patternInput, int currentAmount) {
        if (key == null) return 0;
        if (slot < 0 || slot >= patternInput.getInputs().size()) return 0;
        List<GenericStack> inputs = patternInput.getInputs().get(slot);
        long capacity = 0;
        for (GenericStack input : inputs) {
            if (key.matches(input)) {
                capacity += input.amount();
            }
        }
        return Math.clamp(capacity - currentAmount, 0, amount); // return the amount that can be inserted
    }

    private static long keyCapacity(AEKey key, PatternInput patternInput) {
        if (key == null) return 0;
        List<List<GenericStack>> inputs = patternInput.getInputs();
        long capacity = 0;
        for (List<GenericStack> input : inputs) {
            for (GenericStack stack : input) {
                if (key.matches(stack)) {
                    capacity += stack.amount();
                }
            }
        }
        return capacity;
    }

    public PatternBufferInv getStorageInv() {
        return storageInv;
    }

    public GenericStackInv getPatternInv() {
        return patternInv;
    }

    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public PatternInput getPatternInput() {
        return patternInput;
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        this.saveChanges();
    }

    @Nullable
    public AEKey getPrimaryOutputKey() {
        GenericStack patternStack = patternInv.getStack(0);
        AEItemKey patternKey = patternStack != null && patternStack.what() instanceof AEItemKey key ? key : null;
        IPatternDetails patternDetails = PatternDetailsHelper.decodePattern(patternKey, this.level);
        if (patternDetails == null || patternDetails.getOutputs().isEmpty()) return null;
        return patternDetails.getPrimaryOutput().what();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.configManager;
    }

    public class PatternBufferInv extends GenericStackInv {
        public PatternBufferInv(Runnable runnable, int size) {
            super(runnable, size);
        }

        public PatternInput getPatternInput() {
            return PatternBufferBlockEntity.this.patternInput;
        }

        public PatternBufferBlockEntity getHost() {
            return PatternBufferBlockEntity.this;
        }

        @Override
        public long getMaxAmount(AEKey key) {
            long recipeCapacity = keyCapacity(key, PatternBufferBlockEntity.this.patternInput);
            return recipeCapacity > 0 ? recipeCapacity : Long.MAX_VALUE;
        }

        @Override
        public boolean isAllowedIn(int slot, AEKey what) {
            return super.isAllowedIn(slot, what)
                    && PatternBufferBlockEntity.canInsert(slot, what, 1, PatternBufferBlockEntity.this.patternInput, 0) > 0;
        }

        @Override
        public long getCapacity(AEKeyType aeKeyType) {
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
        public long insert(int slot, AEKey what, long amount, Actionable mode) {
            long inserting = PatternBufferBlockEntity.canInsert(slot, what, amount, PatternBufferBlockEntity.this.patternInput, (int) this.getAmount(slot));
            if (inserting <= 0) return 0;
            return super.insert(slot, what, amount, mode);
        }

        @Override
        public long extract(int slot, AEKey what, long amount, Actionable mode) {
            return super.extract(slot, what, amount, mode);
        }

        @Override
        public void setStack(int slot, @Nullable GenericStack stack) {
            super.setStack(slot, stack);
        }


    }

    private static class PatternInv extends GenericStackInv {
        public PatternInv(Runnable listener, int size) {
            super(listener, size);
        }
    }

    public class PatternInput {
        private List<List<GenericStack>> inputs = new ArrayList<>();

        public PatternInput() {
        }

        public void setInputs(List<List<GenericStack>> inputs) {
            this.inputs = new ArrayList<>(inputs.subList(0, Math.min(inputs.size(), 36)));
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
            while (this.inputs.size() <= index) {
                this.inputs.add(new ArrayList<>());
            }
            List<GenericStack> target = this.inputs.get(index);
            target.clear();
            for (ValueInput inputElement : input) {
                GenericStack stack = GenericStack.readTag(inputElement);
                target.add(stack);
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
        return this.saveWithoutMetadata(registries);
    }
}