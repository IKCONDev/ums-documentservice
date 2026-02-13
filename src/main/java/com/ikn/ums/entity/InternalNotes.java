package com.ikn.ums.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "internal_notes")
public class InternalNotes {

    @Id
    @Column(name = "internal_note_id")
    @SequenceGenerator(
            name = "intr_note_gen",
            sequenceName = "intr_note_gen",
            allocationSize = 1
    )
    @GeneratedValue(generator = "intr_note_gen")
    private Long internalnoteId;

    @Column(name = "internal_note_name", nullable = false)
    private String internalnoteName;

    @Column(name = "internal_note_title", nullable = false)
    private String internalnoteTitle;

    @Column(name = "internal_note_reviewer", unique = false)
    private String internalnoteReviewer;

    @Column(name = "internal_note_approver", nullable = true)
    private String internalnoteApprover;

    @Column(name = "reviewer_status", nullable = true)
    private String reviewerStatus;

    @Column(name = "approver_status", nullable = true)
    private String approverStatus;

    @Column(name = "internal_notes_status", nullable = true)
    private String internalnotesStatus;

    @Column(name = "reviewer_date_time")
    private LocalDateTime reviewerDateTime;

    @Column(name = "approver_date_time")
    private LocalDateTime approverDateTime;

    @Column(name = "meeting_id")
    private Long meetingId;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "modified_date_time", nullable = true)
    private LocalDateTime modifiedDateTime;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_by_email_id")
    private String createdByEmailId;

    @Column(name = "modified_by_email_id", nullable = true)
    private String modifiedByEmailId;

    @Column(name = "internal_note_document")
    private byte[] internalNoteDocument;

    @Column(name = "internal_note_document_name")
    private String internalNoteDocumentName;

    @Column(name = "internal_note_document_type")
    private String internalNoteDocumentType;

    @OneToMany(mappedBy = "internalNotes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InternalNoteHistory> internalNoteHistory;

    @OneToMany(mappedBy = "notes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InternalNoteComment> internalNoteComment;
}
