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
import lombok.Data;

@Entity
@Table(name = "internal_note_history_tab")
@Data
public class InternalNoteHistory {

    @Id
    @SequenceGenerator(
            name = "intr_note_hist_gen",
            sequenceName = "intr_note_hist_gen",
            allocationSize = 1
    )
    @GeneratedValue(generator = "intr_note_hist_gen")
    @Column(name = "id")
    private Long id;

    @Column(name = "created_by_email_id")
    private String cretaedByEmailId;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "modified_by_user")
    private String modifiedByUser;

    @Column(name = "description")
    private String description;

    @Column(name = "modified_by_email_id")
    private String modifiedByEmailId;

    @Column(name = "modified_date_time")
    private LocalDateTime modifiedDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internal_note_id")
    private InternalNotes internalNotes;
}
