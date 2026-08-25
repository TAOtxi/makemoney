<div align="center"><center>

<img alt="Icon" src="../img/icon.png" width="200">

# Makemoney
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/TAOtxi/makemoney)

</center></div>

| <sub>EN</sub> [English](./README_EN.md) | <sub>ZH</sub> [中文](../README.md) |
| --- | --- |


<br>
Mod specifically designed for the `19 World` Server<br>
But it also has many common features.<br>
ohhhhhhh <br>

## Prerequisites
[Yacl >=3.8.2+1.21.11-fabric](https://modrinth.com/mod/yacl)

## Changelog
[history.md](./history_en.md)

### Config Gui
Through command `/makemoney config` or `Mod Menu`.

## Feature

### 🗑️ AutoDrop
Discard unnecessary items in the inventory according to the set matching criteria.<br>
Conditions can be set with names, id, durability, tags and enchantments.<br>
Basic commands:
```bash
/autodrop                      # Alias /ad
/ad help                       # Show help
/ad on | off                   # Enable/disable AutoDrop
/ad config                     # Open config gui
/ad reload                     # Reload config
/ad interval <tick>            # Set interval for drop
/ad test                       # Manual trigger drop
/ad ignore
       ├── current             # Ignore current slot that is not empty
       ├── clear               # Clear ignore list
       └── set <1,2,3,...>     # Set ignore slots, separated by ","
/ad timeTrigger on | off       # Enable/disable time trigger drop
/ad itemTrigger on | off       # Enable/disable item trigger drop
/ad containerTrigger on | off  # Enable/disable container trigger drop
```


### 🎣 AutoFish
By listening to network packets, judge whether to catch a fish.<br>
Basic commands:
```bash
/autofish                    # Alias /fish
/fish help                   # Show help
/fish on | off               # Enable/disable AutoFish
/fish config                 # Open config gui
/fish randomDelay on | off   # Enable/disable random delay
/fish throwDelay <tick>      # Set throw delay
```

### ♋ MendingHelper
When obtained experience orb, automatically replace the equipment with mending and less durability to the offhand<br>
When opening the anvil, automatically combined mending book for equipment<br>
When standing on a gold block, automatically trigger equipment salvage (mcmmo)<br>
When there is netherite equipment in the backpack, it automatically retrieves mending book from the container to enchant<br>
Basic commands:
```bash
/mendingHelper                    # Alias /mh
/mh config                        # Open config gui
/mh autoreplace on | off          # Enable/disable automatic replacement of mending equipment to the offhand
/mh autoenchant on | off          # Enable/disable automatic enchantment of mending book for equipment
/mh autodecompose on | off        # Enable/disable automatic equipment salvage
/mh autorepair on | off           # Enable/disable automatic repair of netherite equipment
/mh autorepair setMendingBookPos  # Set the target block position to the mending book position
```

### 🌐 AutoAFK
- Adjust attack interval dynamically based on server tps.<br>
- When server tps falls below the danger threshold, trigger the rescue command.<br>
  - After triggering the rescue command, if the tps recovers to a good threshold, trigger the home command.<br>
- Position check feature: trigger a command when the player is inside the specified area (usually to prevent the player from idling in the lobby).<br>
Basic commands:
```bash
/autoafk                        # Alias /afkk
/afkk config                    # Open config gui
/afkk help                      # Show help
/afkk attack on | off           # Enable/disable adaptive attack
/afkk attack interval <tick>    # Set attack interval
/afkk attack info               # Show attack interval info
/afkk tpsCheck on | off         # Enable/disable tps check
```

### 🐕 AutoRide
The server needs to support right-click players to ride on this player.<br>
Stick it on other players' heads like a dog skin ointment.<br>
You can also prohibit other players from riding on your head (maybe I really should delete this feature).<br>
Basic commands:
```bash
/autoride                  # Alias /ar
/ar help                   # Show help
/ar on | off               # Enable/disable AutoRide
/ar config                 # Open config gui
/ar distance <distance>    # Set judgment distance
/ar smoothHead on | off    # Enable/disable smooth head feature
/ar target <name>          # Lock target player name
/ar interval <tick>        # Set check interval
/ar reset                  # Reset all settings
```


### 📣 Listen Server Chat Messages
When the server message matches the set regular expression, the command will be executed.<br>
Support using regular expression to capture the content of the group as command parameters.<br>
For example, someone sends a message to the server: `<Him> Hello bro !`<br>
Set matching rule: `<(\w+)> (.*)`<br>
command can be set to: `/say ${1} send a message: ${2}`<br>
After replacement, it will execute: `/say Him send a message: Hello bro !`<br>
More detailed introduction please refer to the mod config gui in the game.<br>


### 🚫 IgnoreMessage
Customize the message rule to ignore. <br>

### 🎯 MenuClick
Customize the menu click slot, way, and interval. <br>
```bash
/menuClick                  # Alias /click
/click help                 # Show help
/click config               # Open config gui
/click run <task_name>      # Run task with specified name
```

### RightClickRide
Server needs to support command `/ride`, which can be used to ride on a living entity when look at it.<br>
Right-click a living entity to ride on it. <br>

### Specific tasks
Usage:
```bash
/task <taskName> on | off  # Enable/Disable task
```

#### One click throw out all items from the shulker box in the inventory (Server needs to support player can open shulker in hand by sneak and right-click)
Task name：`dropAllItemFromShulkerBox`<br>
Task process：Replace the non empty shulker box in the inventory with the main hand, and send data packets of sneak and usage items to the server. After waiting for the gui of the shulker box to open, all items in the shulker box will be automatically thrown out, and the empty box will be thrown in the due south direction. Repeat the above process until there are no non empty latent image boxes in the player's inventory, and the task will automatically finish (there is a small probability that non empty latent image boxes will also be thrown out)。
