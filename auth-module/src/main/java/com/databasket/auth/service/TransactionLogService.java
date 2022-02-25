package com.databasket.auth.service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.databasket.auth.config.UserRevEntity;
import com.databasket.auth.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Repository
@Transactional
public class TransactionLogService {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	EntityManager emEntityManager;
	
	//Get all revision log for UserId(optional) between date range
	public List<Map<String, String>> getAllRevisionBetweenForUser(Date startDate, Date endDate, Long userId){		
		//Audit query selection depending search params
		List<Object[]> revisions = querySelection(startDate, endDate, userId);		

	    //Construct Return Data for API
		 List<Map<String, String>> revisionList = new ArrayList<>();//return data
		 
		 ObjectMapper mapper = new ObjectMapper(); 
		 mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 	 
		 mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		 
		 try {
			User currentRevUser;
						
			for(int i=0; i<revisions.size(); i++){
				Object[] currentRevision = (Object[]) revisions.get(i);				
				currentRevUser = new User();
				
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				currentRevUser = mapper.readValue(mapper.writeValueAsString(currentRevision[0]), User.class);
				currentRevUser.setPassword("*****");
				
				UserRevEntity currentRevDetail = new UserRevEntity();
				currentRevDetail = mapper.readValue(mapper.writeValueAsString(currentRevision[1]), UserRevEntity.class);
							
	         	Map<String, String> obj = new HashMap<>(); 
	         	obj.put("revisionId", currentRevDetail.getId() + "");
	         	obj.put("revisionDate", currentRevDetail.getTimestamp() + "");
	         	obj.put("revisionAuditor", currentRevDetail.getUsername());
	         	obj.put("operation", currentRevision[2].toString());	
	         	obj.put("user", mapper.writeValueAsString(currentRevUser));
         	
	         	revisionList.add(obj);
				}	

		} 
		catch (JsonProcessingException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();} 
		catch (IllegalArgumentException e) {e.printStackTrace();} 			 

	    return revisionList;
	}
	
	//Get revision log with field changes for UserId at RevisionId
	public List<Map<String, String>> getRevisionForUserAtRevision(Long userId, Long revisionId) throws IOException {
		LOGGER.info("getPropertyChanges : userId = {} revisionId = {}", userId, revisionId);
		List<Map<String, String>> returnList = new ArrayList<>();
		
		 AuditReader auditReader = AuditReaderFactory.get(emEntityManager);

		 //Querying for revisions, at which entities of a given class changed
		 AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(User.class, false, true);
		 
		 //filter username here
		 query.add(AuditEntity.id().eq(userId));
		 
		 List<Object> queryList = query.getResultList(); //executing query
			
		 ObjectMapper mapper = new ObjectMapper(); 
		 mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); 
		 mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		try {
			//Retrieve property changes
			User currentAudit, previousAudit;
						
			for(int i=0; i<queryList.size(); i++){
				Object[] queryData = (Object[]) queryList.get(i);
				
				UserRevEntity rev = new UserRevEntity();
				rev = mapper.readValue(mapper.writeValueAsString(queryData[1]), UserRevEntity.class);				
				
				if(rev.getId() == revisionId){//java.lang.ArrayIndexOutOfBoundsException: for first record
					
					if(queryList.size() == 1){//for single revision
						Object[] queryCurrentData = (Object[]) queryList.get(i);
						
						currentAudit = new User();
						currentAudit = mapper.readValue(mapper.writeValueAsString(queryData[0]), User.class);
						
		            	Map<String, String> obj = new HashMap<>(); 
		            	obj.put("revisionId", rev.getId() + "");
		            	obj.put("revisionDate", rev.getTimestamp() + "");
		            	obj.put("revisionAuditor", rev.getUsername());
		            	obj.put("operation", queryCurrentData[2].toString());	
		            	obj.put("user", mapper.writeValueAsString(currentAudit.compareFields(currentAudit)));
		            	
		            	returnList.add(obj);								
					}
					else{//for multiple revision
						Object[] queryPreviousData = (Object[]) queryList.get(i-1);
						
						currentAudit = new User();
						previousAudit = new User();
						
						currentAudit = mapper.readValue(mapper.writeValueAsString(queryData[0]), User.class);
						previousAudit = mapper.readValue(mapper.writeValueAsString(queryPreviousData[0]), User.class);
												
		            	Map<String, String> obj = new HashMap<>(); 
		            	obj.put("revisionId", rev.getId() + "");
		            	obj.put("revisionDate", rev.getTimestamp() + "");
		            	obj.put("revisionAuditor", rev.getUsername());
		            	obj.put("operation", queryPreviousData[2].toString());	
		            	obj.put("user", mapper.writeValueAsString(previousAudit.compareFields(currentAudit)));
		            	
		            	returnList.add(obj);						
					}
				}				
			}			
			return returnList;
		} 
			catch (JsonProcessingException e) {e.printStackTrace();} 
			catch (IllegalAccessException e) {e.printStackTrace();} 
			catch (IllegalArgumentException e) {e.printStackTrace();} 	
		
		return returnList;
	}	

	private List<Object[]> querySelection(Date startDate, Date endDate, Long userId){
		AuditReader auditReader = AuditReaderFactory.get(emEntityManager);		
		List<Object[]> revisions = null;		
		
		if(userId == null && startDate != null && endDate != null){	
		    revisions = (List<Object[]>) auditReader.createQuery()
	                .forRevisionsOfEntity(User.class, false, true)
	                .add(AuditEntity.revisionProperty("timestamp").gt(startDate.getTime()))
	                .add(AuditEntity.revisionProperty("timestamp").lt(endDate.getTime()))
	                .getResultList();				
		}
		else if(userId != null && (startDate == null || endDate == null)){
		    revisions = (List<Object[]>) auditReader.createQuery()
	                .forRevisionsOfEntity(User.class, false, true)
	                .add(AuditEntity.id().eq(userId))
	                .getResultList();				
		}
		else if(userId == null && (startDate == null || endDate == null)){
		    revisions = (List<Object[]>) auditReader.createQuery()
	                .forRevisionsOfEntity(User.class, false, true)
	                .getResultList();				
		}		
		else{
		    revisions = (List<Object[]>) auditReader.createQuery()
	                .forRevisionsOfEntity(User.class, false, true)
	                .add(AuditEntity.id().eq(userId))
	                .add(AuditEntity.revisionProperty("timestamp").gt(startDate.getTime()))
	                .add(AuditEntity.revisionProperty("timestamp").lt(endDate.getTime()))
	                .getResultList();				
		}
		return revisions;		
	}	
}
