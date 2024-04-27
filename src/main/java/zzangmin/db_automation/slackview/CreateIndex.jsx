import React, {useEffect, useState} from 'react';

const CreateIndex = () => {
    const [response, setResponse] = useState(null);
    const [tableSchema, setTableSchema] = useState(null);
    const [selectedDBMS, setSelectedDBMS] = useState('');
    const [dbmsNames, setDBMSNames] = useState([]);
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('');
    const [indexName, setIndexName] = useState('');
    const [indexType, setIndexType] = useState('KEY'); // Initial index type set to "KEY"
    const [columnNames, setColumnNames] = useState(['']);
    const [schemaNames, setSchemaNames] = useState([]);
    const [tableNames, setTableNames] = useState([]);

    const handleCreateIndex = async () => {
        const url = `/ddl/index?databaseName=${selectedDBMS}`;
        const requestBody = {
            commandType: "CREATE_INDEX",
            schemaName: selectedSchema,
            tableName: selectedTable,
            indexName: indexName,
            indexType: indexType,
            columnNames: columnNames.filter(name => name !== ''),
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

    useEffect(() => {
        fetchDBMSNames();
    }, []);



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