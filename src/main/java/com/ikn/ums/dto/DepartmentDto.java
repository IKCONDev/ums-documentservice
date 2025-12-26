package com.ikn.ums.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDto {

	private Long departmentId;
	private String departmentName;
	private String departmentAddress;
	private String departmentCode;
}
