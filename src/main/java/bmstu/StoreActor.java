package bmstu;

import akka.actor.AbstractActor;
import akka.japi.Pair;

import java.util.Map;

public class StoreActor extends AbstractActor {
    static Map<String, Integer> store;
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        String.class,
                        url -> getRes(url))
                .match(
                        Pair.class,
                        resp -> putInMap(resp)
                )
                .build();
    }
    private static Integer getRes(String req){
        System.out.println(req);
        return new Integer(store.getOrDefault(req, -1));
    }
    private static void putInMap(Pair<String , Integer> res){
        store.put(res.first() , res.second());
    }
}
