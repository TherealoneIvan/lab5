package bmstu;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.Pair;

import java.util.HashMap;
import java.util.Map;

public class StoreActor extends AbstractActor {
    static Map<String, Long> store = new HashMap<String, Long>();
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        String.class,(String message) ->{
                            getSender().tell(getRes(message), ActorRef.noSender());
                        })
                .match(
                        Pair.class,
                        StoreActor::putInMap
                )
                .build();
    }
    private static Pair<String,Long> getRes(String req){
        System.out.println(req);
        return new Pair<String,Long> (req , new Long(store.getOrDefault(req, (long) -1)));
    }
    private static void putInMap(Pair<String , Long> res){
        System.out.println(res.first().getClass() + " - " + res.second());
        store.put(res.first() , res.second());
    }
}
