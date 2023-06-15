package zzangmin.db_automation.parser;

import org.springframework.stereotype.Component;
import zzangmin.db_automation.dto.request.*;
import zzangmin.db_automation.dto.response.AddColumnResponseDTO;
import zzangmin.db_automation.entity.Column;
import zzangmin.db_automation.entity.CommandType;
import zzangmin.db_automation.entity.Constraint;

import java.util.List;

@Component
public class DDLParser {

    public String commandToSql(DDLRequestDTO ddlRequestDTO) {
        if (ddlRequestDTO.getCommandType().equals(CommandType.ADD_COLUMN)) {
            return addColumnCommandToSql((AddColumnRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.ALTER_COLUMN)) {

        } else if (ddlRequestDTO.getCommandType().equals(CommandType.CREATE_INDEX)) {
            return createIndexCommandToSql((CreateIndexRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.CREATE_TABLE)) {
            return createTableCommandToSql((CreateTableRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.DELETE_COLUMN)) {
            return deleteColumnCommandToSql((DeleteColumnRequestDTO) ddlRequestDTO);
        } else if (ddlRequestDTO.getCommandType().equals(CommandType.EXTEND_VARCHAR_COLUMN)) {
            return extendVarcharColumnCommandToSql((ExtendVarcharColumnRequestDTO) ddlRequestDTO);
        }

        throw new IllegalArgumentException("존재하지 않는 명령입니다.");
    }

    private String deleteColumnCommandToSql(DeleteColumnRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` DROP COLUMN `");
        sb.append(dto.getColumnName());
        sb.append("`");
        return sb.toString();
    }

    private String extendVarcharColumnCommandToSql(ExtendVarcharColumnRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` MODIFY COLUMN ");
        sb.append(dto.getColumn().getName());
        sb.append(" ");
        sb.append(dto.getColumn().getType());
        sb.append(" ");
        sb.append(dto.getColumn().generateNull());
        sb.append(" ");
        sb.append(dto.getColumn().generateUnique());
        sb.append(" ");
        sb.append(dto.getColumn().generateAutoIncrement());
        sb.append(" COMMENT '");
        sb.append(dto.getColumn().getComment());
        sb.append("'");
        return sb.toString();
    }

    private String addColumnCommandToSql(AddColumnRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` ADD COLUMN `");
        sb.append(dto.getColumn().getName());
        sb.append("` ");
        sb.append(dto.getColumn().getType());
        sb.append(" ");
        sb.append(dto.getColumn().generateNull());
        sb.append(" ");
        sb.append(dto.getColumn().generateUnique());
        sb.append(" ");
        sb.append(dto.getColumn().generateAutoIncrement());
        sb.append(" COMMENT '");
        sb.append(dto.getColumn().getComment());
        sb.append("'");
        return sb.toString();
    }

    private String createIndexCommandToSql(CreateIndexRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE INDEX ");
        sb.append(dto.getIndexName());
        sb.append(" on `");
        sb.append(dto.getSchemaName());
        sb.append("`.`");
        sb.append(dto.getTableName());
        sb.append("` (");
        for (String columnName : dto.getColumnNames()) {
            sb.append("`");
            sb.append(columnName);
            sb.append("`, ");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")\n");
        return sb.toString();
    }

    private String createTableCommandToSql(CreateTableRequestDTO dto) {
        StringBuilder sb = new StringBuilder();

        List<Column> columns = dto.getColumns();
        List<Constraint> constraints = dto.getConstraints();
        sb.append(generateCreateTableStatement(dto.getSchemaName(), dto.getTableName()));
        sb.append(generateColumnStatement(columns));
        sb.append(generateConstraintStatement(constraints));
        sb.append(generateCreateTableOptions(dto.getEngine(), dto.getCharset(), dto.getCollate(), dto.getTableComment()));
        return sb.toString();
    }

    private String generateCreateTableStatement(String schemaName, String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `");
        sb.append(schemaName);
        sb.append("`.`");
        sb.append(tableName);
        sb.append("` (\n");
        return sb.toString();
    }
    private String generateColumnStatement(List<Column> columns) {
        StringBuilder sb = new StringBuilder();
        for (Column column : columns) {
            sb.append("\t`");
            sb.append(column.getName());
            sb.append("` ");
            sb.append(column.getType());
            sb.append(" ");
            sb.append(column.generateNull());
            sb.append(" ");
            sb.append(column.generateUnique());
            sb.append(" ");
            sb.append(column.generateAutoIncrement());
            sb.append(" COMMENT '");
            sb.append(column.getComment());
            sb.append("'");
            sb.append(",\n");
        }
        return sb.toString();
    }

    private String generateConstraintStatement(List<Constraint> constraints) {
        StringBuilder sb = new StringBuilder();
        for (Constraint constraint : constraints) {
            sb.append("\t");
            sb.append(constraint.getType());
            if (!constraint.getType().equals("PRIMARY KEY")) {
                sb.append(" ");
                sb.append(constraint.getKeyName());
            }
            sb.append(" (");
            for (String keyName : constraint.getKeyColumnNames()) {
                sb.append("`");
                sb.append(keyName);
                sb.append("`,");
            }
            // 마지막 쉼표 제거
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("),\n");
        }
        // 마지막 쉼표 제거
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    private String generateCreateTableOptions(String engine, String charset, String collate, String comment) {
        StringBuilder sb = new StringBuilder();
        sb.append(") ENGINE=");
        sb.append(engine);
        sb.append(" DEFAULT CHARSET=");
        sb.append(charset);
        sb.append(" COLLATE=");
        sb.append(collate);
        sb.append(" COMMENT='");
        sb.append(comment);
        sb.append("';");
        return sb.toString();
    }

    /**
     * ALTER TABLE [db명].[table명] ADD COLUMN asdf VARCHAR(123) NOT NULL
     *
     * Finished DDL execution. duration : 0.03 sec.
     * SQL: CREATE TABLE `wholesale`.`consignment_order_items` (
     *                                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
     *                                            `consignment_order_id` bigint(20) NOT NULL COMMENT '위탁판매 주문ID',
     *                                            `order_item_number` varchar(255) NOT NULL COMMENT '주문아이템번호',
     *                                            `shop_id` bigint(20) NOT NULL COMMENT '샵ID',
     *                                            `consignment_product_item_id` bigint(20) DEFAULT NULL COMMENT '위탁판매 상품아이템 ID',
     *                                            `consignment_origin_product_item_id` bigint(20) NOT NULL COMMENT '위탁판매 상품아이템 도매상품 ID',
     *                                            `quantity` int(11) NOT NULL COMMENT '수량',
     *                                            `cancelled_reason` varchar(255) DEFAULT NULL COMMENT '취소 사유',
     *                                            `date_cancelled` datetime(3) DEFAULT NULL COMMENT '취소일자',
     *                                            `date_created` datetime(3) NOT NULL COMMENT '생성일자',
     *                                            `date_updated` datetime(3) NOT NULL COMMENT '수정일자',
     *                                            `cancelled_reason_category` varchar(255) DEFAULT NULL COMMENT '취소 사유 카테고리',
     *                                            `type` varchar(255) NOT NULL COMMENT '위탁판매 주문 타입',
     *                                            PRIMARY KEY (`id`),
     *                                            UNIQUE KEY `id` (`id`),
     *                                            UNIQUE KEY `consignment_order_id_order_item_number` (`consignment_order_id`,`order_item_number`),
     *                                            KEY `consignment_order_id` (`consignment_order_id`)
     * ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='위탁판매 주문아이템'
     *
     *Finished DDL execution. duration : 100.44 sec.
     * SQL: ALTER TABLE fulfillment.wms_product_item_stocks ADD COLUMN sent_at datetime(6) NULL COMMENT '발송예정일';
     *
     * Finished DDL execution. duration : 19.55 sec.
     * SQL: alter table ux.paid_order_item_logs
     *     add shop_biz_department varchar(64) null comment '샵 biz department (shop meta attribute 값)';
     *
     *Starting DDL execution.
     * SQL: ALTER TABLE fulfillment.wms_order_items ADD COLUMN additional_product_item_id bigint comment '추가상품 ID';
     *
     *
     *     Finished DDL execution. duration : 0.07 sec.
     * SQL: ALTER TABLE `user_account_coupon`.`coupon_policy_confirms` ADD COLUMN `title` VARCHAR(255) NOT NULL COMMENT '쿠폰 타이틀';
     *
     *Finished DDL execution. duration : 35.66 sec.
     * SQL: ALTER TABLE fulfillment.wms_product_items ADD COLUMN additional_product_name varchar(255) comment '추가상품명';
     *
     * Starting DDL execution.
     * SQL: create index base_content_id
     *     on fbk.base_content_medias (base_content_id);
     *
     *
     * Finished DDL execution. duration : 0.03 sec.
     * SQL: ALTER TABLE `ux`.`beauty_frequency_coupon_event_logs`
     * ADD UNIQUE INDEX user_account_id_coupon_policy_id (user_account_id, coupon_policy_id);
     *
     * Starting DDL execution.
     * SQL: ALTER TABLE `promotion`.`first_order_conditions`
     *     ADD INDEX promotion_id(promotion_id);
     *
     *
     * Starting DDL execution.
     * SQL: ALTER TABLE `promotion`.`promotions`
     *     ADD INDEX promotion_type_date_promotion_end (promotion_type, date_promotion_end);
     *
     *     Execute DDL immediately.
     * SQL: `create table fbk.base_contents
     * (
     *     id           BIGINT not null auto_increment primary key comment 'id',
     *     title_backoffice varchar(128) not null comment '백오피스 타이틀',
     *     title        varchar(255) null comment '앱 노출 타이틀',
     *     text_content text not null comment '본문',
     *     content_order int          not null comment '순서',
     *     date_started datetime     not null comment '노출 시작',
     *     date_ended   datetime     not null comment '노출 종료',
     *     landing_url  varchar(512) not null comment '랜딩 url',
     *     doer         varchar(255) not null comment '수정자',
     *     is_active    tinyint(1)   not null comment '활성화 여부',
     *     date_created datetime     not null comment '생성일',
     *     date_updated datetime     not null comment '수정일'
     * ) comment 'base 컨텐츠';`
     */



}
