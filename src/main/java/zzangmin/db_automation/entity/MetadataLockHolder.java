package zzangmin.db_automation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MetadataLockHolder {
    private String objectType;
    private String objectSchema;
    private String objectName;
    private String lockType;
    private String lockStatus;
    private long threadId;
    private long processlistId;
    private String processListInfo;
    private long processlistTime;
}
