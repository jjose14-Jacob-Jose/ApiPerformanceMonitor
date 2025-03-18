package com.stc.apm.repositories;

import com.stc.apm.models.ApmUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApmUserRepository extends MongoRepository<ApmUser, String> {
    Optional<ApmUser> findByUsername(String username);
    ApmUser findApmUserByUsername(String username);
    ApmUser findApmUserByEmailId(String emailId);
}
