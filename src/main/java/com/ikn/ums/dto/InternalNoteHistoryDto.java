package com.ikn.ums.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalNoteHistoryDto implements Serializable  {
	
	private static final long serialVersionUID = 1L;

	private Long id;

	private String cretaedByEmailId;

	private LocalDateTime createdDateTime;

	private String modifiedByUser;

	private String description;

	private String modifiedByEmailId;

	private LocalDateTime modifiedDateTime;

}
