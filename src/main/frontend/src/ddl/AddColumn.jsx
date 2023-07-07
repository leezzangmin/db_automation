import React, { useState } from 'react';
import FetchSchemaAndTable from './FetchSchemaAndTable';

const AddColumn = () => {
    const [response, setResponse] = useState(null);
    const [selectedCluster, setSelectedCluster] = useState('');
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('');
    const [columnName, setColumnName] = useState('');
    const [columnType, setColumnType] = useState('');
    const [isNull, setIsNull] = useState('');
    const [defaultValue, setDefaultValue] = useState('');
    const [isUnique, setIsUnique] = useState('');
    const [isAutoIncrement, setIsAutoIncrement] = useState('');
    const [comment, setComment] = useState('');
    const [charset, setCharset] = useState('utf8mb4');
    const [collate, setCollate] = useState('utf8mb4_0900_ai_ci');

    const handleAddColumn = async () => {
        console.log('selectedCluster: ', selectedCluster);
        console.log('selectedSchema: ', selectedSchema);
        console.log('selectedTable: ', selectedTable);
        console.log('columnName: ', columnName);
        console.log('columnType: ', columnType);
        console.log('isNull: ', isNull);
        console.log('defaultValue: ', defaultValue);
        console.log('isUnique: ', isUnique);
        console.log('isAutoIncrement: ', isAutoIncrement);
        console.log('comment: ', comment);
        console.log('charset: ', charset);
        console.log('collate: ', collate);

        const url = `/ddl/column?databaseName=${selectedCluster}`;
        const requestBody = {
            commandType: 'ADD_COLUMN',
            schemaName: selectedSchema,
            tableName: selectedTable,
            column: {
                name: columnName,
                type: columnType,
                isNull,
                defaultValue,
                isUnique,
                isAutoIncrement,
                comment,
                charset,
                collate,
            },
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

    return (
        <div>
            <FetchSchemaAndTable
                onSelectClusterSchemaTable={({ selectedCluster, selectedSchema, selectedTable }) => {
                    setSelectedCluster(selectedCluster);
                    setSelectedSchema(selectedSchema);
                    setSelectedTable(selectedTable);
                }}
            />

            <label>Column Name:</label>
            <input
                type="text"
                value={columnName}
                onChange={(e) => setColumnName(e.target.value)}
            />

            <label>Column Type:</label>
            <input
                type="text"
                value={columnType}
                onChange={(e) => setColumnType(e.target.value)}
            />

            <label>Is Null:</label>
            <select value={isNull} onChange={(e) => setIsNull(e.target.value === 'true')}>
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>Default Value:</label>
            <input
                type="text"
                value={defaultValue}
                onChange={(e) => setDefaultValue(e.target.value)}
            />

            <label>Is Unique:</label>
            <select value={isUnique} onChange={(e) => setIsUnique(e.target.value === 'true')}>
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>Is Auto Increment:</label>
            <select
                value={isAutoIncrement}
                onChange={(e) => setIsAutoIncrement(e.target.value === 'true')}
            >
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>Comment:</label>
            <input
                type="text"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
            />

            <label>Charset:</label>
            <input
                type="text"
                value={charset}
                onChange={(e) => setCharset(e.target.value)}
            />

            <label>Collate:</label>
            <input
                type="text"
                value={collate}
                onChange={(e) => setCollate(e.target.value)}
            />

            <button onClick={handleAddColumn}>Add Column</button>

            {response && (
                <div>
                    <p>Response:</p>
                    <pre>{JSON.stringify(response, null, 2)}</pre>
                </div>
            )}
        </div>
    );
};

export default AddColumn;
