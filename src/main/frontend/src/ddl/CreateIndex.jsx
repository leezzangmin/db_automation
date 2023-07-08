import React, { useState } from 'react';
import FetchSchemaAndTable from './FetchSchemaAndTable';

const CreateIndex = () => {
    const [response, setResponse] = useState(null);
    const [selectedCluster, setSelectedCluster] = useState('');
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('');
    const [indexName, setIndexName] = useState('');
    const [indexType, setIndexType] = useState('KEY'); // Initial index type set to "KEY"
    const [columnNames, setColumnNames] = useState(['']);

    const handleCreateIndex = async () => {
        const url = `/ddl/index?databaseName=${selectedCluster}`;
        const requestBody = {
            commandType: "CREATE_INDEX",
            schemaName: selectedSchema,
            tableName: selectedTable,
            indexName: indexName,
            indexType: indexType,
            columnNames: columnNames.filter(name => name !== ''),
        };

        try {
            const response = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestBody),
            });

            if (response.ok) {
                const data = await response.json();
                setResponse(data);
            } else {
                console.error('Request failed:', response.status);
            }
        } catch (error) {
            console.error('Request failed:', error);
        }
    };

    const handleColumnNameChange = (index, value) => {
        const updatedColumnNames = [...columnNames];
        updatedColumnNames[index] = value;
        setColumnNames(updatedColumnNames);
    };

    const handleAddColumnName = () => {
        setColumnNames([...columnNames, '']);
    };

    const handleRemoveColumnName = (index) => {
        const updatedColumnNames = [...columnNames];
        updatedColumnNames.splice(index, 1);
        setColumnNames(updatedColumnNames);
    };

    return (
        <div>
            <FetchSchemaAndTable
                onSelectClusterSchemaTable={({ selectedCluster, selectedSchema, selectedTable }) => {
                    setSelectedCluster(selectedCluster);
                    setSelectedSchema(selectedSchema);
                    setSelectedTable(selectedTable);
                }}
            />

            <label>Index Name:</label>
            <input
                type="text"
                value={indexName}
                onChange={(e) => setIndexName(e.target.value)}
            />

            <label>Index Type:</label>
            <select
                value={indexType}
                onChange={(e) => setIndexType(e.target.value)}
            >
                <option value="KEY">KEY</option>
                <option value="UNIQUE KEY">UNIQUE KEY</option>
            </select>

            <label>Column Names:</label>
            {columnNames.map((name, index) => (
                <div key={index}>
                    <input
                        type="text"
                        value={name}
                        onChange={(e) => handleColumnNameChange(index, e.target.value)}
                    />
                    <button onClick={() => handleRemoveColumnName(index)}>Remove</button>
                </div>
            ))}
            <button onClick={handleAddColumnName}>Add Column</button>

            <button onClick={handleCreateIndex}>Create Index</button>

            {response && (
                <div>
                    <p>Response:</p>
                    <pre>{JSON.stringify(response, null, 2)}</pre>
                </div>
            )}
        </div>
    );
};

export default CreateIndex;
//