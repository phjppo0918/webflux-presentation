package com.example.webfluxpresentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @PostMapping
    public Mono<Member> save(@RequestBody Member member) {
        return memberRepository.save(member);
    }

    @GetMapping
    public Flux<Member> getAll() {
        return memberRepository.findAll();
                //.delayElements(Duration.ofSeconds(1));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Member> getAllStream() {
        return  memberRepository.findAll().delayElements(Duration.ofSeconds(1));
    }
}
