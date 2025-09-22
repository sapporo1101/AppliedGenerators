---
navigation:
  parent: ag-intro/ag-index.md
  title: 通量发电机
  icon: appgen:flux_generator_1k
categories:
  - blocks
  - generators
item_ids:
  - appgen:flux_generator_1k
  - appgen:flux_generator_4k
  - appgen:flux_generator_16k
  - appgen:flux_generator_64k
  - appgen:flux_generator_256k
  - appgen:flux_generator_1m
  - appgen:flux_generator_4m
  - appgen:flux_generator_16m
  - appgen:flux_generator_64m
  - appgen:flux_generator_256m
---

# 通量发电机

<Column gap="1">
  <Row gap="1">
    <BlockImage id="appgen:flux_generator_1k" scale="4" p:active="true"/>
    <BlockImage id="appgen:flux_generator_4k" scale="4" p:active="true" />
    <BlockImage id="appgen:flux_generator_16k" scale="4" p:active="true" />
    <BlockImage id="appgen:flux_generator_64k" scale="4" p:active="true" />
    <BlockImage id="appgen:flux_generator_256k" scale="4" p:active="true" />
  </Row>
  <Row gap="1">
    <BlockImage id="appgen:flux_generator_1m" scale="4" p:active="true" />
    <BlockImage id="appgen:flux_generator_4m" scale="4" p:active="true" />
    <BlockImage id="appgen:flux_generator_16m" scale="4" p:active="true" />
    <BlockImage id="appgen:flux_generator_64m" scale="4" p:active="true" />
    <BlockImage id="appgen:flux_generator_256m" scale="4" p:active="true" />
  </Row>
</Column>

通量发电机是能永久发电的基础设备。

## FE产能

通量发电机的发电效率如下表。

| 通量发电机                                        | 每刻FE产能（默认） |
|----------------------------------------------|----------------:|
| <ItemLink id="appgen:flux_generator_1k" />   |              20 |
| <ItemLink id="appgen:flux_generator_4k" />   |              80 |
| <ItemLink id="appgen:flux_generator_16k" />  |             320 |
| <ItemLink id="appgen:flux_generator_64k" />  |           1,280 |
| <ItemLink id="appgen:flux_generator_256k" /> |           5,120 |
| <ItemLink id="appgen:flux_generator_1m" />   |          20,480 |
| <ItemLink id="appgen:flux_generator_4m" />   |          81,920 |
| <ItemLink id="appgen:flux_generator_16m" />  |         327,680 |
| <ItemLink id="appgen:flux_generator_64m" />  |       1,310,720 |
| <ItemLink id="appgen:flux_generator_256m" /> |       5,242,880 |

## 设置

通量发电机向以下方向之一输送FE：

- 此通量发电机所在的ME网络。（默认）
- 与此通量发电机相邻的储能方块。

## 升级

奇点发电机支持这些[升级](ae2:items-blocks-machines/upgrade_cards.md)：

-   <ItemLink id="ae2:speed_card" /> 提升50%的产能速率，最高提升150%（即原速率的250%）
-   <ItemLink id="ae2:redstone_card" /> 允许通量发电机被红石信号启停，可用于控制FE的产出。

（注意：在脉冲模式下，通量发电机不会在每次脉冲都产能。
通量发电机在脉冲足够快的时候会一次性产出所有能源。）