package com.project.kore.builder.impl;

import com.project.kore.builder.SlotBuilder;
import com.project.kore.model.Slot;
import com.project.kore.model.User;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Builder concreto per Slot. In build pretende professional, startTime ed endTime non nulli e che
 * startTime preceda endTime.
 */
public class SlotBuilderImpl implements SlotBuilder {
    private Long id;
    private User professional;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private User bookedBy;
    private Integer version;
    private LocalDateTime bookedAt;

    @Override
    public SlotBuilder id(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public SlotBuilder professional(User professional) {
        this.professional = professional;
        return this;
    }

    @Override
    public SlotBuilder startTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    @Override
    public SlotBuilder endTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public SlotBuilder bookedBy(User bookedBy) {
        this.bookedBy = bookedBy;
        return this;
    }

    @Override
    public SlotBuilder version(Integer version) {
        this.version = version;
        return this;
    }

    @Override
    public SlotBuilder bookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
        return this;
    }

    @Override
    public Slot build() {
        Objects.requireNonNull(this.professional, "professional è obbligatorio");
        Objects.requireNonNull(this.startTime, "startTime è obbligatorio");
        Objects.requireNonNull(this.endTime, "endTime è obbligatorio");

        if (!this.startTime.isBefore(this.endTime))
            throw new IllegalArgumentException("startTime deve essere precedente a endTime");

    Slot obj = new Slot();
        obj.setId(this.id);
        obj.setProfessional(this.professional);
        obj.setStartTime(this.startTime);
        obj.setEndTime(this.endTime);
        obj.setBookedBy(this.bookedBy);
        obj.setVersion(this.version);
        if (this.bookedAt != null) obj.setBookedAt(this.bookedAt);
        return obj;
    }
}
