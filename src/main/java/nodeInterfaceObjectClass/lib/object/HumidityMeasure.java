package nodeInterfaceObjectClass.lib.object;

import ch.hevs.cloudio.endpoint.*;

@Conforms("HumidityMeasure")
public class HumidityMeasure extends CloudioObject {

    @StaticAttribute
    public String unit = "%";

    @Measure
    public CloudioAttribute<Double> humidity;

}