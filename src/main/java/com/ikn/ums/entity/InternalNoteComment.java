package com.ikn.ums.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "internal_notes_comments_tab")
public class InternalNoteComment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "commentId")
	private Long id;
	
	@Column(name = "commentorName")
	private String commentorName;
	
	@Column(name = "commentDescription")
	private String commentDescription;
	
	@Column(name = "createdBy")
	private String createdBy;
	
	@Column(name = "modifiedBy")
	private String modifiedBy;
	
	@Column(name = "createdByEmailId")
	private String createdByEmailId;
	
	@Column(name = "modifiedByEmailId")
	private String modifiedByEmailId;
	
	@Column(name = "createdDateTime")
	private LocalDateTime createdDateTime;
	
	@Column(name = "modifiedDateTime")
	private LocalDateTime modifiedDateTime;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internalnote_id") 
    private InternalNotes notes;
}
