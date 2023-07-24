import React, { useState, useEffect } from 'react';

const SchemaAndTables = () => {
    const [data, setData] = useState([]);
    const [selectedDBMS, setSelectedDBMS] = useState('');
    const [dbmsNames, setDBMSNames] = useState([]);

    const fetchSchemaAndTables = async () => {
        try {
            const response = await fetch(`/describe/dbms/schemas?databaseName=${selectedDBMS}`);
            const data = await response.json();
            setData(data);
            console.log(data);
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

    const handleDBMSChange = (e) => {
        const selectedDBMS = e.target.value;
        setSelectedDBMS(selectedDBMS);
        console.log(selectedDBMS);

    };

    useEffect(() => {
        fetchDBMSNames();
    }, []);

    useEffect(() => {
        if (selectedDBMS) {
            fetchSchemaAndTables();
        }
    }, [selectedDBMS]);

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

            <h2>Schema And Table Information</h2>
            <ul>
                {data.map((schemaData) => (
                    <li key={schemaData.schemaName}>
                        <h3>Schema: {schemaData.schemaName}</h3>
                        <ul>
                            {schemaData.tableInfos.map((table) => (
                                <li key={table.tableName}>
                                    Table: {table.tableName}, Size: {table.tableSizeByte} bytes, Rows: {table.tableRowCount}
                                </li>
                            ))}
                        </ul>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default SchemaAndTables;
