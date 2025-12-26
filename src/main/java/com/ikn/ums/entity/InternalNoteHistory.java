package com.ikn.ums.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "internalNoteHistory_tab")
@Data
public class InternalNoteHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "cretaedByEmailId")
	private String cretaedByEmailId;

	@Column(name = "createdDateTime")
	private LocalDateTime createdDateTime;

	@Column(name = "modifiedByUser")
	private String modifiedByUser;

	@Column(name = "description")
	private String description;

	@Column(name = "modifiedByEmailId")
	private String modifiedByEmailId;

	@Column(name = "modifiedDateTime")
	private LocalDateTime modifiedDateTime;
	
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internalnote_id") 
    private InternalNotes internalNotes;

}
