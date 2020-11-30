package bmstu;

import akka.actor.AbstractActor;
import javafx.util.Pair;

import java.util.Map;

public class StroreActor extends AbstractActor {
    Map<Pair<String , Integer>, Integer> store;
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Pair.class , )
    }
}
