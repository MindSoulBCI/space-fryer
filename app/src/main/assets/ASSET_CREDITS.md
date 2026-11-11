# 空间油炸食堂素材接收记录

已检查 7 个输入文件。ZIP 完整性校验通过，GLB JSON 可读取。原始下载文件保留在 Downloads。

## 资源与许可
- KayKit Restaurant Bits FREE： https://kaylousberg.itch.io/restaurant-bits ，CC0，包内文件完整保留。
- Kenney Food Kit： https://kenney.nl/assets/food-kit ，CC0，保留 License.txt。
- Kenney Particle Pack： https://kenney.nl/assets/particle-pack ，CC0，保留 License.txt。
- Banana / Quaternius： https://poly.pizza/m/ruOFtE0B6Z ，页面标注 CC0。
- Corn / Kenney： https://poly.pizza/m/NaEnzEW0JQ ，页面标注 CC0。
- Deep fryer / Gleg3002： https://sketchfab.com/3d-models/deep-fryer-d0e36eefc2bf41b39fd4ac552d452c29 ，GLB 内嵌 CC BY 4.0。
- Popcorn Bucket / beastybevan： https://sketchfab.com/3d-models/popcorn-bucket-115700b607424d7d92b133cb44ec6166 ，GLB 内嵌 CC BY 4.0。

CC BY 4.0 链接：https://creativecommons.org/licenses/by/4.0/
修改内容见下方接入修改记录。

## 接入映射
- deep_fryer.glb：81 个网格，先拆出炸炉/炸篮，不能将整套模型直接缩小替代单个炉子。
- Kenney Models/GLB format：锅、平底锅、碗、盘子及食材候选。
- KayKit Assets/gltf：保留配套 bin 和贴图，作为厨房道具候选。
- Banana、Corn：可用于对应食材的 RAW/COOKING/PLATED 状态。
- popcorn_bucket.glb：爆米花成品桶，不等于独立爆米花颗粒。
- Particle Pack/PNG (Transparent)：火焰和烟雾纹理，需另外实现运行时效果。

## 状态
仅完成素材收集和结构检查，未修改 Kotlin 场景、未生成新 APK、未验证模拟器视觉。
接入工作受到 PICO 场景工作流输入协议错误阻塞；检查结果见 manifest.json。

## 2026-11-04 接入修改
Deep fryer (Gleg3002, CC BY 4.0): 提取中央设备，裁去高于源 Blender Z=.52 的遮挡顶棚结构，纹理缩至1024。Popcorn Bucket (beastybevan, CC BY 4.0): 使用原模型并在运行时缩放。KayKit CC0 碗/菜板/刀转换GLB，刀具旋转为平放；Kenney CC0 火焰及烟雾PNG转换为交叉双面透明卡片；效果由代码缩放/移动驱动。原文件及来源链接见上述记录。

## Frying audio and launcher artwork (2026-11-04)
- Frying pan #2 (0144), Joseph SARDIN / BigSoundBank, CC0. https://bigsoundbank.com/frying-pan-2-s0144.html ; https://bigsoundbank.com/licenses.html . Edited: 6-second excerpt, 250ms loop crossfade, mono PCM.
- Launcher icon: supplied by the project owner in this conversation; replaces template icon.

- Additional tabletop foods: Kenney Food Kit, CC0, imported GLBs unmodified (runtime scale/placement only). Meat, fries, donut, mushroom, fish, eggplant, apple, egg, onion, bread, pepper, cheese.

- Added Kenney Food Kit carrot, broccoli, tomato, cabbage and dim-sum (CC0). Flame/smoke cards are instantiated in several animated layers; oil bubbles and droplets are runtime geometry, not a fluid simulation.

## Expanded sushi and atlas integration — 2026-11-04 / v1.2

- Quaternius Sushi Restaurant Kit, CC0, https://quaternius.com/packs/sushirestaurantkit.html . User-provided Food / Environment / Characters archives. Selected 10 food models, 4 environment props and 2 chef characters converted from glTF to self-contained GLB with Blender. Characters currently use static rest poses; no skeletal animation playback claimed. Model roster: docs/SUSHI_IMPORTS.json. Archive hashes: docs/EXPANDED_INTAKE.json.
- para, Animated particle effects #2, CC0, https://opengameart.org/content/animated-particle-effects-2 . fire_01.png and air_bubbles_02.png are kept unchanged and embedded in GLB geometry with 64 atlas UV frames. Runtime switches frames; no downloaded Unity package or fluid simulation is used.
