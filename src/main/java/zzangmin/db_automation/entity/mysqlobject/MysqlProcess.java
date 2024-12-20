package zzangmin.db_automation.entity.mysqlobject;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class MysqlProcess {

    private long id;
    private String user;
    private String host;
    private String db;
    private String command;
    private long time;
    private String state;
    private String info;

    public MysqlProcess(long id, String user, String host, String db, String command, long time, String state, String info) {
        this.id = id;
        this.user = user;
        this.host = host;
        this.db = db;
        this.command = command;
        this.time = time;
        this.state = state;
        this.info = info;
    }
}
