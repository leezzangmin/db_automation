import React, { useState, useEffect } from 'react';

const AddColumn = () => {
    const [response, setResponse] = useState(null);
    const [selectedDBMS, setSelectedDBMS] = useState('');
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('');
    const [column, setColumn] = useState({
        name: 'column_name',
        type: 'TEXT',
        isNull: true,
        defaultValue: '',
        isUnique: false,
        isAutoIncrement: false,
        comment: 'column comment required',
        charset: 'utf8mb4',
        collate: 'utf8mb4_0900_ai_ci',
    });
    const [dbmsNames, setDBMSNames] = useState([]);
    const [schemaNames, setSchemaNames] = useState([]);
    const [tableNames, setTableNames] = useState([]);

    const handleAddColumn = async () => {
        const url = `/ddl/column?databaseName=${selectedDBMS}`;
        const requestBody = {
            commandType: 'ADD_COLUMN',
            schemaName: selectedSchema,
            tableName: selectedTable,
            column,
        };

        console.log('Request:', requestBody);
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

    const fetchDBMSNames = async () => {
        try {
            const url = '/describe/dbmsNames';
            const response = await fetch(url);
            if (response.ok) {
                const data = await response.json();
                setDBMSNames(data.dbmsNames);
            } else {
                console.error('Failed to fetch dbms names:', response.status);
            }
        } catch (error) {
            console.error('Failed to fetch dbms names:', error);
        }
    };

    const fetchSchemas = async (dbmsName) => {
        try {
            const url = `/describe/dbms/schemaNames?databaseName=${dbmsName}`;
            const response = await fetch(url);
            if (response.ok) {
                const data = await response.json();
                setSchemaNames(data.schemaNames);
            } else {
                console.error('Failed to fetch schema names:', response.status);
            }
        } catch (error) {
            console.error('Failed to fetch schema names:', error);
        }
    };

    const fetchTables = async (dbmsName, schemaName) => {
        try {
            const url = `/describe/dbms/schemas?databaseName=${dbmsName}&schemaName=${schemaName}`;
            const response = await fetch(url);
            if (response.ok) {
                const data = await response.json();
                const schemaData = data.find((item) => item.schemaName === schemaName);
                if (schemaData) {
                    const tableNames = schemaData.tableInfos.map((table) => table.tableName);
                    setTableNames(tableNames);
                }
            } else {
                console.error('Failed to fetch tables:', response.status);
            }
        } catch (error) {
            console.error('Failed to fetch tables:', error);
        }
    };

    const handleDBMSChange = (e) => {
        const selectedDBMS = e.target.value;
        setSelectedDBMS(selectedDBMS);
        setSelectedSchema('');
        setTableNames([]);

        if (selectedDBMS) {
            fetchSchemas(selectedDBMS);
        } else {
            setSchemaNames([]);
        }
    };

    const handleSchemaChange = (e) => {
        const selectedSchema = e.target.value;
        setSelectedSchema(selectedSchema);
        setTableNames([]);

        if (selectedDBMS && selectedSchema) {
            fetchTables(selectedDBMS, selectedSchema);
        }
    };

    const handleColumnNameChange = (e) => {
        const value = e.target.value;
        setColumn((prevColumn) => ({ ...prevColumn, name: value }));
    };

    const handleColumnTypeChange = (e) => {
        const value = e.target.value;
        setColumn((prevColumn) => ({ ...prevColumn, type: value }));
    };

    const handleColumnIsNullChange = (e) => {
        const value = e.target.value === 'true';
        setColumn((prevColumn) => ({ ...prevColumn, isNull: value }));
    };

    const handleColumnDefaultValueChange = (e) => {
        const value = e.target.value;
        setColumn((prevColumn) => ({ ...prevColumn, defaultValue: value }));
    };

    const handleColumnIsUniqueChange = (e) => {
        const value = e.target.value === 'true';
        setColumn((prevColumn) => ({ ...prevColumn, isUnique: value }));
    };

    const handleColumnIsAutoIncrementChange = (e) => {
        const value = e.target.value === 'true';
        setColumn((prevColumn) => ({ ...prevColumn, isAutoIncrement: value }));
    };

    const handleColumnCommentChange = (e) => {
        const value = e.target.value;
        setColumn((prevColumn) => ({ ...prevColumn, comment: value }));
    };

    const handleColumnCharsetChange = (e) => {
        const value = e.target.value;
        setColumn((prevColumn) => ({ ...prevColumn, charset: value }));
    };

    const handleColumnCollateChange = (e) => {
        const value = e.target.value;
        setColumn((prevColumn) => ({ ...prevColumn, collate: value }));
    };

    useEffect(() => {
        fetchDBMSNames();
    }, []);

    return (
        <div>
            <label>Select DBMS:</label>
            <select value={selectedDBMS} onChange={handleDBMSChange}>
                <option value="">Select a DBMS</option>
                {dbmsNames.map((name, index) => (
                    <option key={index} value={name}>
                        {name}
                    </option>
                ))}
            </select>

            <label>Select Schema:</label>
            <select value={selectedSchema} onChange={handleSchemaChange}>
                <option value="">Select a schema</option>
                {schemaNames.map((name, index) => (
                    <option key={index} value={name}>
                        {name}
                    </option>
                ))}
            </select>

            <label>Select Table:</label>
            <select
                value={selectedTable}
                onChange={(e) => setSelectedTable(e.target.value)}
            >
                <option value="">Select a table</option>
                {tableNames.map((name, index) => (
                    <option key={index} value={name}>
                        {name}
                    </option>
                ))}
            </select>

            <label>Column Name:</label>
            <input
                type="text"
                value={column.name}
                onChange={handleColumnNameChange}
            />

            <label>Column Type:</label>
            <input
                type="text"
                value={column.type}
                onChange={handleColumnTypeChange}
            />

            <label>Is Null:</label>
            <select value={column.isNull} onChange={handleColumnIsNullChange}>
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>Default Value:</label>
            <input
                type="text"
                value={column.defaultValue}
                onChange={handleColumnDefaultValueChange}
            />

            <label>Is Unique:</label>
            <select value={column.isUnique} onChange={handleColumnIsUniqueChange}>
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>Is Auto Increment:</label>
            <select
                value={column.isAutoIncrement}
                onChange={handleColumnIsAutoIncrementChange}
            >
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>Comment:</label>
            <input
                type="text"
                value={column.comment}
                onChange={handleColumnCommentChange}
            />

            <label>Charset:</label>
            <input
                type="text"
                value={column.charset}
                onChange={handleColumnCharsetChange}
            />

            <label>Collate:</label>
            <input
                type="text"
                value={column.collate}
                onChange={handleColumnCollateChange}
            />

            <button onClick={handleAddColumn}>Add Column</button>
            {response && <p>{JSON.stringify(response)}</p>}
        </div>
    );
};

export default AddColumn;
