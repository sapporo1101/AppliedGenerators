---
navigation:
  parent: ag-intro/ag-index.md
  title: Pattern Buffer
  icon: appgen:pattern_buffer
categories:
  - blocks
  - featured_blocks
item_ids:
  - appgen:pattern_buffer
---

# Pattern Buffer

<BlockImage id="appgen:pattern_buffer" scale="8" />

The Pattern Buffer is a storage that can store the ingredients for the recipe of the pattern you set in the pattern
slot.

## Capacity

The Pattern Buffer has a limited capacity for each ingredient or each slot. It depends on the type of the ingredient,
and the capacity is as follows:

| Ingredient Type      | Capacity Per Slot (Default) |
|----------------------|----------------------------:|
| Item                 |                       1,024 |
| Fluid                |                       1,024 |
| Forge Energy         |                   1,048,576 |
| Gas (Mekanism)       |                       1,024 |
| Mana (Ars Nouveau)   |                       1,000 |
| Source (Ars Nouveau) |                       1,000 |

## Upgrades

The Pattern Buffer supports the following [upgrades](ae2:items-blocks-machines/upgrade_cards.md):

-   <ItemLink id="ae2:capacity_card" /> multiplies the capacity of the Pattern Buffer Storage by 2, for a max of 1600% of the base capacity.
-   <ItemLink id="ae2:redstone_card" /> lets the Pattern Buffer emit a redstone signal. What that signal does is configured with the Keep Signal setting described below.

### Keep Signal

A <ItemLink id="ae2:redstone_card" /> adds a Keep Signal button to the toolbar, offering three modes:

| Mode   | Redstone Signal                                                           | Extraction (out of the Buffer)        | Insertion (into the Buffer)          |
|--------|---------------------------------------------------------------------------|---------------------------------------|--------------------------------------|
| OFF    | While the pattern's recipe is satisfied.                                  | Allowed.                              | Allowed.                             |
| ON     | From the moment the recipe is satisfied, until the storage becomes empty. | Allowed.                              | Allowed.                             |
| LOCKED | Same as ON.                                                               | Locked until the recipe is satisfied. | Locked once the recipe is satisfied. |

## Deleting Stored Resources

**Middle-click** a storage slot to open a confirmation dialog and then delete whatever is stored in that slot.

**Shift + Middle-click** targets the whole Buffer instead of the clicked slot.
