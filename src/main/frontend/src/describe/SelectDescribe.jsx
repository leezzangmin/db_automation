import React, { useState } from 'react';
import RdsCluster from "./RdsCluster";


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

            </select>

            {selectedPage === 'describeRdsCluster' && (
                <div>
                    <RdsCluster />
                </div>
            )}

        </div>
    );
};

export default SelectDescribe;
