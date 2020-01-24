package nodeInterfaceObjectClass;

import nodeInterfaceObjectClass.lib.node.TemperatureSensor;

// This class extend directly TemperatureSensor who has the @Implement decorator
// Since we inherit only one class, it is possible to just extend it
// This mechanism only work with one class because Java... for multiple class Interface implementation
// the developer is in charge to declare the object by itself as seen in DemoNodeMultipleInterfaces example
public class DemoNodeOneInterface extends TemperatureSensor {
}
