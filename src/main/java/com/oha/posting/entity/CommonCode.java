package com.oha.posting.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_post_common_codes")
public class CommonCode {

    @Id
    private String code;
    private String codeName;
    private String type;
}
