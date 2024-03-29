package com.example.webfluxpresentation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
class FluxTest {
   @Test
   @DisplayName("Mono가 생성")
   void createMono() {
       Mono<String> sample = Mono.just("Hello, world");
       log.info("블로킹하여 데이터 가져오기 {}", sample.block());
       sample.map(i -> i + " : 데이터 추가")
               .doOnNext(i -> log.info("doOnNext : 스트림에 부가적인 수행 {}", i))
               .doOnSuccess(i -> log.info("doOnSuccess : 성공적으로 데이터를 처리했을 때 수행 {}", i))
               .doOnSubscribe(i -> log.info("doOnSubscribe : 구독하는 시점에 수행"))
               .subscribe();
   }

    @Test
    @DisplayName("Flux 생성")
    void createFlux() {
        Flux<String> sample = Flux.just("요소1", "요소2", "요소3");
        log.info("블로킹하여 첫 데이터 가져오기 {}", sample.blockFirst());
        log.info("블로킹하여 마지막 데이터 가져오기 {}", sample.blockLast());
        sample.map(i -> i + " : 데이터 추가")
                .doOnNext(i -> log.info("doOnNext : 스트림에 부가적인 수행 {}", i))
                .doOnComplete(() -> log.info("doOnComplete : 성공적으로 데이터를 처리를 완료 했을 때 수행 "))
                .doOnSubscribe(i -> log.info("doOnSubscribe : 구독하는 시점에 수행"))
                .subscribe();
    }

    @Test
    @DisplayName("mono test")
    void sinkTest() {
        Sinks.One<String> sinkOne = Sinks.one();
        Mono<String> mono = sinkOne.asMono();

        sinkOne.emitValue("Hello", Sinks.EmitFailureHandler.FAIL_FAST);

        mono.subscribe(v -> log.info("{}", v));
    }

    @Test
    @DisplayName("mono test")
    void sinkTest2() {
        Sinks.One<String> sinkOne = Sinks.one();
        Mono<String> mono = sinkOne.asMono();

        sinkOne.emitValue("1 data", Sinks.EmitFailureHandler.FAIL_FAST);
        sinkOne.emitValue("2 data", Sinks.EmitFailureHandler.FAIL_FAST);

        mono.subscribe(v -> log.info("첫 시도 : {}", v));
        mono.subscribe(v -> log.info("두번째 시도 : {}", v));
    }

    @Test
    @DisplayName("unicast Test")
    void unicastTest() {
        Sinks.Many<String> sinkMany = Sinks.many().unicast().onBackpressureBuffer();

        Flux<String> result = sinkMany.asFlux();

        sinkMany.emitNext("data 1", Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext("data 2", Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext("data 3", Sinks.EmitFailureHandler.FAIL_FAST);

        result.subscribe(v -> log.info("{}", v)); // 정상 실행

        result.subscribe(); // 두번째 구독 시 에러 발생 : unicast는 단일 Subscriber만 지원
    }

    @Test
    @DisplayName("replay Test")
    void replayTest() {
        Sinks.Many<String> sinkMany = Sinks.many().replay().all();

        Flux<String> result = sinkMany.asFlux();

        sinkMany.emitNext("data 1", Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext("data 2", Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext("data 3", Sinks.EmitFailureHandler.FAIL_FAST);

        result.subscribe(v -> log.info("1차 실행 : {}", v));
        result.subscribe(v -> log.info("2차 실행 : {}", v));
        sinkMany.emitNext("data 4", Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext("data 5", Sinks.EmitFailureHandler.FAIL_FAST);
        sinkMany.emitNext("data 6", Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    @DisplayName("subscribeOn test")
    void runSubscribeOn() {
        Flux.just("A", "B", "C")
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(data -> log.info("doOnNext:{}", data))
                .doOnSubscribe(sub -> log.info("doOnSubscribe"))
                .subscribe(data -> log.info("subscribe : {}", data));
    }

    @Test
    @DisplayName("publishOn test")
    void runPublishOn() {
        Flux.just("A", "B", "C")
                .map(data -> {
                    log.info("-----상단 map:{}", data);
                    return data;
                })
                .publishOn(Schedulers.parallel())
                .map(data -> {
                    log.info("---중단 map:{}", data);
                    return data;
                })
                .publishOn(Schedulers.parallel())
                .map(data -> {
                    log.info("하단 map:{}", data);
                    return data;
                })
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("subscribe : {}", data));
    }

    @Test
    @DisplayName("parallel test")
    void parallelOn() {
        Flux.just("A", "B", "C")
                .parallel()
                .runOn(Schedulers.parallel())
                .subscribe(data -> log.info("subscribe : {}", data));
    }


}
