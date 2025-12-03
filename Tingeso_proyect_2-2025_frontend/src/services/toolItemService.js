import httpClient from './http-common';

const createItem = data => {
  return httpClient.post('/api/tool-items', data);
}

const getAllItems = () => {
  return httpClient.get('/api/tool-items/');
}

const getItemById = id => {
  return httpClient.get(`/api/tool-items/${id}`);
}

const getItemBySerialNumber = serialNumber => {
  return httpClient.get(`/api/tool-items/serial-number/${serialNumber}`);
}

const getFirstAvailableByType = (toolTypeId) => {
  return httpClient.get(`/api/tool-items/available/type/${toolTypeId}`);
}

const updateItem = (id, data) => {
  return httpClient.put(`/api/tool-items/tool-item/${id}`, data);
}

const disableItem = (serialNumber, data) => {
  return httpClient.put(`/api/tool-items/disable-tool-item/${serialNumber}`, data);
}

const enableItem = (id, data) => {
  return httpClient.put(`/api/tool-items/enable-tool-item/${id}`, data);
}

const evaluateItemDamage = (toolId, toolData) => {
  return httpClient.put(`/api/tool-items/evaluate-damage/${toolId}`, toolData);
}

const removeItem = id => {
  return httpClient.delete(`/api/tool-items/delete/${id}`);
}

export default { createItem, getAllItems, getItemById, getItemBySerialNumber, getFirstAvailableByType, updateItem, disableItem, enableItem, evaluateItemDamage, removeItem };