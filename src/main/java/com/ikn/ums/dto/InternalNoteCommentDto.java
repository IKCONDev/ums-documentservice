package com.ikn.ums.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalNoteCommentDto implements Serializable{

	private static final long serialVersionUID = 1L;
	private Long id;
	private String commentorName;
	private String commentatorName;
	private String commentDescription;
	private String createdBy;
	private String modifiedBy;
	private String createdByEmailId;
	private String modifiedByEmailId;
	private LocalDateTime createdDateTime;
	private LocalDateTime modifiedDateTime;
	private Long internalNoteId;
}
