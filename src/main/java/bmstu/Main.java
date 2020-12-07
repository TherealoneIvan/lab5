package bmstu;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.sun.tools.javac.code.Symbol;
import javafx.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.lang.Integer.parseInt;

public class Main {

    public static final String EMPTY_STRING = "";
    public static final char EQUALS_CHAR = '=';
    public static final int TIMEOUT_MILLIS = 5000;

    public static Flow<HttpRequest, HttpResponse, NotUsed> getCounter(Http http , ActorSystem actorSystem ,
                                                                      ActorMaterializer actorMaterializer , ActorRef storeActor){
        Flow.of(HttpRequest.class)
                .map(item -> {
                    String uri = item.getUri().toString();
                    String countOfReq = countBuilder(uri);
                    return new Pair<String , Integer> (item.getUri().query().toString() ,parseInt(countOfReq));
                })
                .mapAsync(
                        1 , (Pair<String , Integer> pair) -> {
                            CompletionStage<Object> result = (CompletionStage<Object>) Patterns.ask(storeActor , pair , TIMEOUT_MILLIS);
                            result.thenCompose( (Pair<Boolean, Integer> item ) ->{
                                if (item.getKey()){
                                    return  CompletableFuture.completedFuture(item.getValue());
                                }
                                Flow<Pair<String, Integer> , Integer , NotUsed> rFlow =
                                        Flow.<Pair<String , Integer>>create()
                                        .mapConcat(
                                                requestPair -> {
                                                   ArrayList<Pair <String ,Integer>> res = new ArrayList<Pair<String , Integer>>();
                                                   for (int i = 0 ; i < requestPair.getValue(); i++ )
                                                       res.add(new Pair<String, Integer>(requestPair.getKey() , requestPair.getValue()));
                                                   return res;
                                                }
                                        )
                                        .mapAsync()


                                    }
                            );
                        }
                )
    }
//    private static Future<Object> isInStore(Pair<String , Integer> req , ActorRef storeActor){
//         return Future<Object> result = Patterns.ask(storeActor , req , TIMEOUT_MILLIS);
//    };
    private static String countBuilder(String uri) {
        int i = uri.length() - 1;
        String countOfReq = EMPTY_STRING;
        while (uri.charAt(i) != EQUALS_CHAR){
            countOfReq += uri.charAt(i);
            i--;
        }
        return countOfReq;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class));
        final Http http = Http.get(system);
        final ActorMaterializer materializer =
                ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = Flow.of(HttpRequest.class);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", 8080),
                materializer
        );
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }
}
