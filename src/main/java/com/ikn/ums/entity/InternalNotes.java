package com.ikn.ums.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.ikn.ums.dto.InternalNoteCommentDto;

import lombok.Data;

@Data
@Entity
@Table (name = "internal_notes")
public class InternalNotes {
	
	@Id
	@Column(name = "internalnote_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long internalnoteId;

	@Column(name = "internalnote_name", nullable = false)
	private String internalnoteName;

	@Column(name = "internalnote_title", nullable = false)
	private String internalnoteTitle;
	
	@Column(name = "internalnote_reviewer", unique = false)
	private String internalnoteReviewer;

	@Column(name = "internalnote_approver", nullable = true)
	private String internalnoteApprover;
	
	@Column(name = "reviewer_status", nullable = true)
	private String reviewerStatus;
	
	@Column(name = "approver_status", nullable = true)
	private String approverStatus;

	@Column(name = "internalnotes_status", nullable = true)
	private String internalnotesStatus;
	
	@Column(name = "reviewerDateTime")
	private LocalDateTime reviewerDateTime;
	
	@Column(name = "approverDateTime")
	private LocalDateTime approverDateTime;
	
	@Column(name = "meetingId")
	private Long meetingId;
	
	@Column(name = "teamId")
	private Long teamId;

	@Column(name = "departmentId")
	private Long departmentId;
	
	@Column(name = "createdDateTime")
	private LocalDateTime createdDateTime;

	@Column(name = "modifiedDateTime", nullable = true)
	private LocalDateTime modifiedDateTime;

	@Column(name = "createdBy")
	private String createdBy;

	@Column(name = "modifiedBy")
	private String modifiedBy;

	@Column(name = "createdByEmailId")
	private String createdByEmailId;

	@Column(name = "modifiedByEmailId", nullable = true)
	private String modifiedByEmailId;
	
	@Column(name="internalNoteDocument")
	private byte[] internalNoteDocument;
	
	@Column(name="internalNoteDocumentName")
	private String internalNoteDocumentName;
	
	@Column(name="internalNoteDocumentType")
	private String internalNoteDocumentType;

	@OneToMany(mappedBy = "internalNotes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<InternalNoteHistory> internalNoteHistory;
	
	@OneToMany(mappedBy = "notes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<InternalNoteComment> internalNoteComment;


}
