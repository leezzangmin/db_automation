package zzangmin.db_automation.dto;

import zzangmin.db_automation.entity.CommandType;

// 창의적인 SQL 방지 필요
public record DDLRequestDTO(CommandType commandType, String commandSQL, String clusterIdentifier) {
}
