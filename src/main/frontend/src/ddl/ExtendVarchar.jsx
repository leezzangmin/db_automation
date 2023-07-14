import React, { useState, useEffect } from 'react';

const ExtendVarchar = () => {
    const [response, setResponse] = useState(null);
    const [tableSchema, setTableSchema] = useState(null);
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('');
    const [targetColumnName, setTargetColumnName] = useState('target_column_name');
    const [extendSize, setExtendSize] = useState('255');
    const [selectedDBMS, setSelectedDBMS] = useState('');
    const [dbmsNames, setDBMSNames] = useState([]);
    const [schemaNames, setSchemaNames] = useState([]);
    const [tableNames, setTableNames] = useState([]);

    const handleExtendColumn = async () => {
        const url = `/ddl/varchar?databaseName=${selectedDBMS}`;
        const requestBody = {
            commandType: 'EXTEND_VARCHAR_COLUMN',
            schemaName: selectedSchema,
            tableName: selectedTable,
            targetColumnName: targetColumnName,
            extendSize: extendSize,
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

    const fetchTableSchema = async (selectedDBMS, selectedSchema, selectedTable) => {
        try {
            const url = `/describe/table?databaseName=${selectedDBMS}&schemaName=${selectedSchema}&tableName=${selectedTable}`;
            const response = await fetch(url);
            if (response.ok) {
                const data = await response.text();
                setTableSchema(data);
            } else {
                console.error('Failed to fetch table schema:', response.status);
            }
        } catch (error) {
            console.error('Failed to fetch table schema:', error);
        }
    }

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

    const handleTableChange = (e) => {
        const selectedTable = e.target.value;
        setSelectedTable(selectedTable);

        if (selectedDBMS && selectedSchema && selectedTable) {
            fetchTableSchema(selectedDBMS, selectedSchema, selectedTable);
        }
    };


    const handleTargetColumnNameChange = (e) => {
        const value = e.target.value;
        setTargetColumnName(value);
    };
    const handleExtendSizeChange = (e) => {
        const value = e.target.value;
        setExtendSize(value);
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
                value={selectedTable} onChange={handleTableChange}>
                <option value="">Select a table</option>
                {tableNames.map((name, index) => (
                    <option key={index} value={name}>
                        {name}
                    </option>
                ))}
            </select>
            {tableSchema && <p>{tableSchema}</p>}

            <label>Target Column Name:</label>
            <input
                type="text"
                value={targetColumnName}
                onChange={handleTargetColumnNameChange}
            />

            <label>Extend Size:</label>
            <input
                type="text"
                value={extendSize}
                onChange={handleExtendSizeChange}
            />

            <button onClick={handleExtendColumn}>Extend Column</button>
            {response && <p>{JSON.stringify(response)}</p>}
        </div>
    );
};

export default ExtendVarchar;
