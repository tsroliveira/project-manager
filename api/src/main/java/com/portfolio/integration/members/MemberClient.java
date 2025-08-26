package com.portfolio.integration.members;

import com.portfolio.integration.members.dto.MemberCreateRequest;
import com.portfolio.integration.members.dto.MemberResponse;

public interface MemberClient {
    MemberResponse getById(String id);
    MemberResponse create(MemberCreateRequest req);
}
