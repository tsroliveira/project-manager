package com.portfolio.integration.members;

import com.portfolio.exception.BusinessException;
import com.portfolio.integration.members.dto.MemberCreateRequest;
import com.portfolio.integration.members.dto.MemberResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MemberClientImpl implements MemberClient {

    private final WebClient client;

    public MemberClientImpl(WebClient membersWebClient) {
        this.client = membersWebClient;
    }

    @Override
    public MemberResponse getById(String id) {
        return client.get()
                .uri("/members/{id}", id)
                .retrieve()
                .bodyToMono(MemberResponse.class)
                .blockOptional()
                .orElseThrow(() -> new BusinessException("Membro não encontrado na API externa."));
    }

    @Override
    public MemberResponse create(MemberCreateRequest req) {
        return client.post()
                .uri("/members")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(MemberResponse.class)
                .blockOptional()
                .orElseThrow(() -> new BusinessException("Falha ao criar membro na API externa."));
    }
}
