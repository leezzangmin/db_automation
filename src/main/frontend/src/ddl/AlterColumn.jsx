import React, { useState, useEffect } from 'react';

const AlterColumn = () => {
    const [response, setResponse] = useState(null);
    const [selectedDBMS, setSelectedDBMS] = useState('');
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('');
    const [targetColumnName, setTargetColumnName] = useState('target_column_name');
    const [afterColumn, setAfterColumn] = useState({
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

    const handleAlterColumn = async () => {
        const url = `/ddl/column?databaseName=${selectedDBMS}`;
        const requestBody = {
            commandType: 'ALTER_COLUMN',
            schemaName: selectedSchema,
            tableName: selectedTable,
            targetColumnName: targetColumnName,
            afterColumn: afterColumn,
        };
        console.log('Request:', requestBody);
        try {
            const response = await fetch(url, {
                method: 'PATCH',
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

    const handleAfterColumnNameChange = (e) => {
        const value = e.target.value;
        setAfterColumn((prevColumn) => ({ ...prevColumn, name: value }));
    };

    const handleAfterColumnTypeChange = (e) => {
        const value = e.target.value;
        setAfterColumn((prevColumn) => ({ ...prevColumn, type: value }));
    };

    const handleAfterColumnIsNullChange = (e) => {
        const value = e.target.value === 'true';
        setAfterColumn((prevColumn) => ({ ...prevColumn, isNull: value }));
    };

    const handleAfterColumnDefaultValueChange = (e) => {
        const value = e.target.value;
        setAfterColumn((prevColumn) => ({ ...prevColumn, defaultValue: value }));
    };

    const handleAfterColumnIsUniqueChange = (e) => {
        const value = e.target.value === 'true';
        setAfterColumn((prevColumn) => ({ ...prevColumn, isUnique: value }));
    };

    const handleAfterColumnIsAutoIncrementChange = (e) => {
        const value = e.target.value === 'true';
        setAfterColumn((prevColumn) => ({ ...prevColumn, isAutoIncrement: value }));
    };

    const handleAfterColumnCommentChange = (e) => {
        const value = e.target.value;
        setAfterColumn((prevColumn) => ({ ...prevColumn, comment: value }));
    };

    const handleAfterColumnCharsetChange = (e) => {
        const value = e.target.value;
        setAfterColumn((prevColumn) => ({ ...prevColumn, charset: value }));
    };

    const handleAfterColumnCollateChange = (e) => {
        const value = e.target.value;
        setAfterColumn((prevColumn) => ({ ...prevColumn, collate: value }));
    };

    const handleTargetColumnNameChange = (e) => {
        const value = e.target.value;
        setTargetColumnName(value);
        setAfterColumn((prevColumn) => ({ ...prevColumn, name: value }));
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

            <label>Target Column Name:</label>
            <input
                type="text"
                value={targetColumnName}
                onChange={handleTargetColumnNameChange}
            />

            <label>After Column Type:</label>
            <input
                type="text"
                value={afterColumn.type}
                onChange={handleAfterColumnTypeChange}
            />

            <label>After Column Is Null:</label>
            <select value={afterColumn.isNull} onChange={handleAfterColumnIsNullChange}>
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>After Column Default Value:</label>
            <input
                type="text"
                value={afterColumn.defaultValue}
                onChange={handleAfterColumnDefaultValueChange}
            />

            <label>After Column Is Unique:</label>
            <select value={afterColumn.isUnique}
                    onChange={handleAfterColumnIsUniqueChange}>
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>After Column Is Auto Increment:</label>
            <select
                value={afterColumn.isAutoIncrement}
                onChange={handleAfterColumnIsAutoIncrementChange}
            >
                <option value={true}>True</option>
                <option value={false}>False</option>
            </select>

            <label>After Column Comment:</label>
            <input
                type="text"
                value={afterColumn.comment}
                onChange={handleAfterColumnCommentChange}
            />

            <label>After Column Charset:</label>
            <input
                type="text"
                value={afterColumn.charset}
                onChange={handleAfterColumnCharsetChange}
            />

            <label>After Column Collate:</label>
            <input
                type="text"
                value={afterColumn.collate}
                onChange={handleAfterColumnCollateChange}
            />

            <button onClick={handleAlterColumn}>Alter Column</button>
            {response && <p>{JSON.stringify(response)}</p>}
        </div>
    );
};

export default AlterColumn;
