package com.ikn.ums.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
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
    @SequenceGenerator(
            name = "intr_note_comm_gen",
            sequenceName = "intr_note_comm_gen",
            allocationSize = 1
    )
    @GeneratedValue(generator = "intr_note_comm_gen")
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "commentor_name")
    private String commentorName;

    @Column(name = "comment_description")
    private String commentDescription;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_by_email_id")
    private String createdByEmailId;

    @Column(name = "modified_by_email_id")
    private String modifiedByEmailId;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "modified_date_time")
    private LocalDateTime modifiedDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_note_id")
    private InternalNotes notes;
}
