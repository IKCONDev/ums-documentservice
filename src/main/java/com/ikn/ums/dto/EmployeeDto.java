package com.ikn.ums.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto {

	private Integer id;
	private String employeeOrgId;
	//required for batch processing of teams meetings
	private String teamsUserId;
	private String firstName;
	private String lastName;
	private String email;
	private String reportingManager;
	private String designation;
	private DesignationDto empDesignation;
	private TeamDto team;
	private Long departmentId;
	private String gender;
	private DepartmentDto department;
	private String dateOfJoining;
	private String employeeStatus;
}
