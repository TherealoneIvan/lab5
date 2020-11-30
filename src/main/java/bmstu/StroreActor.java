package bmstu;

import akka.actor.AbstractActor;
import javafx.util.Pair;

import java.util.Map;

public class StroreActor extends AbstractActor {
    static Map<Pair<String , Integer>, Integer> store;
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        Pair.class,
                        pair -> getRes(pair));
    }
    private static Pair<Boolean , Integer> getRes(Pair<String , Integer> req){
        if (store.containsKey(req)){
            return new Pair<>(true , store.get(req));
        }else
            return new Pair<>(false , 0);
    }
}
