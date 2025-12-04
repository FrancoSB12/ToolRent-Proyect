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

const updateReplacementValue = (id, newValue) => {
    const toolTypeUpdate = {
        id: id,
        replacementValue: newValue
    };

    return httpClient.put(`/api/tool-types/tool-type/${id}`, toolTypeUpdate);
};

const updateRentalFee = (id, newFee) => {
    const toolTypeUpdate = {
        id: id,
        rentalFee: newFee
    };

    return httpClient.put(`/api/tool-types/tool-type/${id}`, toolTypeUpdate);
};

const deleteType = id => {
    return httpClient.delete(`/api/tool-types/delete/${id}`);
}

export default { createType, getAllTypes, getTypeById, getTypeByName, updateReplacementValue, updateRentalFee, deleteType };