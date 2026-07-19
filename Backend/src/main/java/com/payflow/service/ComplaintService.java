package com.payflow.service;

import com.payflow.dto.complaint.ComplaintResponse;
import com.payflow.dto.complaint.CreateComplaintRequest;
import com.payflow.dto.complaint.ResolveComplaintRequest;
import com.payflow.entity.User;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public interface ComplaintService {
	 ComplaintResponse create(User user, CreateComplaintRequest request);

	    List<ComplaintResponse> listMine(User user);

	    List<ComplaintResponse> listAll();

	    ComplaintResponse resolve(String id, ResolveComplaintRequest request);
    
}