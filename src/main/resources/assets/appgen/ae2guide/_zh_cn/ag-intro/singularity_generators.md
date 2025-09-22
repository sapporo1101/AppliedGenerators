---
navigation:
  parent: ag-intro/ag-index.md
  title: 奇点发电机
  icon: appgen:singularity_generator_1k
categories:
  - blocks
  - generators
item_ids:
  - appgen:singularity_generator_1k
  - appgen:singularity_generator_4k
  - appgen:singularity_generator_16k
  - appgen:singularity_generator_64k
  - appgen:singularity_generator_256k
  - appgen:singularity_generator_1m
  - appgen:singularity_generator_4m
  - appgen:singularity_generator_16m
  - appgen:singularity_generator_64m
  - appgen:singularity_generator_256m
---

# Singularity Generators

<Column gap="1">
  <Row gap="1">
    <BlockImage id="appgen:singularity_generator_1k" scale="4" p:active="true"/>
    <BlockImage id="appgen:singularity_generator_4k" scale="4" p:active="true" />
    <BlockImage id="appgen:singularity_generator_16k" scale="4" p:active="true" />
    <BlockImage id="appgen:singularity_generator_64k" scale="4" p:active="true" />
    <BlockImage id="appgen:singularity_generator_256k" scale="4" p:active="true" />
  </Row>
  <Row gap="1">
    <BlockImage id="appgen:singularity_generator_1m" scale="4" p:active="true" />
    <BlockImage id="appgen:singularity_generator_4m" scale="4" p:active="true" />
    <BlockImage id="appgen:singularity_generator_16m" scale="4" p:active="true" />
    <BlockImage id="appgen:singularity_generator_64m" scale="4" p:active="true" />
    <BlockImage id="appgen:singularity_generator_256m" scale="4" p:active="true" />
  </Row>
</Column>

奇点发电机是一种能够通过消耗<ItemLink id="ae2:singularity" />生产FE的高能设备。

## FE产能

当奇点发电机消耗<ItemLink id="ae2:singularity" />时，其按下表生产FE。

| 奇点发电机                                               | 每个奇点产出的FE（默认） | 每刻生产的FE（默认） |
|-----------------------------------------------------|---------------------:|------------------:|
| <ItemLink id="appgen:singularity_generator_1k" />   |            1,000,000 |               200 |
| <ItemLink id="appgen:singularity_generator_4k" />   |            4,000,000 |               800 |
| <ItemLink id="appgen:singularity_generator_16k" />  |           16,000,000 |             3,200 |
| <ItemLink id="appgen:singularity_generator_64k" />  |           64,000,000 |            12,800 |
| <ItemLink id="appgen:singularity_generator_256k" /> |          256,000,000 |            51,200 |
| <ItemLink id="appgen:singularity_generator_1m" />   |        1,024,000,000 |           204,800 |
| <ItemLink id="appgen:singularity_generator_4m" />   |        4,096,000,000 |           819,200 |
| <ItemLink id="appgen:singularity_generator_16m" />  |       16,384,000,000 |         3,276,800 |
| <ItemLink id="appgen:singularity_generator_64m" />  |       65,536,000,000 |        13,107,200 |
| <ItemLink id="appgen:singularity_generator_256m" /> |      262,144,000,000 |        52,428,800 |

## 设置

奇点发电机向以下方向输送FE：

- 奇点发电机所在的ME网络。（默认）
- 与奇点发电机相邻的储能方块。

## 升级

奇点发电机支持以下[升级](ae2:items-blocks-machines/upgrade_cards.md)：

-   <ItemLink id="ae2:speed_card" /> 提升50%的产能速率，最多提升150%（即原速率的250%）。
-   <ItemLink id="ae2:energy_card" /> 提升50%的产能效率，最多提升150%（即原效率的250%）。
