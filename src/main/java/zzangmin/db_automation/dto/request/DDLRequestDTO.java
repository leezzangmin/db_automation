package zzangmin.db_automation.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import zzangmin.db_automation.entity.CommandType;


@ToString
@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "commandType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddColumnRequestDTO.class, name = "ADD_COLUMN"),
        @JsonSubTypes.Type(value = AlterColumnRequestDTO.class, name = "ALTER_COLUMN"),
        @JsonSubTypes.Type(value = CreateIndexRequestDTO.class, name = "CREATE_INDEX"),
        @JsonSubTypes.Type(value = CreateTableRequestDTO.class, name = "CREATE_TABLE"),
        @JsonSubTypes.Type(value = DeleteColumnRequestDTO.class, name = "DELETE_COLUMN"),
        @JsonSubTypes.Type(value = ExtendVarcharColumnRequestDTO.class, name = "EXTEND_VARCHAR_COLUMN"),
        @JsonSubTypes.Type(value = RenameColumnRequestDTO.class, name = "RENAME_COLUMN"),
        @JsonSubTypes.Type(value = RenameIndexRequestDTO.class, name = "RENAME_INDEX"),
        @JsonSubTypes.Type(value = AlterColumnCommentRequestDTO.class, name = "ALTER_COLUMN_COMMENT"),
        @JsonSubTypes.Type(value = AlterTableCommentRequestDTO.class, name = "ALTER_TABLE_COMMENT"),
})
public abstract class DDLRequestDTO {
    @NotBlank
    private CommandType commandType;
}
