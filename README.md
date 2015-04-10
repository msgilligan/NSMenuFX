# NSMenuFX

A simple library allowing to access and modify the menu 
items that are generated by default for OS X applications.

More details about the project can be found at the codecentric blog: 
[Tweaking the Menu Bar of JavaFX 8 Applications on OS X](https://blog.codecentric.de/en/2015/04/tweaking-the-menu-bar-of-javafx-8-applications-on-os-x/)


## Usage

The following snippet shows a simple example on how to change a menu name:

    // Get the default menu bar as JavaFX object
    MenuBar menuBar = adapter.getMenuBar();
    
    // Change the name of the first menu item
    menuBar.getMenus().get(0).setText("Hello World");

    // Update the menu bar
	adapter.setMenuBar(menuBar);

To find more usage examples, have a look into the test classes.

## Remarks

Make sure to disable -XstartOnFirstThread in your run configuration.