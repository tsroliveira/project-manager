package com.portfolio.repository;

import com.portfolio.domain.MemberCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberCacheRepository extends JpaRepository<MemberCache, String> {

    Optional<MemberCache> findByExternalId(String externalId);

    @Modifying
    @Query(value = """
        insert into member_cache (id, external_id, name, role, created_at, updated_at)
        values (:id, :externalId, :name, :role, now(), now())
        on conflict (external_id) do update
            set name = excluded.name,
                role = excluded.role,
                updated_at = now()
        """, nativeQuery = true)
    void upsertByExternalId(@Param("id") String id,
                            @Param("externalId") String externalId,
                            @Param("name") String name,
                            @Param("role") String role);
}
