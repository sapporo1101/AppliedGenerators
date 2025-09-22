---
navigation:
  parent: ag-intro/ag-index.md
  title: 样板缓存器
  icon: appgen:pattern_buffer
categories:
  - blocks
  - featured_blocks
item_ids:
  - appgen:pattern_buffer
---

# 样板缓存器

<BlockImage id="appgen:pattern_buffer" scale="8" />

样板缓存器是一种容器，其能够存储样板栏位所放置的样板的原料。

## 容量

样板缓存器对每一个栏位都有容量限制。容量取决于原料的类型，见下表。

| 原料类型          | 每格容量（默认） |
|---------------|--------------:|
| 物品            |         1,024 |
| 流体            |         1,024 |
| FE            |     1,048,576 |
| 化学品 （通用机械）    |         1,024 |
| 魔力 （新生魔艺）     |         1,000 |
| 魔源 （新生魔艺）     |         1,000 |

## 升级

样板缓存器支持以下[升级](ae2:items-blocks-machines/upgrade_cards.md)：

-   <ItemLink id="ae2:capacity_card" /> 使样板缓存器的容量上限翻倍，最多为原容量的1600%。
-   <ItemLink id="ae2:redstone_card" /> 当满足样板的合成配方时发出红石信号。