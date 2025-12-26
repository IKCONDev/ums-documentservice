package com.ikn.ums.service;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.ikn.ums.dto.InternalNotesDTO;

public interface InternalNotesService {

	InternalNotesDTO saveInternalNotes(InternalNotesDTO internalNotesDTO);
	InternalNotesDTO updateInternalNotes(InternalNotesDTO internalNotesDTO);
	boolean deleteInternalNotes(Long internalNoteId);
	boolean deleteSelectedInternalNotesByIds(List<Long> ids);
	InternalNotesDTO getInternalNotesById(Long id);
	List<InternalNotesDTO> getAllInternalNotes();
	String getInternalNoteDocumentType(Long internalnoteId);
	String getInternalNoteDocumentName(Long internalnoteId);
	byte[] getDocumentById(Long internalnoteId);

}
