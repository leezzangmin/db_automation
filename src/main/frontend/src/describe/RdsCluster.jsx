import React, { useState, useEffect } from 'react';
import axios from 'axios';

const RdsCluster = () => {
    const [clustersData, setClustersData] = useState([]);

    const fetchData = async () => {
        try {
            const response = await axios.get('/describe/cluster');
            setClustersData(response.data.clusters);
            console.log(clustersData);
        } catch (error) {
            console.error('Error fetching data:', error);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    return (
        <div>
            <h1>RDS Clusters Information</h1>
            <ul>
                {clustersData.map((cluster) => (
                    <li key={cluster.clusterName}>
                        <p>Cluster Name: {cluster.clusterName}</p>
                        <p>Engine Version: {cluster.engineVersion}</p>
                        <p>Multi-AZ Enabled: {cluster.multiAzEnabled ? 'Yes' : 'No'}</p>
                        <p>Deletion Protection Enabled: {cluster.deletionProtectionEnabled ? 'Yes' : 'No'}</p>
                        <p>CPU Usage: {cluster.cpuUsage}</p>
                        <p>Freeable Memory: {cluster.freeableMemory}</p>
                        <p>Average Active Session: {cluster.averageActiveSession}</p>
                        <p>Connection: {cluster.connection}</p>
                        <p>Free Local Storage: {cluster.freeLocalStorage}</p>
                        <p>Select Throughput: {cluster.selectThroughput}</p>
                        <p>Write Throughput: {cluster.writeThroughput}</p>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default RdsCluster;
