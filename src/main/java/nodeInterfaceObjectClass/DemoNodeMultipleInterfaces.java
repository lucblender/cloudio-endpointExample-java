package nodeInterfaceObjectClass;

import ch.hevs.cloudio.endpoint.CloudioNode;
import ch.hevs.cloudio.endpoint.Implements;
import nodeInterfaceObjectClass.lib.object.HumidityMeasure;
import nodeInterfaceObjectClass.lib.object.Switch;
import nodeInterfaceObjectClass.lib.object.TemperatureMeasure;

// Since java doesn't support multiple inheritance, we have to implement the class by hand when multiple
// interfaces. For single interface, inheritance can be used as seen in DemoNodeOneInterface example
@Implements({"TemperatureSensor", "HumiditySensor"})
public class DemoNodeMultipleInterfaces extends CloudioNode {

    //for the TemperatureSensor
    public Switch triggerSwitch;
    public TemperatureMeasure ambientTemperature;

    //for the HumiditySensor
    public HumidityMeasure humidity;

}