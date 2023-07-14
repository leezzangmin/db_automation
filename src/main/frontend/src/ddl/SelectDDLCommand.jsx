import React, { useState } from 'react';
import AddColumn from "./AddColumn";
import CreateIndex from "./CreateIndex";
import CreateTable from "./CreateTable";
import DeleteColumn from "./DeleteColumn";
import AlterColumn from "./AlterColumn";
import RenameColumn from "./RenameColumn";
import ExtendVarchar from "./ExtendVarchar";

const SelectDDLCommand = () => {
    const [selectedCommand, setSelectedCommand] = useState('');

    const handleChangeCommand = (e) => {
        setSelectedCommand(e.target.value);
    };

    return (
        <div>
            <label>Select Command Type:</label>
            <select value={selectedCommand} onChange={handleChangeCommand}>
                <option value="">Select a command type</option>
                <option value="add_column">Add Column</option>
                <option value="alter_column">Alter Column</option>
                <option value="create_index">Create Index</option>
                <option value="create_table">Create Table</option>
                <option value="delete_column">Delete Column</option>
                <option value="extend_varchar_column">Extend Varchar Column</option>
                <option value="rename_column">Rename Column</option>
            </select>

            {selectedCommand === 'add_column' && (
                <div>
                    <AddColumn />
                </div>
            )}

            {selectedCommand === 'alter_column' && (
                <div>
                    <AlterColumn />
                </div>
            )}

            {selectedCommand === 'create_index' && (
                <div>
                    <CreateIndex />
                </div>
            )}

            {selectedCommand === 'create_table' && (
                <div>
                    <CreateTable />
                </div>
            )}

            {selectedCommand === 'delete_column' && (
                <div>
                    <DeleteColumn />
                </div>
            )}

            {selectedCommand === 'extend_varchar_column' && (
                <div>
                    <ExtendVarchar />
                </div>
            )}

            {selectedCommand === 'rename_column' && (
                <div>
                    <RenameColumn />
                </div>
            )}
        </div>
    );
};

export default SelectDDLCommand;
