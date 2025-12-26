package com.ikn.ums.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ikn.ums.dto.InternalNotesDTO;
import com.ikn.ums.entity.InternalNotes;
import com.ikn.ums.exception.ControllerException;
import com.ikn.ums.exception.EmptyInputException;
import com.ikn.ums.exception.EntityNotFoundException;
import com.ikn.ums.exception.ErrorCodeMessages;
import com.ikn.ums.exception.FileProcessingException;
import com.ikn.ums.repository.InternalNotesRepository;
import com.ikn.ums.service.InternalNotesService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/internalnotes")
public class InternalNotesController {

    @Autowired
    private InternalNotesService internalNotesService;
    
    @Autowired
    private InternalNotesRepository internalNotesRepository;
    
    @PostMapping("/save")
	public ResponseEntity<InternalNotesDTO> saveInternalNotes(@RequestPart("note") InternalNotesDTO internalNotesDTO,
			@RequestPart("internalNoteDocument") MultipartFile internalNoteDocument) {
		if (internalNoteDocument == null || internalNotesDTO == null) {
			log.info("saveInternalNotes() - internalNotesDTO or internalNoteDocument is null");
			throw new EntityNotFoundException(ErrorCodeMessages.INTERNAL_NOTES_OBJECT_IS_EMPTY_CODE,
					ErrorCodeMessages.INTERNAL_NOTES_OBJECT_IS_EMPTY_MSG);
		}

		try {
			log.info("saveInternalNotes() is under execution...");
			internalNotesDTO.setInternalNoteDocument(internalNoteDocument.getBytes());
			internalNotesDTO.setInternalNoteDocumentName(internalNoteDocument.getOriginalFilename());
			internalNotesDTO.setInternalNoteDocumentType(internalNoteDocument.getContentType());
			log.info("saveInternalNotes() executed successfully");
			return ResponseEntity.ok(internalNotesService.saveInternalNotes(internalNotesDTO));
		}catch (Exception e) {
				log.error("saveInternalNotes() : Exception Occured !" + e.getMessage(),e);
				throw new ControllerException(ErrorCodeMessages.INTERNAL_NOTES_SAVE_ERROR_CODE,
						ErrorCodeMessages.INTERNAL_NOTES_SAVE_ERROR_MSG);
			}
	}

    
    @PutMapping("/update")
    public ResponseEntity<InternalNotesDTO> updateInternalNotes(
            @RequestPart("UpdatedNote") InternalNotesDTO internalNotesDTO,
            @RequestPart(value = "internalNoteUpdatedDocument", required = false) MultipartFile internalNoteDocument) {

    	if (internalNoteDocument == null || internalNotesDTO == null) {
			log.info("saveInternalNotes() - internalNotesDTO or internalNoteDocument is null");
			throw new EntityNotFoundException(ErrorCodeMessages.INTERNAL_NOTES_OBJECT_IS_EMPTY_CODE,
					ErrorCodeMessages.INTERNAL_NOTES_OBJECT_IS_EMPTY_MSG);
		}
        try {
            if (internalNoteDocument != null && !internalNoteDocument.isEmpty()) {
                log.info("updateInternalNotes() - New document uploaded: {}", internalNoteDocument.getOriginalFilename());
                internalNotesDTO.setInternalNoteDocument(internalNoteDocument.getBytes());
                internalNotesDTO.setInternalNoteDocumentName(internalNoteDocument.getOriginalFilename());
                internalNotesDTO.setInternalNoteDocumentType(internalNoteDocument.getContentType());
            } else {
                log.info("updateInternalNotes() - No new document uploaded. Retrieving existing document details for ID: {}", internalNotesDTO.getInternalnoteId());
                Optional<InternalNotes> existingOpt = internalNotesRepository.findById(internalNotesDTO.getInternalnoteId());
                if (existingOpt.isPresent()) {
                    InternalNotes existing = existingOpt.get();
                    internalNotesDTO.setInternalNoteDocument(existing.getInternalNoteDocument());
                    internalNotesDTO.setInternalNoteDocumentName(existing.getInternalNoteDocumentName());
                    internalNotesDTO.setInternalNoteDocumentType(existing.getInternalNoteDocumentType());
                }else {
                    log.error("updateInternalNotes() - Internal note not found with ID: {}", internalNotesDTO.getInternalnoteId());
                    throw new EntityNotFoundException(
                            ErrorCodeMessages.INTERNAL_NOTES_NOT_FOUND_CODE,
                            ErrorCodeMessages.INTERNAL_NOTES_NOT_FOUND_MSG
                    );
                }
            }
            InternalNotesDTO updatedDto = internalNotesService.updateInternalNotes(internalNotesDTO);
            if (updatedDto != null) {
                log.info("updateInternalNotes() - Successfully updated internal note with ID: {}", updatedDto.getInternalnoteId());
                return ResponseEntity.ok(updatedDto);
            } else {
                log.warn("updateInternalNotes() - Update failed or note not found.");
                return ResponseEntity.notFound().build();
            }

        } catch (IOException e) {
            log.error("updateInternalNotes() - Failed to read uploaded file: {}", e.getMessage(), e);
            throw new FileProcessingException("Failed to read uploaded file", e);
        }
    }


    
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteInternalNotes(@PathVariable Long id) {
    	if( id == null || id <= 0 ) {
			log.info("deleteInternalNotes() EntityNotFoundException : internal note id is null ");
			throw new EmptyInputException(ErrorCodeMessages.ERR_INTERNAL_NOTES_ID_IS_EMPTY_CODE,
					ErrorCodeMessages.ERR_INTERNAL_NOTES_ID_IS_EMPTY_MSG);
		
		}
    	try {
		log.info("deleteInternalNotes() is under execution...");
		log.info("deleteInternalNotes() executed successfully");
        return internalNotesService.deleteInternalNotes(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    	}catch (Exception e) {
			log.error("deleteInternalNotes() is exited with exception :"+ e.getMessage(),e);
			throw new ControllerException(ErrorCodeMessages.INTERNAL_NOTES_DELETE_ERROR_CODE,
					ErrorCodeMessages.INTERNAL_NOTES_DELETE_ERROR_MSG);
		}
    }
    
    @DeleteMapping("/deleteSelected")
	public ResponseEntity<Void> deleteSelectedInternalNotes(@RequestBody List<Long> ids) {
		if (ids == null || ids.size() <= 0) {
			log.info("deleteSelectedInternalNotes() EntityNotFoundException : internal note id is null ");
			throw new EmptyInputException(ErrorCodeMessages.ERR_INTERNAL_NOTES_ID_IS_EMPTY_CODE,
					ErrorCodeMessages.ERR_INTERNAL_NOTES_ID_IS_EMPTY_MSG);

		}
		try {
			return internalNotesService.deleteSelectedInternalNotesByIds(ids) ? ResponseEntity.ok().build()
					: ResponseEntity.badRequest().build();
		} catch (Exception e) {
			log.error("deleteInternalNotes() is exited with exception :" + e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.INTERNAL_NOTES_DELETE_ERROR_CODE,
					ErrorCodeMessages.INTERNAL_NOTES_DELETE_ERROR_MSG);
		}
	}
    
	@GetMapping("/get/{id}")
	public ResponseEntity<InternalNotesDTO> getInternalNotesById(@PathVariable Long id) {
		if (id == null || id <= 0) {
			log.info("getInternalNotesById() EntityNotFoundException : internal note id is null ");
			throw new EmptyInputException(ErrorCodeMessages.ERR_INTERNAL_NOTES_ID_IS_EMPTY_CODE,
					ErrorCodeMessages.ERR_INTERNAL_NOTES_ID_IS_EMPTY_MSG);

		}
		try {
			log.info("getInternalNotesById() is under execution...");
			InternalNotesDTO dto = internalNotesService.getInternalNotesById(id);
			log.info("getInternalNotesById() executed successfully");
			return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
		} catch (Exception e) {
			log.error("getInternalNotesById() is exited with exception :" + e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.INTERNAL_NOTES_FETCH_ERROR_CODE,
					ErrorCodeMessages.INTERNAL_NOTES_FETCH_ERROR_MSG);
		}
	}
    
    @GetMapping("/all")
    public ResponseEntity<List<InternalNotesDTO>> getAllInternalNotes() {
		log.info("getAllInternalNotes() is under execution...");
		log.info("getAllInternalNotes() executed successfully");
        return ResponseEntity.ok(internalNotesService.getAllInternalNotes());
    }
    
    @GetMapping("/document/{id}")
    public ResponseEntity<byte[]> getInternalNoteDocument(@PathVariable Long id) {

        byte[] document = internalNotesService.getDocumentById(id);
        String fileName = internalNotesService.getInternalNoteDocumentName(id);
        String fileType = internalNotesService.getInternalNoteDocumentType(id);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .header("file-name", fileName)
                .header("Access-Control-Expose-Headers", "file-name, Content-Disposition")
                .header("Content-Type", fileType)
                .body(document);
    }
	
}
