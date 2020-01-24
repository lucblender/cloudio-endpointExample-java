package nodeInterfaceObjectClass.lib.node;

import ch.hevs.cloudio.endpoint.CloudioNode;
import ch.hevs.cloudio.endpoint.Implements;
import nodeInterfaceObjectClass.lib.object.Switch;
import nodeInterfaceObjectClass.lib.object.TemperatureMeasure;

@Implements("TemperatureSensor")
public class TemperatureSensor extends CloudioNode {

    public Switch triggerSwitch;
    public TemperatureMeasure ambientTemperature;
}
