import React, { useState, useEffect } from 'react';

const TableStatusInfo = () => {
    const [data, setData] = useState(null);
    const [selectedDBMS, setSelectedDBMS] = useState('');
    const [dbmsNames, setDBMSNames] = useState([]);
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('');
    const [schemaNames, setSchemaNames] = useState([]);
    const [tableNames, setTableNames] = useState([]);

    const fetchTableStatusInfo = async () => {
        try {
            const url = `/describe/table/status?databaseName=${selectedDBMS}&schemaName=${selectedSchema}&tableName=${selectedTable}`;
            const response = await fetch(url);
            const data = await response.json();
            setData(data);
        } catch (error) {
            console.error('Error fetching data:', error);
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
        setSelectedDBMS(e.target.value); // Update the state variable directly
        setSelectedSchema('');
        setSelectedTable('');
        setTableNames([]);

        if (e.target.value) {
            fetchSchemas(e.target.value); // Use e.target.value directly
        } else {
            setSchemaNames([]);
        }
    };


    const handleSchemaChange = (e) => {
        const selectedSchema = e.target.value;
        setSelectedSchema(selectedSchema);
        setSelectedTable('');

        if (selectedDBMS && selectedSchema) {
            fetchTables(selectedDBMS, selectedSchema);
        }
    };

    const handleTableChange = (e) => {
        const selectedTable = e.target.value;
        setSelectedTable(selectedTable);
        if (selectedTable) {
            fetchTableStatusInfo();
        }
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
            <select value={selectedTable} onChange={handleTableChange}>
                <option value="">Select a table</option>
                {tableNames.map((name, index) => (
                    <option key={index} value={name}>
                        {name}
                    </option>
                ))}
            </select>

            <h2>Table Status Information</h2>

            <div>
                {data && (
                    <>
                        <h3>Database Identifier: {data.databaseIdentifier}</h3>
                        <h3>Schema Name: {data.schemaName}</h3>
                        <h3>Table Name: {data.tableName}</h3>
                        <h3>Columns:</h3>
                        <ul>
                            {data.columns &&
                                data.columns.map((column) => (
                                    <li key={column.name}>
                                        {column.name}: {column.type}
                                    </li>
                                ))}
                        </ul>
                        <h3>Change Histories:</h3>
                        <ul>
                            {data.changeHistories &&
                                data.changeHistories.map((history) => (
                                    <li key={history.id}>
                                        CommandType: {history.commandType}
                                        <br />
                                        Doer: {history.doer}
                                        <br />
                                        Do DateTime: {history.doDateTime}
                                        <br />
                                        Change Content: {history.changeContent}
                                    </li>
                                ))}
                        </ul>
                    </>
                )}
            </div>

        </div>
    );
};

export default TableStatusInfo;
