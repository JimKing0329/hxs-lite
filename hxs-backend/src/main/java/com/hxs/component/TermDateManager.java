package com.hxs.component;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 学期日期管理组件
 */
@Data
@Component
@Slf4j
@RequiredArgsConstructor
public class TermDateManager {
    private Integer year;
    private Integer term;
    private LocalDate termStartDate;
}
