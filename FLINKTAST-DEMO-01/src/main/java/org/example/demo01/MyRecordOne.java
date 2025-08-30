package org.example.demo01;

import lombok.Data;
import org.example.demo01.sinkBD.SQLParameter;
import org.example.demo01.sinkBD.SQLParameterSqlStr;


@Data
@SQLParameterSqlStr(name = "insert into my_record(id,name,value) values(?,?,?)")
public class MyRecordOne implements MyRecord {
    @SQLParameter(index = 1)
    private Integer id;
    @SQLParameter(index = 2)
    private String name;
    @SQLParameter(index = 3)
    private String value;


    public MyRecordOne(Integer id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }
}