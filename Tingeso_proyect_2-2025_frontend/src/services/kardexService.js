import httpClient from './http-common';

const getkardexByToolName = (toolName) => {
    return httpClient.get(`/api/kardexes/tool/${toolName}`);
}

const getAllKardex = () => {
    return httpClient.get('/api/kardexes/');
}

const getKardexByDateRange = (startDate, endDate) => {
    return httpClient.get(`/api/kardexes/by-date?start=${startDate}&end=${endDate}`);
}

export default { getkardexByToolName, getAllKardex, getKardexByDateRange };