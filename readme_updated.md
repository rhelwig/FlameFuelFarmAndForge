# About this Minecraft Mod

[This doc is exploring the possibilities.]

This mod expands Minecraft into a richer **Copper–Bronze–Brass age** progression with realistic materials, burning torches, new fuels, early industrial machines, and a full fluid & hydraulic power system.

## Torches burn out

Both regular and copper torches will burn out. Regular torches last about a day, copper torches last three days.

Once they are burned out they become a Burnt Torch. When these are mined they drop only ashes (i.e. nothing). If they are mined before they burn out they don't drop themselves, but instead drop Kindling.

Kindling is only useful for burning in furnaces.

## Torches cause fire damage

If you cross through a torch's hitbox there is a decent chance you take a little fire damage. Mobs will also take the same damage. So be careful about where you place them!

If you hold a torch and hit a mob or animal with it, it will do some fire damage and can set it on fire. This applies to regular, copper, redstone, and soul torches.

Also, regular torches, but not copper torches, can set things they are placed on on fire. Be careful not to place them on things that can burn. Coal/Anthracite ore does burn.

## World generation of torches is updated

Torches that get placed on world generation (such as in villages) are changed to be either copper torches or lanterns. If they *can* be lanterns there is a good chance they will be - we try to place lanterns in the torch’s space and immediate neighbor blocks. Copper torches that are generated this way can still burn out.

## Animals now drop Animal Fat

Animal Fat is a new item that some animals drop in addition to whatever they already do. This can be used as fuel in furnaces and lanterns.

A few of the ores are now renamed to be more accurate. For example, Raw Copper is now Malachite and Coal is Anthracite.

## Tin, Bronze, Zinc, and Brass

### Tin and Bronze

The mod adds Cassiterite Ore (AKA Tin), which can be smelted into Tin Ingots. Tin Ingots can be crafted into Tin Nuggets and Tin Blocks (and vice-versa).

It allows crafting Bronze Blocks, Bronze Ingots, Bronze Nuggets, Bronze Tools, Bronze Swords, and Bronze Armor. It disables the copper tools, swords, and armor since those were always made with bronze.

Stone swords, pickaxe, and shovel are disabled since those don't make any sense.

We now have Bronze Lanterns and Bronze Hoppers (that have 3 spaces instead of 5 and are slower).

Lanterns now require Animal Fat to light up. They need to be refilled, holding 16 units in a single slot. One unit of Animal Fat lasts a day.

### Zinc and Brass

Added Zinc Ore (AKA Sphalerite), which smelts into Zinc Ingots (but only in a Blast Furnace or better).

Can now craft Zinc Nuggets and Blocks.

Brass is crafted from 8 Copper Ingots + 1 Zinc Ingot (Brass Block), or from 8 copper nuggets + 1 zinc nugget (Brass Ingot).

Brass is used for advanced pumps, valves, turbines, and hydraulic machinery.

## Copper Kettle

The Copper Kettle is a small utility block for early liquid handling.

It functions like a cauldron but:
- Capacity: 2 units  
- Can hold water, Animal Fat, or Oil  
- Cannot hold hot liquids  
- Accepts drips from above  
- Can be heated by campfire, soul campfire, furnace, etc.

### Converting Fat → Oil

If the kettle contains Animal Fat and is heated, it converts the fat into Oil over time.

### Composters Produce Oil

Whenever a composter produces Bone Meal, it drips 1 unit of oil downward.  
A Copper Kettle or Cauldron below will collect it.

## Fluid System Overview

Liquids use a simple **unit system**:
- 1 unit = 1 bottle  
- 3 units = 1 cauldron

### Copper Pipes

Copper Pipes create **single-fluid networks**:
- Each network can contain only one fluid type  
- Connecting different-fluid networks is blocked  
- Networks store a virtual pool of fluid  
- Can transport water, Animal Fat, and Oil (but not hot fluids)

## Mechanical Power (Bronze Age)

### Waterwheels

Spin when adjacent to flowing or falling water, generating mechanical rotation.

### Axles

Transmit mechanical rotation to machines.

### Bronze Mechanical Pump

Requires axle input.  
Moves 1 unit/tick horizontally (slower upward).  
Generates **low pressure** only.  
Not redstone controlled.

## Brass Age: Pressure & Hydraulics

### Pressure States

Networks have three pressure levels:
1. No Pressure  
2. Low Pressure (bronze pumps or gravity)  
3. High Pressure (brass pumps)

### Brass Pump

Powered by redstone.  
Moves 2 units/tick.  
Generates **high pressure**.  
Can act as an on/off valve.

### Valves

- **Manual Valve** – hand toggle  
- **Check Valve** – one-way flow  
- **Redstone Valve** – opens on redstone  
- **Pressure Valve** – opens only under high pressure  

## Hydraulic Pistons

- No Pressure → Retract  
- Low Pressure → Hold  
- High Pressure → Extend  

Used for elevators, gates, presses, farms, drawbridges, and logic.

## Water Turbines

Placed inline with pipes.  
Consume water units per tick to produce mechanical rotation.  
High-pressure input produces more power.

## Future Additions

- Brass Tanks  
- Tin Sling + Tin Bullets  
- Dirt crafting (Sand + Bone Meal)  
- Decorative gears, crankshafts  
- Aurochs: large cattle with wool, meat, and extra fat drops
