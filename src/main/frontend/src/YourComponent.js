import React, { useState } from 'react';

const YourComponent = () => {
    const [response, setResponse] = useState(null);

    const handleAddColumn = async () => {
        const url = '/ddl/column?databaseName=zzangmin-db';
        console.log("adsf")
        const requestBody = {
            commandType: "ADD_COLUMN",
            schemaName: 'test_schema',
            tableName: 'macdy_tdableekccazdcece',
            column: {
                name: 'column_newwwwww',
                type: "VARCHAR(255)",
                isNull: true,
                defaultValue: null,
                isUnique: false,
                isAutoIncrement: false,
                comment: "Column 1 comment",
                charset: "utf8mb4",
                collate: "utf8mb4_0900_ai_ci"
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
            <button onClick={handleAddColumn}>Add Column</button>
            {response && (
                <div>
                    <p>Response:</p>
                    <pre>{JSON.stringify(response, null, 2)}</pre>
                </div>
            )}
            <body>asdf</body>

        </div>
    );
};

export default YourComponent;
