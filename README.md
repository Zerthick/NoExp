# NoExp - Prevent Players from gaining Experience

## The Idea
The idea behind NoExp is simple, in vanilla Minecraft, players can gain experience points through a variety of actions such as killing mobs, fishing, or mining ores.  The aim of NoExp is to restrict which of these actions produce experience. It does this through the use of a simple config file located at `~/mods/config/NoExp.conf`.  The default config is shown below:

```
# Entites within this map will be monitored by NoExp 
# If their id is set to false then they won't drop exp orbs when bred or killed
entityMap {
    "minecraft:bat"=true
    "minecraft:blaze"=true
    "minecraft:cave_spider"=true
    "minecraft:chicken"=true
    "minecraft:cow"=true
    "minecraft:creeper"=true
    "minecraft:donkey"=true
    "minecraft:elder_guardian"=true
    "minecraft:ender_dragon"=true
    "minecraft:enderman"=true
    "minecraft:endermite"=true
    "minecraft:evocation_illager"=true
    "minecraft:ghast"=true
    "minecraft:giant"=true
    "minecraft:guardian"=true
    "minecraft:horse"=true
    "minecraft:husk"=true
    "minecraft:illusion_illager"=true
    "minecraft:llama"=true
    "minecraft:magma_cube"=true
    "minecraft:mooshroom"=true
    "minecraft:mule"=true
    "minecraft:ocelot"=true
    "minecraft:parrot"=true
    "minecraft:pig"=true
    "minecraft:polar_bear"=true
    "minecraft:rabbit"=true
    "minecraft:sheep"=true
    "minecraft:shulker"=true
    "minecraft:silverfish"=true
    "minecraft:skeleton"=true
    "minecraft:skeleton_horse"=true
    "minecraft:slime"=true
    "minecraft:snowman"=true
    "minecraft:spider"=true
    "minecraft:squid "=true
    "minecraft:stray"=true
    "minecraft:vex "=true
    "minecraft:villager"=true
    "minecraft:villager_golem"=true
    "minecraft:vindication_illager"=true
    "minecraft:witch "=true
    "minecraft:wither"=true
    "minecraft:wither_skeleton"=true
    "minecraft:wolf"=true
    "minecraft:zombie"=true
    "minecraft:zombie_horse"=true
    "minecraft:zombie_pigman"=true
    "minecraft:zombie_villager"=true
}
# Whether exp bottles should produce exp orbs Default:true
expBottle=true
# Whether fishing should produce exp orbs Default:true
fishing=true
# Whether mining blocks should produce exp orbs Default:true
mining=true
# Whether smelting should produce exp orbs Default:true
smelting=true
```
By setting the node for the corresponding action to false, the action will no long produce experience points.

## Planned Features
 * Spliting entity actions into separate breed and kill actions
 * Controlling players dropping experience points when killed
 * Controlling experience drops when mining specific blocks and smelting specific items
