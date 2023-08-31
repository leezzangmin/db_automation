package zzangmin.db_automation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import zzangmin.db_automation.entity.Query;

import java.util.List;

@ToString
@Getter
@AllArgsConstructor
public class QueriesInLongTransactionResponseDTO {

    private String databaseName;
    private long historyListLength;
    private List<Query> queries;
}
