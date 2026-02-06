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
