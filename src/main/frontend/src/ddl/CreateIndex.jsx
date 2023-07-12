import React, {useEffect, useState} from 'react';

const CreateIndex = () => {
    const [response, setResponse] = useState(null);
    const [selectedCluster, setSelectedCluster] = useState('');
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('');
    const [indexName, setIndexName] = useState('');
    const [indexType, setIndexType] = useState('KEY'); // Initial index type set to "KEY"
    const [columnNames, setColumnNames] = useState(['']);
    const [clusterNames, setClusterNames] = useState([]);
    const [schemaNames, setSchemaNames] = useState([]);
    const [tableNames, setTableNames] = useState([]);

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

    useEffect(() => {
        fetchClusterNames();
    }, []);


    const fetchClusterNames = async () => {
        try {
            const url = '/describe/clusterNames';
            const response = await fetch(url);
            if (response.ok) {
                const data = await response.json();
                setClusterNames(data.clusterNames);
            } else {
                console.error('Failed to fetch cluster names:', response.status);
            }
        } catch (error) {
            console.error('Failed to fetch cluster names:', error);
        }
    };

    const fetchSchemas = async (clusterName) => {
        try {
            const url = `/describe/cluster/schemaNames?databaseName=${clusterName}`;
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

    const fetchTables = async (clusterName, schemaName) => {
        try {
            const url = `/describe/cluster/schemas?databaseName=${clusterName}&schemaName=${schemaName}`;
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

    const handleClusterChange = (e) => {
        const selectedCluster = e.target.value;
        setSelectedCluster(selectedCluster);
        setSelectedSchema('');
        setTableNames([]);

        if (selectedCluster) {
            fetchSchemas(selectedCluster);
        } else {
            setSchemaNames([]);
        }
    };

    const handleSchemaChange = (e) => {
        const selectedSchema = e.target.value;
        setSelectedSchema(selectedSchema);
        setTableNames([]);

        if (selectedCluster && selectedSchema) {
            fetchTables(selectedCluster, selectedSchema);
        }
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
            <label>Select Cluster:</label>
            <select value={selectedCluster} onChange={handleClusterChange}>
                <option value="">Select a cluster</option>
                {clusterNames.map((name, index) => (
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