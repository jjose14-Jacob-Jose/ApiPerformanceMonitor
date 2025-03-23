package com.stc.apm.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestForDateRange {
    private String dateTimeRangeStartString;
    private String dateTimeRangeEndString;
}
