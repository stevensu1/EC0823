package org.example.demo01;

import lombok.Data;


@Data
@SQLParameterSqlStr(name = "insert into my_record(id,name,value) values(?,?,?)")
public class MyRecordOne implements MyRecord {
    @SQLParameter(index = 1)
    private Integer id;
    @SQLParameter(index = 2)
    private String name;
    @SQLParameter(index = 3)
    private int value;


    public MyRecordOne(Integer id, String name, int value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }
}