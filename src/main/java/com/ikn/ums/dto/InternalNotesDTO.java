package com.ikn.ums.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class InternalNotesDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long internalnoteId;
	private String internalnoteName;
	private String internalnoteTitle;
	private String internalnoteReviewer;
	private String internalnoteApprover;
	private String reviewerStatus;
	private String approverStatus;
	private String internalnotesStatus;
	private LocalDateTime reviewerDateTime;
	private LocalDateTime approverDateTime;
	private Long meetingId;
	private Long teamId;
	private Long departmentId;
	private String departmentName;
	private String teamName;
	private String approverName;
	private String reviewerName;
	private LocalDateTime createdDateTime;
	private LocalDateTime modifiedDateTime;
	private String createdBy;
	private String modifiedBy;
	private String createdByEmailId;
	private String modifiedByEmailId;
	private byte[] internalNoteDocument;
	private String internalNoteDocumentName;
	private String internalNoteDocumentType;
	private List<InternalNoteHistoryDto> internalNoteHistoryDto;
	private List<InternalNoteCommentDto> internalNoteCommentDto;


}
