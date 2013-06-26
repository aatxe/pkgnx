# pkgnx #
pkgnx is a simple Java library for the NX file format. The format was designed by [Peter Atashian](http://github.com/retep998) and [angelsl](https://github.com/angelsl) and has a public specification available [here](http://nxformat.github.io/). The format intended on replacing the proprietary WZ data format designed by Wizet for the popular game, [MapleStory](http://www.maplestory.com).

## Using pkgnx ##
Using pkgnx is really quite simple! The first step, of course, is to include the library as a dependency either by adding it to your classpath or adding it as a maven dependency. Once that's done, you can start coding away. The code itself is quite simple.

```java
    NXFile file = new NXFile("path/to/file");
    // Do stuff, like...
    System.out.println(file.getRoot().getChild("Mob").getChild("8800000.img"));
    // Or...
    System.out.println(file.resolve("Mob/8800000.img"));
```
    
You can also parse the file later like so:
```java
    NXFile file = new NXFile("path/to/file", false);
    // Do some other stuff and then later...
    file.parse();
```

## Versioning ##
pkgnx follows the [Semantic Versioning guidelines](http://semver.org/). This means that the versions follow the structure Major.Minor.Patch where increments in patch number mean that no API changes took place, increments in minor number mean that all changes are backwards compatible, and increments in major number mean that changes are backwards-incompatible. This should make it easy to know when it's safe for you as a user to update this library!

## Contributing ##
To contribute a patch to pkgnx, simply fork it and run. When you've finished all of your commits, go ahead and send a pull request. All changes are required to match the official format specification and changes that do not fix bugs or add additional functionality will be rejected. This means no style changes!

## Acknowledgements ##
* [Peter Atashian](http://github.com/retep998) and [angelsl](https://github.com/angelsl) for designing the PKG specification.
* [Cedric Van Goethem](https://github.com/Zepheus) for creating [javanx](https://github.com/Zepheus/javanx), the PKG3 Java NX library.
* [angelsl](https://github.com/angelsl) for creating [libjinx](https://github.com/angelsl/ms-libjinx), the first Java NX library.

## Licensing ##
pkgnx is licensed under the MIT License. The full license text can be found in LICENSE.md for your convenience.
