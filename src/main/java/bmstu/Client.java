package bmstu;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.http.javadsl.model.HttpEntities;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static org.asynchttpclient.Dsl.asyncHttpClient;

public class Client {
    public static final int TIMEOUT_MILLIS = 5;
    private static Duration duration = Duration.ofSeconds(TIMEOUT_MILLIS);
    public static Flow<HttpRequest, HttpResponse, NotUsed> getCounter(ActorMaterializer actorMaterializer , ActorRef storeActor){
        return Flow.of(HttpRequest.class)
                .map(item -> {
                    String uri = item.getUri().query().get("testUrl").get();
                    String countOfReq = item.getUri().query().getOrElse("count" , "");
                    return new Pair<String , Long>(uri ,parseLong(countOfReq));
                })
                .mapAsync(
                        1 ,(Pair<String, Long> req) ->
                                Patterns.ask(storeActor , new String(req.first()) , duration)
                                .thenCompose( (Object item) -> {
                                    if ((Long) item != -1) {
                                        return CompletableFuture.completedFuture(new Pair<String,Long> (req.first(),(Long)item));
                                    }
                                    return Source.from(Collections.singletonList(req))
                                            .toMat(getSink(), Keep.right()).run(actorMaterializer)
                                            .thenApply(reqTime -> {
                                                System.out.println(req.first() + " " + reqTime / req.second());
                                                return new Pair<>(req.first(), reqTime / req.second());
                                            });
                                }))
                .map(resp -> {
                    storeActor.tell(resp , ActorRef.noSender());
                    return HttpResponse.create().withEntity(
                            HttpEntities.create(
                                    ((Pair<String , Long>) resp).first() + " " + ((Pair<String , Long>) resp).second())
                            );

                });
}

    private static Sink<Pair<String, Long>, CompletionStage<Long>> getSink() {
        return
                        Flow.<Pair<String , Long>>create()
                                .mapConcat(Client::concatReq)
                                .mapAsync( 3 , Client::asyncHttp)
                                .toMat(Sink.fold(0L , Long::sum) , Keep.right());
    }

    private static Iterable<Pair<String, Long>> concatReq(Pair<String, Long> requestPair) {
        ArrayList<Pair<String, Long>> res = new ArrayList<Pair<String, Long>>();
        for (int i = 0; i < requestPair.second(); i++)
            res.add(new Pair<String, Long>(requestPair.first(), requestPair.second()));
        return res;
    }

    private static CompletionStage<Long> asyncHttp(Pair<String, Long> request) {

        AsyncHttpClient asyncHttpClient = asyncHttpClient();
        Long startTime = System.currentTimeMillis();
        return asyncHttpClient
                .prepareGet(request.first())
                .execute()
                .toCompletableFuture()
                .thenCompose(response -> CompletableFuture.completedFuture(System.currentTimeMillis() - startTime));
    }
}
