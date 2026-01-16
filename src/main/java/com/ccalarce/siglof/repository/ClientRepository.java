package com.ccalarce.siglof.repository;

import com.ccalarce.siglof.model.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByDocumentNumber(String documentNumber);

    /**
     * Optimized query with JOIN FETCH to load all Clients with their TokenQR in a
     * single query.
     * Prevents N+1 queries when listing clients.
     */
    @Query("SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.tokenQr")
    List<Client> findAllWithTokens();

    /**
     * Paginated version of findAll with custom query (Spring Data handles
     * pagination).
     */
    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.tokenQr")
    Page<Client> findAllWithTokensPaginated(Pageable pageable);
}
