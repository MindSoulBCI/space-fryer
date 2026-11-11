package com.example.spacefryerdiner.p91d27a30.ui.game

import com.pico.spatial.core.ecs.*
import com.pico.spatial.core.ecs.resource.*
import com.pico.spatial.core.math.*
import com.example.spacefryerdiner.p91d27a30.domain.model.*
import kotlin.math.sin
import android.util.Log
import kotlinx.coroutines.CancellationException

/** Imported food and kitchen props, with a permanent self-service ingredient shelf. */
class FryerScene {
 val root=Entity()
 private val cube=MeshResource.createBox(Vector3(1f))
 private val sphere=MeshResource.createSphere(.5f)
 private val materials=mutableListOf<UnlitMaterial>()
 private fun material(r:Float,g:Float,b:Float)=UnlitMaterial.create().also{it.setBaseColor(Color4(r,g,b,1f));materials.add(it)}
 private val steel=material(.10f,.16f,.19f)
 private val trim=material(.27f,.40f,.43f)
 private val oil=material(.94f,.51f,.06f)
 private val mint=material(.18f,.78f,.68f)
 private val foodMaterial=material(1f,.88f,.59f)
 private val fireMaterial=material(1f,.35f,.04f)
 private fun model(mesh:MeshResource,mat:UnlitMaterial,p:Vector3,size:Vector3):Entity = ModelEntity(mesh,mat).also {
  it.components[TransformComponent::class.java]?.apply{setPosition(p);setScaleVector(size)};root.addChild(it)
 }
 private fun box(mat:UnlitMaterial,x:Float,y:Float,z:Float,w:Float,h:Float,d:Float)=model(cube,mat,Vector3(x,y,z),Vector3(w,h,d))
 private var closed=false
 private val pending=mutableListOf<Pair<String,Entity>>()
 private val loaded=mutableMapOf<String,Entity>()
 private val recipeModels=mapOf(1 to "corn",3 to "meat-raw",4 to "fries",8 to "banana",14 to "donut",15 to "mushroom",17 to "sushi-fillet",18 to "eggplant",20 to "apple",22 to "egg",23 to "onion",24 to "bread",26 to "pepper",27 to "cheese",31 to "carrot",32 to "broccoli",33 to "tomato",34 to "cabbage",35 to "dim-sum",5 to "sushi-shrimp",6 to "sushi-squid",16 to "sushi-crab",28 to "sushi-gyoza",36 to "sushi-avocado",37 to "sushi-salmon",38 to "sushi-rice",39 to "sushi-cucumber",40 to "sushi-octopus")
 private val extraEffects=listOf("fx-flame-0","fx-flame-1","fx-flame-2","fx-smoke-0","fx-smoke-1")
 private val spriteFrames=mutableMapOf<String,List<Entity>>()
 private val spriteIndex=mutableMapOf<String,Int>()
 private fun animateAtlas(name:String,clock:Float,fps:Float) {
  val frames=spriteFrames[name]?:return
  val next=(clock*fps).toInt()%frames.size
  val previous=spriteIndex[name]?:-1
  if(previous!=next){if(previous>=0)frames[previous].enabled=false;frames[next].enabled=true;spriteIndex[name]=next}
 }
 private var effectTime=0f
 private var lastFrameNanos=0L
 private val stockNames=(recipeModels.values + "popcorn").toList()
 suspend fun loadAssets() {
  for(name in (listOf("plate","fryer","pot","frying-pan","kay-bowl","kay-cuttingboard","kay-knife","flame","smoke","oil-atlas") + stockNames + listOf("sushi-counter","sushi-fridge","sushi-steamer","sushi-bottles","chef-panda","chef-rabbit"))) {
   try {
    val asset=when(name){"flame"->"para-fire";"oil-atlas"->"para-bubbles";else->name}
    val e=Entity.loadSuspend("asset://models/$asset.glb")
    if(closed)e.destroy() else {
     pending.add(name to e)
     val copies=if(name in stockNames)listOf("stock-$name") else extraEffects.filter{it.startsWith("fx-$name-")}
     copies.forEach { copyName ->
      val copy=e.clone(Entity.CloneOptions(recursive=true,shouldShareMaterialInstance=true))
      if(copy!=null)pending.add(copyName to copy) else Log.e("FryerAssets","Clone failed $copyName")
     }
    }
   } catch(e:CancellationException){throw e}
   catch(e:Exception){Log.e("FryerAssets","Failed to load $name",e)}
  }
 }
 private fun fit(e:Entity,width:Float,depth:Float,height:Float,x:Float,y:Float,z:Float) {
  val t=requireNotNull(e.components[TransformComponent::class.java])
  t.setPosition(Vector3(0f));t.setScaleVector(Vector3(1f))
  val b=e.getVisualBounds(root)
  require(b.size.x>0f && b.size.y>0f && b.size.z>0f){"Empty model bounds"}
  val scale=minOf(width/b.size.x,depth/b.size.z,height/b.size.y)
  t.setScaleVector(Vector3(scale))
  val scaled=e.getVisualBounds(root)
  t.setPosition(Vector3(x-scaled.center.x,y-scaled.min.y,z-scaled.center.z))
  val finalBounds=e.getVisualBounds(root)
  Log.i("FryerAssets","fit size=${finalBounds.size} center=${finalBounds.center} bottom=${finalBounds.min.y}")
 }
 private val foodNames=stockNames.toSet()
 private var visualKey=""
 private var oilHeight=.99f
 private var fryerPlaceholder:Entity?=null
 private var oilSurface:Entity?=null
 private val food:Entity
 private val fire:List<Entity>
 private val bubbles:List<Entity>
 private val droplets:List<Entity>
 init {
  // PICO world meters: +Y up, -Z away from the viewer. Tabletop is a prototype entity.
  box(steel,-.59f,.78f,-2.25f,.68f,.07f,.72f)
  box(steel,.59f,.78f,-2.25f,.68f,.07f,.72f)
  box(steel,-1.08f,.78f,-2.35f,.28f,.07f,.42f)
  box(steel,1.08f,.78f,-2.35f,.28f,.07f,.42f)
  // Three rows of food on the front counter, separate from the cooking/serving zones.
  box(steel,0f,.78f,-1.55f,2.50f,.07f,.68f)
  stockNames.forEachIndexed { i,_ ->
   box(mint,-1.035f+(i%10)*.23f,.822f,-1.32f-(i/10)*.22f,.19f,.012f,.18f)
  }
  fryerPlaceholder=box(trim,0f,.89f,-2.25f,.48f,.18f,.36f)
  oilSurface=box(oil,0f,.99f,-2.25f,.40f,.018f,.29f)
  box(mint,-.46f,.83f,-2.25f,.29f,.025f,.32f)
  box(mint,.46f,.83f,-2.25f,.29f,.025f,.32f)
  food=model(sphere,foodMaterial,Vector3(-.46f,.92f,-2.25f),Vector3(.09f,.09f,.09f))
  fire=(0..4).map { model(sphere,fireMaterial,Vector3((it-2)*.065f,.86f,-2.00f),Vector3(.035f,.09f,.035f)) }
  droplets=(0..11).map { model(sphere,oil,Vector3(0f),Vector3(.008f)).also{it.enabled=false} }
  bubbles=(0..23).map { model(sphere,oil,Vector3((it%4-1.5f)*.08f,1.02f,-2.25f+(it/4-.5f)*.12f),Vector3(.014f)) }
 }
 fun update(s:GameState,time:Float) {
  val now=java.lang.System.nanoTime()
  if(lastFrameNanos!=0L&&!s.suspended)effectTime+=((now-lastFrameNanos)/1_000_000_000f).coerceIn(0f,.1f)
  lastFrameNanos=now
  if(pending.isNotEmpty()) {
   val ready=pending.toList();pending.clear()
   ready.forEach { (name,e)->
    root.addChild(e)
    try {
     when(name) {
      "plate"->fit(e,.27f,.28f,.04f,.46f,.843f,-2.25f)
      "fryer"->{
       fit(e,.48f,.42f,.39f,0f,.70f,-2.25f)
       fryerPlaceholder?.enabled=false
       // Source oil plane at Blender Z=.28; source cabinet bottom=-.5086.
       val b=e.getVisualBounds(root)
       oilHeight=b.min.y+b.size.x/1.29483f*(.28f+.508626f)
       oilSurface?.components?.get(TransformComponent::class.java)?.setPosition(Vector3(0f,oilHeight,-2.25f))
      }
      "pot"->fit(e,.22f,.22f,.18f,-.78f,.815f,-2.42f)
      "frying-pan"->fit(e,.24f,.27f,.08f,.77f,.815f,-2.40f)
      "kay-bowl"->fit(e,.18f,.18f,.10f,.77f,.815f,-2.08f)
      "kay-cuttingboard"->fit(e,.25f,.20f,.025f,-.64f,.815f,-2.03f)
      "kay-knife"->fit(e,.18f,.10f,.03f,-.64f,.845f,-2.03f)
      "sushi-counter"->fit(e,2.0f,.80f,.80f,0f,0f,-3.25f)
      "sushi-fridge"->fit(e,.55f,.55f,1.45f,-1.35f,0f,-2.95f)
      "sushi-steamer"->fit(e,.22f,.22f,.22f,1.08f,.815f,-2.35f)
      "sushi-bottles"->fit(e,.26f,.18f,.25f,-1.08f,.815f,-2.35f)
      "chef-panda"->fit(e,.68f,.55f,.65f,.42f,.80f,-3.25f)
      "chef-rabbit"->fit(e,.68f,.55f,.65f,-.42f,.80f,-3.25f)
      "oil-atlas"->{fit(e,.20f,.20f,.18f,0f,oilHeight+.01f,-2.25f);e.enabled=false}
      "flame"->{fit(e,.24f,.16f,.13f,0f,.72f,-2.02f);e.enabled=false}
      "smoke"->{fit(e,.10f,.10f,.10f,0f,1.03f,-2.25f);e.enabled=false}
      else->if(name.startsWith("fx-")) {
       val flame=name.contains("flame")
       fit(e,.12f,.12f,.16f,0f,if(flame).72f else oilHeight+.03f,-2.02f)
       e.enabled=false
      } else if(name.startsWith("stock-")) {
       val i=stockNames.indexOf(name.removePrefix("stock-"))
       fit(e,.16f,.15f,.16f,-1.035f+(i%10)*.23f,.828f,-1.32f-(i/10)*.22f)
      } else {fit(e,.20f,.20f,.16f,-.46f,.86f,-2.25f);e.enabled=false}
     }
     if(name=="flame" || name.startsWith("fx-flame-") || name=="oil-atlas") {
      val frames=(0..63).mapNotNull{e.findEntity("frame_%02d".format(it))}
      require(frames.size==64){"Expected 64 atlas frame nodes, found ${frames.size}"}
      frames.forEach{it.enabled=false};spriteFrames[name]=frames;spriteIndex[name]=-1
      Log.i("FryerAssets","Atlas $name ready: ${frames.size} frames")
     }
     loaded[name]=e;visualKey=""
     Log.i("FryerAssets","Loaded $name")
    }catch(error:Exception){Log.e("FryerAssets","Placement failed $name",error);e.destroy()}
   }
  }
  val name=if(s.recipe.id==1 && s.phase==FoodPhase.PLATED) "popcorn" else recipeModels[s.recipe.id]
  val key="$name:${s.phase}"
  val activeModel=loaded[name]
  if(key!=visualKey){
   loaded.filterKeys{it in foodNames}.values.forEach{it.enabled=false}
   if(activeModel!=null && s.phase!=FoodPhase.EMPTY){
    activeModel.enabled=true
    val x=when(s.phase){FoodPhase.RAW->-.46f;FoodPhase.PLATED->.46f;else->0f}
    val y=when(s.phase){FoodPhase.RAW->.86f;FoodPhase.PLATED->loaded["plate"]?.getVisualBounds(root)?.max?.y ?: .86f;else->oilHeight+.02f}
    fit(activeModel,.20f,.20f,.16f,x,y,-2.25f)
   }
   visualKey=key
  }
  food.enabled=s.phase!=FoodPhase.EMPTY && activeModel==null
  val pos=when(s.phase){FoodPhase.RAW->Vector3(-.46f,.93f,-2.25f);FoodPhase.PLATED->Vector3(.46f,.92f,-2.25f);else->Vector3(0f,oilHeight+.05f,-2.25f)}
  food.components[TransformComponent::class.java]?.apply{setPosition(pos);setScaleVector(Vector3(if(s.phase==FoodPhase.READY||s.phase==FoodPhase.PLATED).16f else .09f))}
  foodMaterial.setBaseColor(when(s.phase){FoodPhase.BURNT->Color4(.06f,.05f,.04f,1f);FoodPhase.READY,FoodPhase.PLATED->Color4(1f,.65f,.12f,1f);else->Color4(1f,.88f-.3f*s.progress,.59f-.3f*s.progress,1f)})
  val cooking=s.phase==FoodPhase.COOKING||s.phase==FoodPhase.READY||s.phase==FoodPhase.BURNT
  val heat=.4f+s.focus*1.4f
  (listOf("flame")+extraEffects.filter{it.contains("flame")}).forEachIndexed { i,name ->
   loaded[name]?.let { e->
    e.enabled=cooking
    animateAtlas(name,effectTime+i*.17f,18f+s.focus*12f)
    val pulse=sin(effectTime*(9f+heat*5f)+i*1.7f)
    e.components[TransformComponent::class.java]?.apply {
     setPosition(Vector3((i-1.5f)*.085f,.72f,-2.02f))
     setScaleVector(Vector3(.08f+s.focus*.06f,.06f+s.focus*.24f+.012f*pulse,.08f+s.focus*.06f))
    }
   }
  }
  (listOf("smoke")+extraEffects.filter{it.contains("smoke")}).forEachIndexed { i,name ->
   loaded[name]?.let { e->
    e.enabled=cooking
    val age=(effectTime*(.3f+heat*.12f)+i/3f)%1f
    val size=(.06f+.08f*age)*(if(age>.8f)(1f-age)*5f else 1f)
    e.components[TransformComponent::class.java]?.apply {
     setPosition(Vector3(.045f*sin(effectTime+i),oilHeight+.02f+age*.23f,-2.25f))
     setScaleVector(Vector3(size))
    }
   }
  }
  loaded["oil-atlas"]?.let { e->
   e.enabled=s.phase==FoodPhase.COOKING||s.phase==FoodPhase.READY
   animateAtlas("oil-atlas",effectTime,(12f+18f*s.focus)*(.35f+.65f*s.mood))
   e.components[TransformComponent::class.java]?.apply {
    setPosition(Vector3(0f,oilHeight+.01f,-2.25f))
    setScaleVector(Vector3(.25f,.12f+s.focus*.12f,.14f))
   }
  }
  droplets.forEachIndexed { i,e ->
   val age=(effectTime*(1f+heat)+i/12f)%1f
   e.enabled=cooking && s.phase!=FoodPhase.BURNT && age<.65f
   e.components[TransformComponent::class.java]?.apply {
    setPosition(Vector3(sin(i*2.4f)*(.025f+age*.16f),oilHeight+.02f+(.13f+heat*.03f)*sin(age*3.14159f),-2.25f+sin(i*4.1f)*(.02f+age*.10f)))
    setScaleVector(Vector3(.009f*(1f-age)))
   }
  }
  fire.forEach { it.enabled=cooking && loaded["flame"]==null }

  fireMaterial.setBaseColor(if(s.focus>=.75f)Color4(.30f,.35f,1f,1f) else Color4(1f,.35f,.04f,1f))
  fire.forEachIndexed { i,e->e.components[TransformComponent::class.java]?.setScaleVector(Vector3(.035f,.025f+s.focus*.11f+(if(s.suspended)0f else .01f*sin(time*7+i)),.035f)) }
  bubbles.forEachIndexed { i,e->
   val age=(effectTime*(1f+heat*2f)+i/24f)%1f
   e.enabled=(s.phase==FoodPhase.COOKING||s.phase==FoodPhase.READY)&&age<.88f
   e.components[TransformComponent::class.java]?.apply {
    setPosition(Vector3((i%6-2.5f)*.058f,oilHeight+.012f+age*.025f,-2.25f+(i/6-1.5f)*.065f))
    setScaleVector(Vector3(.005f+age*(.010f+heat*.004f)))
   }
  }
 }
 fun close(){closed=true;pending.forEach{it.second.destroy()};pending.clear();root.destroy();cube.close();sphere.close();materials.forEach{it.close()}}
}
