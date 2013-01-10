# Welcome

Welcome to flashmind plugin wiki page. Flashmind is XMind plugin, created in order to export mindmaps in flash format.

## Installation
1. Download zip from [there](https://bitbucket.org/vershinin/flashmind/downloads).
2. Unpack into plugins directory
3. Restart XMind

## Usage
Export->Flash->Select path to save html file. 

Plugin creates html file and directory with resources (javascrpit, mindmap player, and mindmap in freemind format).

## Compilation from source
You should convert some xmind jar's into maven artifatcs, and install it into local .m2 repository. You can use following commands:  

`mvn install:install-file -DgroupId=org.xmind.ui -DartifactId=mindmap -Dversion=3.3.0 -Dpackaging=jar -Dfile=/usr/local/xmind/plugins/org.xmind.ui.mindmap_3.3.0.201208102038.jar`

`mvn install:install-file -DgroupId=org.xmind -DartifactId=core -Dversion=3.3.0 -Dpackaging=jar -Dfile=/usr/local/xmind/plugins/org.xmind.core_3.3.0.201208102038.jar`  

Then you can build plugin with `mvn package` command. You get distribution zip in your /target directory.
