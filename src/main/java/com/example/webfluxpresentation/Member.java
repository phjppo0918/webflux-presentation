package com.example.webfluxpresentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@NoArgsConstructor
public class Member {
    @Id
    private String name;
}
