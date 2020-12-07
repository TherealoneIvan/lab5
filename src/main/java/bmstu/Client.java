package bmstu;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import javafx.util.Pair;
import org.asynchttpclient.AsyncHttpClient;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.lang.Integer.parseInt;
import static org.asynchttpclient.Dsl.asyncHttpClient;

public class Client {
    public static final String EMPTY_STRING = "";
    public static final char EQUALS_CHAR = '=';
    public static final int TIMEOUT_MILLIS = 5000;

    private static String countBuilder(String uri) {
        int i = uri.length() - 1;
        String countOfReq = EMPTY_STRING;
        while (uri.charAt(i) != EQUALS_CHAR) {
            countOfReq += uri.charAt(i);
            i--;
        }
        return countOfReq;
    }
    public static Flow<HttpRequest, HttpResponse, NotUsed> getCounter(Http http , ActorSystem actorSystem ,
                                                                      ActorMaterializer actorMaterializer , ActorRef storeActor){
        Flow.of(HttpRequest.class)
                .map(item -> {
                    String uri = item.getUri().toString();
                    String countOfReq = countBuilder(uri);
                    return new Pair<String , Integer>(item.getUri().query().toString() ,parseInt(countOfReq));
                })
                .mapAsync(
                        1 , (Pair<String , Integer> pair) -> {
                            CompletionStage<Object> result = (CompletionStage<Object>) Patterns.ask(storeActor , pair , TIMEOUT_MILLIS);
                            result.thenCompose( (Pair<Boolean, Integer> item ) ->{
                                        if (item.getKey()){
                                            return  CompletableFuture.completedFuture(item.getValue());
                                        }
                                        Flow<Pair<String, Integer>, Object, NotUsed> rFlow =
                                                Flow.<Pair<String , Integer>>create()
                                                        .mapConcat(Client::apply)
                                                        .mapAsync( 3 , Client::asyncHttp)
                                                        .toMat(Sink.fold())

                            
                        }
                
    }
}

    private static Iterable<Pair<String, Integer>> apply(Pair<String, Integer> requestPair) {
        ArrayList<Pair<String, Integer>> res = new ArrayList<Pair<String, Integer>>();
        for (int i = 0; i < requestPair.getValue(); i++)
            res.add(new Pair<String, Integer>(requestPair.getKey(), requestPair.getValue()));
        return res;
    }

    private static CompletionStage<Object> asyncHttp(Pair<String, Integer> request) {

        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        Long startTime = System.currentTimeMillis();
        return asyncHttpClient
                .prepareGet(request.getKey())
                .execute()
                .toCompletableFuture()
                .thenCompose(response -> CompletableFuture.completedFuture(System.currentTimeMillis() - startTime));
    }
}
