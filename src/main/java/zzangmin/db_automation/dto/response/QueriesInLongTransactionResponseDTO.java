package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import zzangmin.db_automation.entity.mysqlobject.Query;

import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class QueriesInLongTransactionResponseDTO {

    private String databaseName;
    private Long historyListLength;
    private List<Query> queries;
}
