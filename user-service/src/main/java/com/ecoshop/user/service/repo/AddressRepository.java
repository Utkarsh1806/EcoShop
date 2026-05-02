package com.ecoshop.user.service.repo;

import com.ecoshop.user.service.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserId(UUID userId);
}
