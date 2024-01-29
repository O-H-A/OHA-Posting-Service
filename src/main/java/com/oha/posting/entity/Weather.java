package com.oha.posting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_post_weathers")
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weatherId;

    @JoinColumn(name = "weather_code")
    @ManyToOne(fetch = FetchType.LAZY)
    private CommonCode weatherCommonCode;

    private Long userId;

    private Integer dayParts;

    private Date weatherDt;

    private Long hcode;
}
