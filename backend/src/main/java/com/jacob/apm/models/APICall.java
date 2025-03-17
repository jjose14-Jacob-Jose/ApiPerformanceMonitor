package com.jacob.apm.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@Document(collection = "api_calls")
public class APICall {

//    Following arguments represent the columns on the table 'api_calls'.
//    Data type of the arguments will be same as that in the table.
    @Id
    private String callId; //
    private String callerMessage;
    private String callerName;
    private String callerTimestampUTC;

    public String toLogString() {
        return "APICall{" +
                "callId='" + callId + '\'' +
                ", callerName='" + callerName + '\'' +
                '}';
    }
}
