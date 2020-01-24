package basicDataModel;

import ch.hevs.cloudio.endpoint.*;

public class DemoObject extends CloudioObject {

    //static attribute must be initialized
    @StaticAttribute
    public double demoStatic = 42.42;

    @Measure
    public CloudioAttribute<Double> demoMeasure;

    @Parameter
    public CloudioAttribute<Boolean> demoParameter;

    @Status
    public CloudioAttribute<Integer> demoStatus;

    @SetPoint
    public CloudioAttribute<Integer> demoSetpoint;

}
