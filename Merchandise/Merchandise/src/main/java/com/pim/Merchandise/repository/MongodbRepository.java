package com.pim.Merchandise.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.pim.Merchandise.model.PIMProductTracker;

public interface MongodbRepository extends MongoRepository<PIMProductTracker,String>{

	@Query(value="{fileName:'?0'}")
	List<PIMProductTracker> findAll(String category);
}
