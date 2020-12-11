package bmstu;

import akka.actor.AbstractActor;
import akka.japi.Pair;

import java.util.Map;

public class StoreActor extends AbstractActor {
    static Map<String, Integer> store = new Map<String, Integer>()
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        String.class,
                        StoreActor::getRes)
                .match(
                        Pair.class,
                        StoreActor::putInMap
                )
                .build();
    }
    private static Integer getRes(String req){
        System.out.println(req);
        return new Integer(store.getOrDefault(req, -1));
    }
    private static void putInMap(Pair<String , Integer> res){
        System.out.println(res.first().getClass() + " - " + res.second());
        store.put(res.first() , res.second());
    }
}
