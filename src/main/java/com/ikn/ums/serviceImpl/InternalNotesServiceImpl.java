package com.ikn.ums.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ikn.ums.dto.DepartmentDto;
import com.ikn.ums.dto.EmployeeDto;
import com.ikn.ums.dto.InternalNoteCommentDto;
import com.ikn.ums.dto.InternalNotesDTO;
import com.ikn.ums.dto.TeamDto;
import com.ikn.ums.entity.InternalNoteComment;
import com.ikn.ums.entity.InternalNoteHistory;
import com.ikn.ums.entity.InternalNotes;
import com.ikn.ums.exception.BusinessException;
import com.ikn.ums.exception.ErrorCodeMessages;
import com.ikn.ums.repository.InternalNotesRepository;
import com.ikn.ums.service.InternalNotesService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InternalNotesServiceImpl implements InternalNotesService{

    @Autowired
    private InternalNotesRepository internalNotesRepository;
    
    @Autowired
    private ModelMapper modelMapper;

	@Autowired
	@Qualifier("internalRestTemplate")
	private RestTemplate restTemplate;
	
	@Autowired
	ModelMapper mapper;

    @Override
      public InternalNotesDTO saveInternalNotes(InternalNotesDTO internalNotesDTO) {

          log.info("In InternalNotesServiceImpl, saveInternalNotes(): Initializing internal note save process...");
          try {
              InternalNotes internalNotes = modelMapper.map(internalNotesDTO, InternalNotes.class);
              internalNotes.setCreatedDateTime(LocalDateTime.now());
              internalNotes.setInternalNoteDocument(internalNotesDTO.getInternalNoteDocument());
              internalNotes.setInternalNoteDocumentName(internalNotesDTO.getInternalNoteDocumentName());
              internalNotes.setInternalNoteDocumentType(internalNotesDTO.getInternalNoteDocumentType());
              InternalNotes savedEntity = internalNotesRepository.save(internalNotes);
              log.info("Internal note saved successfully with ID: {}", savedEntity.getInternalnoteId());

              return modelMapper.map(savedEntity, InternalNotesDTO.class);
          } catch (Exception e) {
              log.error("Unexpected error occurred while saving internal notes.", e);
              throw new BusinessException(ErrorCodeMessages.INTERNAL_NOTES_SAVE_ERROR_CODE,
                      ErrorCodeMessages.INTERNAL_NOTES_SAVE_ERROR_MSG);
          }
      }

    @Override
    @Transactional
    public InternalNotesDTO updateInternalNotes(InternalNotesDTO internalNotesDTO) {
        log.info("Updating internal note with ID: {}", internalNotesDTO.getInternalnoteId());
		log.info("updateInternalNotes() is under execution...");
        try {
            return internalNotesRepository.findById(internalNotesDTO.getInternalnoteId())
                .map(existingEntity -> {

                    String originalReviewerStatus = existingEntity.getReviewerStatus();
                    String originalApproverStatus = existingEntity.getApproverStatus();
                    String originalStatus = existingEntity.getInternalnotesStatus();
                    String originalTitle = existingEntity.getInternalnoteTitle();
                    String originalApprover = existingEntity.getInternalnoteApprover();
                    String originalReviewer = existingEntity.getInternalnoteReviewer();
                    String modifiedEmailId = internalNotesDTO.getModifiedByEmailId();
                    Map<String, String> empMap = getEmployeesMap();

                    List<InternalNoteHistory> historyList = existingEntity.getInternalNoteHistory();
                    if (historyList == null) {
                        historyList = new ArrayList<>();
                    }

                    modelMapper.map(internalNotesDTO, existingEntity);

                    boolean reviewerStatusChanged = !Objects.equals(originalReviewerStatus, internalNotesDTO.getReviewerStatus());
                    boolean approverStatusChanged = !Objects.equals(originalApproverStatus, internalNotesDTO.getApproverStatus());
                    boolean statusChanged = !Objects.equals(originalStatus, internalNotesDTO.getInternalnotesStatus());
                    boolean titleChanged = !Objects.equals(originalTitle, internalNotesDTO.getInternalnoteTitle());
                    boolean approverChanged = !Objects.equals(originalApprover, internalNotesDTO.getInternalnoteApprover());
                    boolean reviewerChanged = !Objects.equals(originalReviewer, internalNotesDTO.getInternalnoteReviewer());

                    if (reviewerStatusChanged) {
                        existingEntity.setReviewerDateTime(LocalDateTime.now());
                    }

                    if (approverStatusChanged) {
                        existingEntity.setApproverDateTime(LocalDateTime.now());
                    }

                    String reviewerStatus = internalNotesDTO.getReviewerStatus();
                    String approverStatus = internalNotesDTO.getApproverStatus();

                    if ("Reviewed - Approved".equals(reviewerStatus) && "Awaiting Approval".equals(approverStatus)) {
                        existingEntity.setInternalnotesStatus("Under Approval");
                    } else if ("Reviewed - Approved".equals(reviewerStatus) && "Approved".equals(approverStatus)) {
                        existingEntity.setInternalnotesStatus("Approved");
                    } else if ("Reviewed - Changes Requested".equals(reviewerStatus) && "Awaiting Approval".equals(approverStatus)) {
                        existingEntity.setInternalnotesStatus("Changes Required");
                    } else if ("Reviewed - Approved".equals(reviewerStatus) && "Rejected".equals(approverStatus)) {
                        existingEntity.setInternalnotesStatus("Changes Required");
                    } else {
                        existingEntity.setInternalnotesStatus("In Review");
                    }

                    existingEntity.setModifiedDateTime(LocalDateTime.now());

                    if (internalNotesDTO.getInternalNoteDocument() != null && internalNotesDTO.getInternalNoteDocument().length > 0) {
                        existingEntity.setInternalNoteDocument(internalNotesDTO.getInternalNoteDocument());
                        existingEntity.setInternalNoteDocumentName(internalNotesDTO.getInternalNoteDocumentName());
                        existingEntity.setInternalNoteDocumentType(internalNotesDTO.getInternalNoteDocumentType());
                    }

                    if (reviewerStatusChanged) {
                        InternalNoteHistory history = new InternalNoteHistory();
                        history.setCreatedDateTime(LocalDateTime.now());
                        history.setCretaedByEmailId(empMap.get(modifiedEmailId));
                        history.setDescription("Updated the internalnote reviewer status \n" +
                                originalReviewerStatus + " 🡺 " + internalNotesDTO.getReviewerStatus());
                        history.setInternalNotes(existingEntity);
                        historyList.add(history);
                    }
                    
                    if (approverStatusChanged) {
                        InternalNoteHistory history = new InternalNoteHistory();
                        history.setCreatedDateTime(LocalDateTime.now());
                        history.setCretaedByEmailId(empMap.get(modifiedEmailId));
                        history.setDescription("Updated the internalnote approver status \n" +
                                originalApproverStatus + " 🡺 " + internalNotesDTO.getApproverStatus());
                        history.setInternalNotes(existingEntity);
                        historyList.add(history);
                    }
                    
                    if (statusChanged) {
                        InternalNoteHistory history = new InternalNoteHistory();
                        history.setCreatedDateTime(LocalDateTime.now());
                        history.setCretaedByEmailId(empMap.get(modifiedEmailId));
                        history.setDescription("Updated the internalnote status \n" +
                                originalStatus + " 🡺 " + internalNotesDTO.getInternalnotesStatus());
                        history.setInternalNotes(existingEntity);
                        historyList.add(history);
                    }
                    
                    if (titleChanged) {
                        InternalNoteHistory history = new InternalNoteHistory();
                        history.setCreatedDateTime(LocalDateTime.now());
                        history.setCretaedByEmailId(empMap.get(modifiedEmailId));
                        history.setDescription("Updated the internalnote title status \n" +
                                originalTitle + " 🡺 " + internalNotesDTO.getInternalnoteTitle());
                        history.setInternalNotes(existingEntity);
                        historyList.add(history);
                    }
                    
                    if (approverChanged) {
                        InternalNoteHistory history = new InternalNoteHistory();
                        history.setCreatedDateTime(LocalDateTime.now());
                        history.setCretaedByEmailId(empMap.get(modifiedEmailId));
                        String originalApproverName = empMap.getOrDefault(originalApprover, originalApprover);
                        String updatedApproverName = empMap.getOrDefault(internalNotesDTO.getInternalnoteApprover(), internalNotesDTO.getInternalnoteApprover());
                        history.setDescription("Updated the internalnote approver \n" +
                        		originalApproverName+ " 🡺 " + updatedApproverName );
                        history.setInternalNotes(existingEntity);
                        historyList.add(history);
                    }
                    
                    if (reviewerChanged) {
                        InternalNoteHistory history = new InternalNoteHistory();
                        history.setCreatedDateTime(LocalDateTime.now());
                        history.setCretaedByEmailId(empMap.get(modifiedEmailId));
                        String originalReviewerName = empMap.getOrDefault(originalReviewer, originalReviewer);
                        String updatedReviewerName = empMap.getOrDefault(internalNotesDTO.getInternalnoteReviewer(), internalNotesDTO.getInternalnoteReviewer());
                        history.setDescription("Updated the internalnote reviewer \n" +
                        		originalReviewerName + " 🡺 " + updatedReviewerName);
                        history.setInternalNotes(existingEntity);
                        historyList.add(history);
                    }

                    existingEntity.setInternalNoteHistory(historyList);
                    List<InternalNoteCommentDto> commentDtoList = internalNotesDTO.getInternalNoteCommentDto();
                    if (commentDtoList != null && !commentDtoList.isEmpty()) {
                        List<InternalNoteComment> commentEntities = commentDtoList.stream().map(commentDto -> {
                            InternalNoteComment comment = new InternalNoteComment();
                            comment.setCommentorName(commentDto.getCommentorName());
                            comment.setCommentDescription(commentDto.getCommentDescription());
                            comment.setCreatedBy(commentDto.getCreatedBy());
                            comment.setModifiedBy(commentDto.getModifiedBy());
                            comment.setCreatedByEmailId(commentDto.getCreatedByEmailId());
                            comment.setModifiedByEmailId(commentDto.getModifiedByEmailId());
                            comment.setCreatedDateTime(commentDto.getCreatedDateTime());
                            comment.setModifiedDateTime(commentDto.getModifiedDateTime());
                            comment.setNotes(existingEntity);  // This is critical for JPA mapping
                            return comment;
                        }).collect(Collectors.toList());

                        existingEntity.setInternalNoteComment(commentEntities);
                    }

                    InternalNotes updatedEntity = internalNotesRepository.save(existingEntity);
                    log.info("Internal note updated successfully with ID: {}", updatedEntity.getInternalnoteId());

                    return modelMapper.map(updatedEntity, InternalNotesDTO.class);
                })
                .orElseThrow(() -> {
                    log.error("Error: Internal note with ID {} not found!", internalNotesDTO.getInternalnoteId());
                    return new BusinessException(
                            ErrorCodeMessages.INTERNAL_NOTES_NOT_FOUND_CODE,
                            "Internal note not found with ID: " + internalNotesDTO.getInternalnoteId());
                });

        } catch (Exception e) {
            log.error("Unexpected error occurred while updating internal note.", e);
            throw new BusinessException(
                    ErrorCodeMessages.INTERNAL_NOTES_UPDATE_ERROR_CODE,
                    ErrorCodeMessages.INTERNAL_NOTES_UPDATE_ERROR_MSG);
        }
    }



	@Override
	public boolean deleteInternalNotes(Long internalNoteId) {
        log.info("In InternalNotesServiceImpl, deleteInternalNotes(): Deleting internal note with ID: {}", internalNoteId);
        try {
            if (!internalNotesRepository.existsById(internalNoteId)) {
                log.error("Error: Internal note with ID {} not found!", internalNoteId);
                throw new BusinessException(ErrorCodeMessages.INTERNAL_NOTES_NOT_FOUND_CODE, 
                		ErrorCodeMessages.INTERNAL_NOTES_NOT_FOUND_MSG);
            }
            internalNotesRepository.deleteById(internalNoteId);
            log.info("Internal note deleted successfully with ID: {}", internalNoteId);
            return true;
        } catch (Exception e) {
            log.error("Unexpected error occurred while deleting internal note.", e);
            throw new BusinessException(ErrorCodeMessages.INTERNAL_NOTES_DELETE_ERROR_CODE,
                    ErrorCodeMessages.INTERNAL_NOTES_DELETE_ERROR_MSG);
        }
	}

	@Override
	public boolean deleteSelectedInternalNotesByIds(List<Long> ids) {
       
		try {
            internalNotesRepository.deleteAllById(ids);
            return true;
        } catch (Exception e) {
            return false;
        }
	}

	@Override
	public InternalNotesDTO getInternalNotesById(Long id) {

       log.info("In InternalNotesServiceImpl, getInternalNotesById(): Fetching internal note with ID: {}", id);
        try {
            return internalNotesRepository.findById(id)
                .map(entity -> {
                    log.info("Internal note retrieved successfully with ID: {}", id);
                    return modelMapper.map(entity, InternalNotesDTO.class);
                }).orElseThrow(() -> {
                    log.error("Error: Internal note with ID {} not found!", id);
                    return new BusinessException(ErrorCodeMessages.INTERNAL_NOTES_NOT_FOUND_CODE, 
                    		ErrorCodeMessages.INTERNAL_NOTES_NOT_FOUND_MSG);
                });
        } catch (Exception e) {
            log.error("Unexpected error occurred while fetching internal note.", e);
            throw new BusinessException(ErrorCodeMessages.INTERNAL_NOTES_FETCH_ERROR_CODE,
                    ErrorCodeMessages.INTERNAL_NOTES_FETCH_ERROR_MSG);
        }	
		
	}
	
	
	private Map<Long, String> getAllDepartmentsMap() {
		log.info("getAllDepartments() entered");
		ResponseEntity<List<DepartmentDto>> deptListResp = restTemplate.exchange("http://UMS-DEPARTMENT-SERVICE/departments/all", HttpMethod.GET, null, new ParameterizedTypeReference<List<DepartmentDto>>() {
		});
		List<DepartmentDto> deptList = deptListResp.getBody();
		Map<Long, String> deptMap = new LinkedHashMap<>();
		deptList.forEach(dept -> {
			deptMap.put(dept.getDepartmentId(), dept.getDepartmentName());
		});
		log.info("getAllDepartments() executed successfully.");
		return deptMap;
	}
    private Map<Long,String> getAllTeamsMap(){
    	log.info("getAllTeamsMap() entered");
    	ResponseEntity<List<TeamDto>> teamListResp = restTemplate.exchange("http://UMS-DEPARTMENT-SERVICE/teams/all", HttpMethod.GET, null, new ParameterizedTypeReference<List<TeamDto>>() {
		});
    	List<TeamDto> teamList = teamListResp.getBody();
    	Map<Long, String> teamMap = new LinkedHashMap<>();
    	teamList.forEach(team ->{
    		teamMap.put(team.getTeamId(), team.getTeamName());
    	});
    	log.info("getAllTeamsMap() executed successfully.");
    	return teamMap;
    }
	private Map<String, String> getEmployeesMap(){
		log.info("getEmployeesMap() entered");
		ResponseEntity<List<EmployeeDto>> empListResp = restTemplate.exchange("http://UMS-EMPLOYEE-SERVICE/employees/all", HttpMethod.GET, null, new ParameterizedTypeReference<List<EmployeeDto>>() {
		});
		List<EmployeeDto> empList = empListResp.getBody();
		Map<String, String> empMap = new LinkedHashMap<>();
		empList.forEach(emp -> {
			empMap.put(emp.getEmail(), emp.getFirstName()+" "+emp.getLastName());
		});
		log.info("getEmployeesMap() executed successfully.");
		return empMap;
	}

	@Override
	public List<InternalNotesDTO> getAllInternalNotes() {
	    log.info("In InternalNotesServiceImpl, getAllInternalNotes(): Fetching all internal notes...");
	    
	    try {
	        List<InternalNotes> notesList = internalNotesRepository.findAll();
	        Map<String, String> empMap = getEmployeesMap();
	        Map<Long, String> teamMap = getAllTeamsMap();
	        Map<Long, String> departmentsMap = getAllDepartmentsMap();
	        
	        log.info("Total internal notes retrieved: {}", notesList.size());
	        ModelMapper localMapper = new ModelMapper();
		    localMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		    localMapper.typeMap(InternalNotes.class, InternalNotesDTO.class)
		    .addMappings(m -> {
		        m.skip(InternalNotesDTO::setInternalNoteDocument);
		        m.skip(InternalNotesDTO::setInternalNoteCommentDto);
		        m.skip(InternalNotesDTO::setInternalNoteHistoryDto);
		    });
	        return notesList.stream().map(note -> {
	            InternalNotesDTO dto = localMapper.map(note, InternalNotesDTO.class);
	            dto.setApproverName(empMap.get(dto.getInternalnoteApprover()));
	            dto.setReviewerName(empMap.get(dto.getInternalnoteReviewer()));
	            dto.setTeamName(teamMap.get(dto.getTeamId()));
	            dto.setDepartmentName(departmentsMap.get(dto.getDepartmentId()));
	            
	            if ("Reviewed - Approved".equals(dto.getReviewerStatus()) && "Awaiting Approval".equals(dto.getApproverStatus())) {
	                dto.setInternalnotesStatus("Under Approval");
	            } else if ("Reviewed - Approved".equals(dto.getReviewerStatus()) && "Approved".equals(dto.getApproverStatus())) {
	                dto.setInternalnotesStatus("Approved");
	            }else if ("Reviewed - Changes Requested".equals(dto.getReviewerStatus()) && "Awaiting Approval".equals(dto.getApproverStatus())) {
	                dto.setInternalnotesStatus("Changes Required");
	            }else if ("Reviewed - Approved".equals(dto.getReviewerStatus()) && "Rejected".equals(dto.getApproverStatus())) {
	                dto.setInternalnotesStatus("Changes Required");
	            }else {
	                dto.setInternalnotesStatus("In Review");
	            }   

	            return dto;
	        }).collect(Collectors.toList());

	    } catch (Exception e) {
	        log.error("Unexpected error occurred while fetching all internal notes.", e);
	        throw new BusinessException(
	            ErrorCodeMessages.INTERNAL_NOTES_FETCH_ALL_ERROR_CODE,
	            ErrorCodeMessages.INTERNAL_NOTES_FETCH_ALL_ERROR_MSG
	        );
	    }
	}
	
	public byte[] getDocumentById(Long internalnoteId) {
		  InternalNotes note = internalNotesRepository.findById(internalnoteId)
	                .orElseThrow(() -> new RuntimeException("Internal Note not found"));

	        return note.getInternalNoteDocument();
		
	}
	
	public String getInternalNoteDocumentName(Long internalnoteId) {
        InternalNotes note = internalNotesRepository.findById(internalnoteId)
                .orElseThrow(() -> new RuntimeException("Internal Note not found"));
        log.info("the document name is "+ note.getInternalNoteDocumentName());
        return note.getInternalNoteDocumentName();
    }

    public String getInternalNoteDocumentType(Long internalnoteId) {
        InternalNotes note = internalNotesRepository.findById(internalnoteId)
                .orElseThrow(() -> new RuntimeException("Internal Note not found"));

        return note.getInternalNoteDocumentType();
    }
	


}
