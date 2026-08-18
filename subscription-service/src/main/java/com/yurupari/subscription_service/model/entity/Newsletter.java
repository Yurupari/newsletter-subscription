package com.yurupari.subscription_service.model.entity;

import com.yurupari.common_data.model.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "newsletter")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamicUpdate
public class Newsletter extends BaseEntity {

    private String title;

    private String description;

    private Boolean isActive;
}
