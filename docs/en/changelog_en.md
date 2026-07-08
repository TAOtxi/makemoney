### v2.6.0+1.21.11
Add highlight entity module<br>
Update english translation

### v2.5.5+1.21.11
Now MenuClick module can operate without delay.<br>
Add task to automatically drop items in the inventory


### v2.5.0+1.21.11
Change the Config Array configuration class to generic.<br>
AutoAFK module update:
- Add dynamic attack cycle based on tps
- Add rescue command when tps is below danger threshold
- Add return to command when tps returns to good threshold


### v2.4.0+1.21.11
MendingHelper update:
- When there is netherite equipment in the inventory and its durability is not yet full, it will automatically enchant mending<br>
AutoDrop module update:
- Add durability match sub-condition
- item be clean in container now can be put in the inventory

### v2.3.1+1.21.11 - v2.3.3+1.21.11
AutoDrop module update:
- Add blacklist mode
- Add clean container items function
- Add config gui to fold match condition

### v2.3.0+1.21.11
Rewrite AutoDrop module

### v2.2.2+1.21.11
Optimize AutoDrop module issue:
- Fix match condition issue (before it forgot to initialize)
- Optimize match process, add default namespace for id field, clear tag field if it contains '*'
- Optimize throttle mechanism
- Optimize configuration field class

### v2.2.1+1.21.11
Because too lazy to adapt the container, now the autodrop module will disable drop when opening the container (there was originally an option to control it, but there was actually a problem and it was deleted...)

### v2.2.0+1.21.11
Add menu click module

### v2.1.0+1.21.11
Add message command module

### v2.0.0+1.21.11
- Rewrite configuration management tool
- Module autodrop adds reset configuration and reload configuration function
- Module autodrop adds pick up specified item trigger function (`ClientboundTakeItemEntityPacket`)
- Use task queue instead of eventbus
- Add Gui tool to open config gui
- Add configuration version update prompt window
- Use task queue instead of most tick loop
- Remove loop detection of player riding, listen to packet `ClientboundSetPassengersPacket` to check if player is riding or not
- Cache configuration items
- tag contains `beta` field triggers ci pre-release build

### v1.2.7+1.21.11
autoride can prevent players from riding on you

### v1.2.6+1.21.11
- Add tools to obtain real online players
- Exclude living entities from riding target
- Right-click riding excludes entities with saddle and harness

### v1.2.5+1.21.11
Fix autofish binding issue

### v1.2.1+1.21.11
Module autofish update:
- Add throwing rod with random delay, ranging from 1 to 20 ticks
- Add custom delay for throwing rod
- Improve command feedback information

### v1.2.0+1.21.11
Add autofish module:
- Create task tool class
- Periodic fishing detection: fishing rod is thrown again if it is not in water for a long time [Wiki](https://minecraft.wiki/w/Java_Edition_protocol/Entity_metadata#Fishing_Bobber)
- Can dynamically rotate the angle of view, switching between the direction of the previous rotation and the current rotation

### v1.0.3+1.21.11
Module autodrop adds a drop count control option

### v1.0.1+1.21.11
Split the config gui of each module and create a new main config gui