"""Wrap unchanged CC0 8x8 PNG atlases in GLBs; UV geometry exposes 64 switchable frames."""
from pathlib import Path
import struct,json
root=Path(__file__).resolve().parents[1];src=root/'AssetIntake/20261104-expanded/Particles';out=root/'app/src/main/assets/models'
for name,image in [('para-fire','fire_01.png'),('para-bubbles','air_bubbles_02.png')]:
 blob=bytearray();views=[];accessors=[]
 def view(data):
  while len(blob)%4:blob.append(0)
  start=len(blob);blob.extend(data);views.append({'buffer':0,'byteOffset':start,'byteLength':len(data)});return len(views)-1
 def acc(data,component,count,kind,**extras):
  accessors.append(dict(bufferView=view(data),componentType=component,count=count,type=kind,**extras));return len(accessors)-1
 pos=[-.5,0,0,.5,0,0,.5,1,0,-.5,1,0,0,0,-.5,0,0,.5,0,1,.5,0,1,-.5]
 pa=acc(struct.pack('<24f',*pos),5126,8,'VEC3',min=[-.5,0,-.5],max=[.5,1,.5])
 ia=acc(struct.pack('<12H',0,1,2,0,2,3,4,5,6,4,6,7),5123,12,'SCALAR')
 nodes=[];meshes=[]
 for f in range(64):
  col=f%8;row=f//8
  # glTF texture coordinates start at the image top; tiny inset avoids adjacent-frame bleeding.
  u0=(col*128+.5)/1024;u1=((col+1)*128-.5)/1024
  v0=(row*128+.5)/1024;v1=((row+1)*128-.5)/1024
  uv=[u0,v1,u1,v1,u1,v0,u0,v0]*2
  ua=acc(struct.pack('<16f',*uv),5126,8,'VEC2')
  meshes.append({'primitives':[{'attributes':{'POSITION':pa,'TEXCOORD_0':ua},'indices':ia,'material':0}]})
  nodes.append({'name':f'frame_{f:02d}','mesh':f})
 img=view((src/image).read_bytes())
 doc={'asset':{'version':'2.0','generator':'Diner atlas wrapper'},'scene':0,'scenes':[{'nodes':list(range(64))}], 'nodes':nodes,'meshes':meshes,'buffers':[{'byteLength':len(blob)}],'bufferViews':views,'accessors':accessors,'images':[{'bufferView':img,'mimeType':'image/png'}],'textures':[{'source':0,'sampler':0}],'samplers':[{'magFilter':9729,'minFilter':9729,'wrapS':33071,'wrapT':33071}],'materials':[{'doubleSided':True,'alphaMode':'BLEND','pbrMetallicRoughness':{'baseColorTexture':{'index':0},'metallicFactor':0,'roughnessFactor':1},'extensions':{'KHR_materials_unlit':{}}}],'extensionsUsed':['KHR_materials_unlit']}
 js=json.dumps(doc,separators=(',',':')).encode();js+=b' '*((-len(js))%4);blob+=b'\0'*((-len(blob))%4)
 data=struct.pack('<III',0x46546c67,2,12+8+len(js)+8+len(blob))+struct.pack('<II',len(js),0x4e4f534a)+js+struct.pack('<II',len(blob),0x004e4942)+blob
 (out/f'{name}.glb').write_bytes(data)
 print(name,len(data),'bytes, 64 frames')
