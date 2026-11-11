"""Run with Blender --background --python tools/convert_sushi_assets.py."""
from pathlib import Path
import bpy,json
root=Path(__file__).resolve().parents[1]
src=root/'AssetIntake/20261104-expanded/Sushi';out=root/'app/src/main/assets/models'
items={
'sushi-shrimp':'Food/glTF/FoodIngredient_Ebi.gltf',
'sushi-squid':'Food/glTF/FoodIngredient_Squid.gltf',
'sushi-crab':'Food/glTF/FoodIngredient_Crabsticks.gltf',
'sushi-fillet':'Food/glTF/FoodIngredient_FishFillet.gltf',
'sushi-gyoza':'Food/glTF/Food_Gyoza.gltf',
'sushi-avocado':'Food/glTF/FoodIngredient_Avocado.gltf',
'sushi-salmon':'Food/glTF/FoodIngredient_Salmon.gltf',
'sushi-rice':'Food/glTF/FoodIngredient_Rice.gltf',
'sushi-cucumber':'Food/glTF/FoodIngredient_Cucumber.gltf',
'sushi-octopus':'Food/glTF/FoodIngredient_Octopus.gltf',
'sushi-counter':'Environment/glTF/Environment_Counter_Straight.gltf',
'sushi-fridge':'Environment/glTF/Environment_Fridge.gltf',
'sushi-steamer':'Environment/glTF/Environment_ChukamanSteamer.gltf',
'sushi-bottles':'Environment/glTF/Environment_Bottles.gltf',
'chef-panda':'Characters/With Knife and Pan/glTF/Panda.gltf',
'chef-rabbit':'Characters/With Knife and Pan/glTF/Rabbit_Pink.gltf',
}
for name,path in items.items():
 bpy.ops.wm.read_factory_settings(use_empty=True)
 bpy.ops.import_scene.gltf(filepath=str(src/path))
 # Keep imported rest pose for the two chef mascots; skeletal animation integration is separate.
 bpy.ops.export_scene.gltf(filepath=str(out/f'{name}.glb'),export_format='GLB',export_animations=False)
 print('EXPORTED',name,flush=True)
(root/'docs/SUSHI_IMPORTS.json').write_text(json.dumps(items,indent=2))
