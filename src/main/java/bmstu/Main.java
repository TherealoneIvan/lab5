package bmstu;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import javafx.util.Pair;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

import static java.lang.Integer.parseInt;

public class Main {

    public static final String EMPTY_STRING = "";
    public static final char EQUALS_CHAR = '=';

    public static Flow<HttpRequest, HttpResponse, NotUsed> getCounter(Http http , ActorSystem actorSystem , ActorMaterializer actorMaterializer){
        Flow.of(HttpRequest.class)
                .map(item -> {
                    String uri = item.getUri().toString();
                    String countOfReq = countBuilder(uri);
                    return new Pair<String , Integer> (item.getUri().query().toString() ,parseInt(countOfReq));
                })
                .map()
    }

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
