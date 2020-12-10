package bmstu;

import akka.actor.AbstractActor;
import akka.japi.Pair;

import java.util.Map;

public class StoreActor extends AbstractActor {
    static Map<Pair<String , Integer>, Integer> store;
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        Pair.class,
                        pair -> getRes(pair))
                .build();
    }
    private static Integer getRes(Pair<String , Integer> req){
        System.out.println(req.first() + " " + req.second());
        return new Integer(store.getOrDefault(req, -1));
    }
}
