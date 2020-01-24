package nodeInterfaceObjectClass.lib.object;

import ch.hevs.cloudio.endpoint.CloudioAttribute;
import ch.hevs.cloudio.endpoint.Conforms;
import ch.hevs.cloudio.endpoint.SetPoint;
import ch.hevs.cloudio.endpoint.Status;

@Conforms("Switch")
public class Switch {

    @SetPoint
    public CloudioAttribute<Boolean> state;

    @Status
    public CloudioAttribute<Boolean> stateFeedback;

}
