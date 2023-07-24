import React, { useState } from 'react';
import RdsCluster from "./RdsCluster";
import SchemaAndTables from "./SchemaAndTables";
import TableStatusInfo from "./TableStatusInfo";


const SelectDescribe = () => {
    const [selectedPage, setSelectedPage] = useState('');

    const handleChangePage = (e) => {
        setSelectedPage(e.target.value);
    };

    return (
        <div>
            <label>Select Describe Page:</label>
            <select value={selectedPage} onChange={handleChangePage}>
                <option value="">Select a Describe page</option>
                <option value="describeRdsCluster">Describe Rds Clusters</option>
                <option value="describeSchemaAndTables">Describe Schema And Table</option>
                <option value="describeTableInfo">Describe Table Info</option>

            </select>

            {selectedPage === 'describeRdsCluster' && (
                <div>
                    <RdsCluster />
                </div>
            )}

            {selectedPage === 'describeSchemaAndTables' && (
                <div>
                    <SchemaAndTables />
                </div>
            )}

            {selectedPage === 'describeTableInfo' && (
                <div>
                    <TableStatusInfo />
                </div>
            )}

        </div>
    );
};

export default SelectDescribe;
