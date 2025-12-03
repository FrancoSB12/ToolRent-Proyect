import httpClient from './http-common';

const createType = data => {
    return httpClient.post('/api/tool-types', data);
}

const getAllTypes = () => {
    return httpClient.get('/api/tool-types/');
}

const getTypeById = id => {
    return httpClient.get(`/api/tool-types/${id}`);
}

const getTypeByName = name => {
    return httpClient.get(`/api/tool-types/name/${name}`);
}

const updateType = (id, data) => {
    return httpClient.put(`/api/tool-types/tool-type/${id}`, data);
}

const deleteType = id => {
    return httpClient.delete(`/api/tool-types/delete/${id}`);
}

export default { createType, getAllTypes, getTypeById, getTypeByName, updateType, deleteType };