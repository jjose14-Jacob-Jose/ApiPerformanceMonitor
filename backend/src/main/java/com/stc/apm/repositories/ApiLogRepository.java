package com.stc.apm.repositories;

import com.stc.apm.models.ApiCall;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiLogRepository extends MongoRepository<ApiCall, String> {
    List<ApiCall> findByCallerTimestampUTCBetween(String dateStart, String dateEnd);
}
