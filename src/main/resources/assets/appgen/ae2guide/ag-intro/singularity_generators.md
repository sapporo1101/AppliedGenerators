---
navigation:
  parent: ag-intro/ag-index.md
  title: Singularity Generators
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

The Singularity Generators are powerful devices that can generate Forge Energy by
consuming a <ItemLink id="ae2:singularity" />.

## FE Generation

When <ItemLink id="ae2:singularity" /> is consumed by the Singularity Generator, the following amount of FE is generated
at the following rates.

| Singularity Generator                               | FE Generation Per Singularity (Default) | FE Generation Per Tick (Default) |
|-----------------------------------------------------|----------------------------------------:|---------------------------------:|
| <ItemLink id="appgen:singularity_generator_1k" />   |                              10,000,000 |                              100 |
| <ItemLink id="appgen:singularity_generator_4k" />   |                              20,000,000 |                              400 |
| <ItemLink id="appgen:singularity_generator_16k" />  |                              40,000,000 |                            1,600 |
| <ItemLink id="appgen:singularity_generator_64k" />  |                              80,000,000 |                            6,400 |
| <ItemLink id="appgen:singularity_generator_256k" /> |                             160,000,000 |                           25,600 |
| <ItemLink id="appgen:singularity_generator_1m" />   |                             320,000,000 |                          102,400 |
| <ItemLink id="appgen:singularity_generator_4m" />   |                             640,000,000 |                          409,600 |
| <ItemLink id="appgen:singularity_generator_16m" />  |                           1,280,000,000 |                        1,638,400 |
| <ItemLink id="appgen:singularity_generator_64m" />  |                           2,560,000,000 |                        6,553,600 |
| <ItemLink id="appgen:singularity_generator_256m" /> |                           5,120,000,000 |                       26,214,400 |

## Settings

The Singularity Generators send FE to one of the following destinations:

- The ME System which the Singularity Generator belongs to. (Default)
- The energy storages adjacent to the Singularity Generator.

## Upgrades

The Singularity Generators support the following [upgrades](ae2:items-blocks-machines/upgrade_cards.md):

-   <ItemLink id="ae2:speed_card" /> increases the generation speed of the Singularity Generator by +50%, for a max of +150%, or 250% of the base generation speed.
-   <ItemLink id="ae2:energy_card" /> increases the FE generation per singularity by +50%, for a max of +150%, or 250% of the base FE generation.
