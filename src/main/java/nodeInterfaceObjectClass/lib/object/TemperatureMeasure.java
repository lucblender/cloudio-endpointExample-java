package nodeInterfaceObjectClass.lib.object;

import ch.hevs.cloudio.endpoint.*;

@Conforms("TemperatureMeasure")
public class TemperatureMeasure extends CloudioObject {

    @StaticAttribute
    public String unit = "K";

    @Measure
    public CloudioAttribute<Double> temperature;

}
