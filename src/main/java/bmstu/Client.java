package bmstu;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.asynchttpclient.AsyncHttpClient;
import java.util.ArrayList;
import java.util.Collections;
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
    public static Flow<HttpRequest, HttpResponse, NotUsed> getCounter(ActorMaterializer actorMaterializer , ActorRef storeActor){
        return Flow.of(HttpRequest.class)
                .map(item -> {
                    String uri = item.getUri().toString();
                    String countOfReq = countBuilder(uri);
                    return new Pair<String , Integer>(item.getUri().query().toString() ,parseInt(countOfReq));
                })
                .mapAsync(
                        1 ,(Pair<String, Integer> req) -> {
                            CompletionStage<Object> result = (CompletionStage<Object>) Patterns.ask(storeActor , new Pair<String , Integer>(req.first() , req.second()) , TIMEOUT_MILLIS);
                            result.thenCompose( (Object item) ->{
                                        if ((Integer) item != -1 ){
                                            return  CompletableFuture.completedFuture((Integer) item);
                                        }
                                        return Source.from(Collections.singletonList(req))
                                            .toMat(getSink(), Keep.right()).run(actorMaterializer)
                                                .thenApply(reqTime -> new Pair<>(req.first() , reqTime/req.second()));
                            });
                            return result;
                        })
                .map(resp -> {
                    storeActor.tell(resp , ActorRef.noSender());
                    return HttpResponse.create().withEntity(String.valueOf(resp));
                });
}

    private static Sink<Pair<String, Integer>, CompletionStage<Long>> getSink() {
        return
                        Flow.<Pair<String , Integer>>create()
                                .mapConcat(Client::apply)
                                .mapAsync( 3 , Client::asyncHttp)
                                .toMat(Sink.fold(0L , Long::sum) , Keep.right());
    }

    private static Iterable<Pair<String, Integer>> apply(Pair<String, Integer> requestPair) {
        ArrayList<Pair<String, Integer>> res = new ArrayList<Pair<String, Integer>>();
        for (int i = 0; i < requestPair.second(); i++)
            res.add(new Pair<String, Integer>(requestPair.first(), requestPair.second()));
        return res;
    }

    private static CompletionStage<Long> asyncHttp(Pair<String, Integer> request) {

        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        Long startTime = System.currentTimeMillis();
        return asyncHttpClient
                .prepareGet(request.first())
                .execute()
                .toCompletableFuture()
                .thenCompose(response -> CompletableFuture.completedFuture(System.currentTimeMillis() - startTime));
    }
}
