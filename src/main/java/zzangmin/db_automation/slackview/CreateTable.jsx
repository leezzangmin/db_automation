import React, { useState, useEffect } from 'react';

const CreateTable = () => {
    const [response, setResponse] = useState(null);
    const [selectedDBMS, setSelectedDBMS] = useState('');
    const [dbmsNames, setDBMSNames] = useState([]);
    const [selectedSchema, setSelectedSchema] = useState('');
    const [selectedTable, setSelectedTable] = useState('table_name');
    const [columns, setColumns] = useState([
        {
            name: '',
            type: '',
            isNull: true,
            defaultValue: '',
            isUnique: false,
            isAutoIncrement: false,
            comment: '',
            charset: 'utf8mb4',
            collate: 'utf8mb4_0900_ai_ci',
        },
    ]);
    const [constraints, setConstraints] = useState([{ type: 'PRIMARY KEY', keyName: '', keyColumnNames: [''] }]);
    const [engine, setEngine] = useState('InnoDB');
    const [charset, setCharset] = useState('utf8mb4');
    const [collate, setCollate] = useState('utf8mb4_0900_ai_ci');
    const [tableComment, setTableComment] = useState('');
    const [schemaNames, setSchemaNames] = useState([]);

    const handleCreateTable = async () => {
        const url = `/ddl/table?databaseName=${selectedDBMS}`;
        const requestBody = {
            commandType: "CREATE_TABLE",
            schemaName: selectedSchema,
            tableName: selectedTable,
            columns,
            constraints,
            engine,
            charset,
            collate,
            tableComment
        };

        try {
            console.log('Create Table Request:', requestBody); // Logging the request body

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


    const handleDBMSChange = (e) => {
        const selectedDBMS = e.target.value;
        setSelectedDBMS(selectedDBMS);
        setSelectedSchema('');

        if (selectedDBMS) {
            fetchSchemas(selectedDBMS);
        } else {
            setSchemaNames([]);
        }
    };

    const handleSchemaChange = (e) => {
        const selectedSchema = e.target.value;
        setSelectedSchema(selectedSchema);

    };

    const handleTableChange = (e) => {
        const selectedTable = e;
        setSelectedTable(selectedTable);
    };


    useEffect(() => {
        fetchDBMSNames();
    }, []);


    const handleColumnNameChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].name = value;
        setColumns(updatedColumns);
    };

    const handleColumnTypeChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].type = value;
        setColumns(updatedColumns);
    };

    const handleColumnIsNullChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].isNull = value;
        setColumns(updatedColumns);
    };

    const handleColumnDefaultValueChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].defaultValue = value;
        setColumns(updatedColumns);
    };

    const handleColumnIsUniqueChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].isUnique = value;
        setColumns(updatedColumns);
    };

    const handleColumnIsAutoIncrementChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].isAutoIncrement = value;
        setColumns(updatedColumns);
    };

    const handleColumnCommentChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].comment = value;
        setColumns(updatedColumns);
    };

    const handleColumnCharsetChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].charset = value;
        setColumns(updatedColumns);
    };

    const handleColumnCollateChange = (index, value) => {
        const updatedColumns = [...columns];
        updatedColumns[index].collate = value;
        setColumns(updatedColumns);
    };

    const handleAddColumn = () => {
        setColumns([...columns, { name: '', type: '', isNull: true, defaultValue: '', isUnique: false, isAutoIncrement: false, comment: '', charset: '', collate: '' }]);
    };

    const handleRemoveColumn = (index) => {
        const updatedColumns = [...columns];
        updatedColumns.splice(index, 1);
        setColumns(updatedColumns);
    };

    const handleConstraintTypeChange = (index, value) => {
        const updatedConstraints = [...constraints];
        updatedConstraints[index].type = value;
        setConstraints(updatedConstraints);
    };

    const handleConstraintKeyNameChange = (index, value) => {
        const updatedConstraints = [...constraints];
        updatedConstraints[index].keyName = value;
        setConstraints(updatedConstraints);
    };

    const handleConstraintColumnNameChange = (constraintIndex, columnIndex, value) => {
        const updatedConstraints = [...constraints];
        updatedConstraints[constraintIndex].keyColumnNames[columnIndex] = value;
        setConstraints(updatedConstraints);
    };

    const handleAddConstraint = () => {
        setConstraints([...constraints, { type: 'PRIMARY KEY', keyName: '', keyColumnNames: [''] }]);
    };

    const handleRemoveConstraint = (index) => {
        const updatedConstraints = [...constraints];
        updatedConstraints.splice(index, 1);
        setConstraints(updatedConstraints);
    };
    const handleAddColumnName = (constraintIndex) => {
        const updatedConstraints = [...constraints];
        updatedConstraints[constraintIndex].keyColumnNames.push('');
        setConstraints(updatedConstraints);
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

        <label>table Name:</label>
        <input type="text" value={selectedTable} onChange={(e) => handleTableChange(e.target.value)} /><br />


        <label>Columns:</label><br />
      {columns.map((column, columnIndex) => (
        <div key={columnIndex}>
          <label>Column Name:</label><br />
          <input type="text" value={column.name} onChange={(e) => handleColumnNameChange(columnIndex, e.target.value)} /><br />

          <label>Column Type:</label><br />
          <input type="text" value={column.type} onChange={(e) => handleColumnTypeChange(columnIndex, e.target.value)} /><br />

          <label>Null:</label><br />
          <input type="checkbox" checked={column.isNull} onChange={(e) => handleColumnIsNullChange(columnIndex, e.target.checked)} /><br />

          <label>Default value:</label><br />
          <input type="text" value={column.defaultValue} onChange={(e) => handleColumnDefaultValueChange(columnIndex, e.target.value)} /><br />

          <label>Unique:</label><br />
          <input type="checkbox" checked={column.isUnique} onChange={(e) => handleColumnIsUniqueChange(columnIndex, e.target.checked)} /><br />

          <label>Auto Increment:</label><br />
          <input type="checkbox" checked={column.isAutoIncrement} onChange={(e) => handleColumnIsAutoIncrementChange(columnIndex, e.target.checked)} /><br />

          <label>Column Comment:</label><br />
          <input type="text" value={column.comment} onChange={(e) => handleColumnCommentChange(columnIndex, e.target.value)} /><br />

          <label>Column Charset:</label><br />
          <input type="text" value={column.charset} onChange={(e) => handleColumnCharsetChange(columnIndex, e.target.value)} /><br />

          <label>Column Collate:</label><br />
          <input type="text" value={column.collate} onChange={(e) => handleColumnCollateChange(columnIndex, e.target.value)} /><br />

          <button onClick={() => handleRemoveColumn(columnIndex)}>Remove Column</button><br /><br />
        </div>
      ))}
      <button onClick={handleAddColumn}>Add Column</button><br /><br />

      <label>Constraints:</label><br />
      {constraints.map((constraint, constraintIndex) => (
        <div key={constraintIndex}>
          <select value={constraint.type} onChange={(e) => handleConstraintTypeChange(constraintIndex, e.target.value)}>
            <option value="">Select a constraint type</option>
            <option value="PRIMARY KEY">PRIMARY KEY</option>
            <option value="KEY">KEY</option>
            <option value="UNIQUE KEY">UNIQUE KEY</option>
          </select><br />

          <label>Constraint name:</label><br />
          <input type="text" value={constraint.keyName} onChange={(e) => handleConstraintKeyNameChange(constraintIndex, e.target.value)} /><br />
        <label>Constraint Column names:</label><br />

            {constraint.keyColumnNames.map((columnName, columnIndex) => (
                <div key={columnIndex}>
                    <input
                        type="text"
                        value={columnName}
                        onChange={(e) => handleConstraintColumnNameChange(constraintIndex, columnIndex, e.target.value)}
                    /><br />
                </div>
            ))}
            <button onClick={() => handleAddColumnName(constraintIndex)}>Add Column Name</button><br /><br />

          <button onClick={() => handleRemoveConstraint(constraintIndex)}>Remove Constraint</button><br /><br />
        </div>
      ))}
      <button onClick={handleAddConstraint}>Add Constraint</button><br /><br />

      <label>Engine:</label><br />
      <input type="text" value={engine} onChange={(e) => setEngine(e.target.value)} /><br /><br />

      <label>Charset:</label><br />
      <input type="text" value={charset} onChange={(e) => setCharset(e.target.value)} /><br /><br />

      <label>Collate:</label><br />
      <input type="text" value={collate} onChange={(e) => setCollate(e.target.value)} /><br /><br />

      <label>Table Comment:</label><br />
      <input type="text" value={tableComment} onChange={(e) => setTableComment(e.target.value)} /><br /><br />

      <button onClick={handleCreateTable}>Create Table</button><br /><br />

      {response && (
        <div>
          <p>Response:</p>
          <pre>{JSON.stringify(response, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

export default CreateTable;
